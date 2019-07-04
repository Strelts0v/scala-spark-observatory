package observatory

import java.io.File

import org.apache.spark.SparkConf
import org.apache.spark.SparkContext
import org.apache.spark.rdd.RDD
import Extraction._
import observatory.grid.Grid
import Manipulation._

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


  // Note: The climatalogical term for what the course descrives as temperature deviations is "anomalies"
  def doWeek5(): Unit = {

    // Setup Spark environment
    val conf: SparkConf = new SparkConf()
      .setAppName("Observatory")
      .setMaster("local[3]")
      .set("spark.executor.memory", "3g")

    val sc: SparkContext = new SparkContext(conf)

    // Load data into RDDs
    val years: RDD[Int] = sc.parallelize(1975 until 2016)
    val temps: RDD[(Int, Iterable[(Location, Double)])] = years.map( (year: Int) => {
      (year, locationYearlyAverageRecords(locateTemperatures(year, "/stations.csv", s"/${year}.csv")))
    })
    val grids: RDD[(Int, Grid)] = temps.map({
      case (year: Int, temps: Iterable[(Location, Double)]) => (year, new Grid(360, 180, temps))
    })

    // Calculate normals from 1975-1989
    val normalGrid: Grid = averageGridRDD(grids.filter(_._1 < 1990).map(_._2))
    // TODO : Calculate anomalies for 1990-2015

    // TODO : Create tiles
  }

  doWeek5()

}

