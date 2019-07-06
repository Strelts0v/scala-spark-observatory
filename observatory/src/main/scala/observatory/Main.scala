package observatory

import java.io.File

import org.apache.spark.SparkConf
import org.apache.spark.SparkContext
import org.apache.spark.rdd.RDD
import Extraction._
import observatory.grid.{Grid, GridBuilder}
import Manipulation._
import com.sksamuel.scrimage.Image
import observatory.resource.{DataExtractor, DataSource}

import scala.io.Source
import scala.math.pow

object Main extends App {

  val firstYear = 1975
  val lastYear = 2016

  val colors: List[(Double, Color)] = List(
    (60.0, Color(255, 255, 255)),
    (32.0, Color(255, 0, 0)),
    (12.0, Color(255, 255, 0)),
    (0.0, Color(0, 255, 255)),
    (-15.0, Color(0, 0, 255)),
    (-27.0, Color(255, 0, 255)),
    (-50.0, Color(33, 0, 107)),
    (-60.0, Color(0, 0, 0))
  )

  val anomalyColors: List[(Double, Color)] = List(
    (7.0, Color(0, 0, 0)),
    (4.0, Color(255, 0, 0)),
    (2.0, Color(255, 255, 0)),
    (0.0, Color(255, 255, 255)),
    (-2.0, Color(0, 255, 255)),
    (-7.0, Color(0, 0, 255))
  )

  def doWeek1(): Unit = {
    val temperatures: Map[Int, Iterable[(Location, Double)]] = {
      for {
        year <- firstYear until lastYear
      } yield (year, locationYearlyAverageRecords(locateTemperatures(year, "/stations.csv", s"/$year.csv")))
    }.toMap
  }

  def doWeek2(): Unit = {
    val temperatures = locationYearlyAverageRecords(locateTemperatures(1975, "/stations.csv", "/1975.csv"))
    val image = Visualization.visualize(temperatures, colors)
    image.output("./map_1975.png")

    ()
  }

  def doWeek3(): Unit = {
    val temps = locationYearlyAverageRecords(locateTemperatures(2015, "/stations.csv", "/1975.csv"))

    def generateTile(year: Int, tile: Tile, data: Iterable[(Location, Double)]): Unit = {
      val tileDir = new File(s"./target/temperatures/2015/${tile.zoom}")
      tileDir.mkdirs()
      val tileFile = new File(tileDir, s"${tile.x}-${tile.y}.png")

      if (tileFile.exists ){
        println(s"Tile ${tile.zoom}:${tile.x}:y already exists")
      }
      else {
        println(s"Generating tile ${tile.zoom}:${tile.x}:${tile.y} for $year")
        val image = Interaction.tile(data, colors, Tile(tile.x, tile.y, tile.zoom))
        println(s"Done tile ${tile.zoom}:${tile.x}:${tile.y} for $year")
        image.output(tileFile)
      }

      ()
    }

    Interaction.generateTiles(List((1975, temps)), generateTile)
  }

  def doWeek5(resourceDir: String): Unit = {
    // Setup Spark environment
    val conf: SparkConf = new SparkConf()
      .setAppName("Observatory")
      .setMaster("local[3]")
      .set("spark.executor.memory", "3g")

    val sc: SparkContext = new SparkContext(conf)

    val lookupResource: DataSource.Lookup = (path: String) => {
      Source.fromFile(s"${resourceDir}/${path}")
    }
    val sparkExtractor = new DataExtractor(lookupResource)

    // Load data into RDDs
    val years: RDD[Int] = sc.parallelize(1975 until 2016, 32)
    val temps: RDD[(Int, Iterable[(Location, Double)])] = years.map( (year: Int) => {
      println(s"OBSERVATORY: Loading data for year ${year}")
      (year, sparkExtractor.locationYearlyAverageRecords(sparkExtractor.locateTemperatures(year, "/stations.csv", s"/${year}.csv")))
    })
    val grids: RDD[(Int, Grid)] = temps.map({
      case (year: Int, temps: Iterable[(Location, Double)]) => {
        println(s"OBSERVATORY: Generating grid for year ${year}")
        (year, GridBuilder.fromIterable(temps))
      }
    })

    // Calculate normals from 1975-1989
    // TODO : broadcast normalGrid
    val normalGrid: Grid = averageGridRDD(grids.filter(_._1 < 1990).map(_._2))
    // Calculate anomalies for 1990-2015
    val anomalies: RDD[(Int, Grid)] = grids.filter({
      case (year: Int, g: Grid) => year >= 1990
    }).map({
      case (year: Int, g: Grid) => (year, g.diff(normalGrid))
    })

    // TODO : Create tiles


    // Anomalies
    val tileParams = anomalies.flatMap({
      case (year: Int, grid: Grid) => for {
        zoom <- 0 until 4
        y <- 0 until pow(2.0, zoom).toInt
        x <- 0 until pow(2.0, zoom).toInt
      } yield (year, zoom, x, y, grid)
    }).repartition(32)

    tileParams.foreach({
      case (year: Int, zoom: Int, x: Int, y: Int, grid: Grid) => {
        println(s"Generating image for $year $zoom:$x:$y")
        val tile: Image = Visualization2.visualizeGrid(
          grid.asFunction(),
          anomalyColors,
          Tile(x, y, zoom)
        )

        val tileDir = new File(s"${resourceDir}/target/deviations/${year}/$zoom")
        tileDir.mkdirs()
        val tileFile = new File(tileDir, s"$x-$y.png")

        println(s"Done tile $zoom:$x:$y for $year")
        tile.output(tileFile)

        ()
      }

    })
  }

  override def main(args: Array[String]): Unit = {

    // First argument is the resources directory
    val resourceDir = args(0)

    doWeek5(resourceDir)

  }
}

