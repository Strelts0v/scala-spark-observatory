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
import observatory.constant.ColorConstants._
import observatory.config.DataConfig._

import scala.io.Source
import scala.math.pow

object Main extends App {

  def doWeek1(): Unit = {
    val temperatures: Map[Int, Iterable[(Location, Double)]] = {
      for {
        year <- firstYear until lastYear
      } yield (year, locationYearlyAverageRecords(locateTemperatures(year, "/stations.csv", s"/$year.csv")))
    }.toMap
  }

  def doWeek2(): Unit = {
    val temperatures = locationYearlyAverageRecords(locateTemperatures(1975, "/stations.csv", "/1975.csv"))
    val image = Visualization.visualize(temperatures, temperaturesColorScale)
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
        val image = Interaction.tile(data, temperaturesColorScale, Tile(tile.x, tile.y, tile.zoom))
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
    val years: RDD[Int] = sc.parallelize(firstYear until lastYear, 32)

    val temps: RDD[(Int, Iterable[(Location, Double)])] = years.map( (year: Int) => {
      println(s"OBSERVATORY: Loading data for year ${year}")
      (year, sparkExtractor.locationYearlyAverageRecords(sparkExtractor.locateTemperatures(year, "/stations.csv", s"/${year}.csv")))
    })

    val grids: RDD[(Int, Grid)] = temps.map({
      case (year: Int, temperatures: Iterable[(Location, Double)]) => {
        println(s"OBSERVATORY: Generating grid for year ${year}")
        (year, GridBuilder.fromIterable(temperatures))
      }
    }).cache

    // Calculate normals from 1975-1989
    // Broadcast result to all nodes
    val normalGridVar = sc.broadcast(averageGridRDD(grids.filter(_._1 < normalYearsBefore).map(_._2)))

    // Calculate anomalies for 1990-2015
    val anomalies: RDD[(Int, Grid)] = grids.filter({
      case (year: Int, g: Grid) => year >= normalYearsBefore
    }).map({
      case (year: Int, g: Grid) => (year, g.diff(normalGridVar.value))
    })

    def makeTiles(gridRDD: RDD[(Int, Grid)], subDir: String, colorScale: List[(Double, Color)]): Unit = {

      val tileParams = gridRDD.flatMap({
        case (year: Int, grid: Grid) => for {
          zoom <- 0 until 4
          y <- 0 until pow(2.0, zoom).toInt
          x <- 0 until pow(2.0, zoom).toInt
        } yield (year, zoom, x, y, grid)
      })

      tileParams.foreach({
        case (year: Int, zoom: Int, x: Int, y: Int, grid: Grid) => {
          val tileDir = new File(s"${resourceDir}/$subDir/${year}/$zoom")
          tileDir.mkdirs()
          val tileFile = new File(tileDir, s"$x-$y.png")

          if (tileFile.exists()) {
            println(s"$subDir tile for $year $zoom:$x:$y already exists")
          }
          else {
            println(s"Generating $subDir tile for $year $zoom:$x:$y")
            val tile: Image = Visualization2.visualizeGrid(
              grid.asFunction(),
              colorScale,
              Tile(x, y, zoom)
            )
            println(s"Done $subDir tile $zoom:$x:$y for $year")
            tile.output(tileFile)
          }

          ()
        }
      })
    }

    // Create anomaly tiles
    makeTiles(anomalies, "deviations", temperaturesColorScale)

    // Create normal tiles
    makeTiles(grids, "temperatures", anomaliesColorScale)
  }

  override def main(args: Array[String]): Unit = {

    // First argument is the resources directory
    val resourceDir = args(0)

    doWeek5(resourceDir)

  }
}

