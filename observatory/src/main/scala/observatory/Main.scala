package observatory

import org.apache.spark.SparkConf
import org.apache.spark.SparkContext
import org.apache.spark.rdd.RDD
import observatory.grid.{Grid, GridBuilder}
import Manipulation._
import observatory.resource.{DataExtractor, DataSource}
import observatory.constant.ColorConstants._
import observatory.config.DataConfig._

import scala.io.Source

object Main extends App {

  override def main(args: Array[String]): Unit = {

    // First argument is the resources directory where all
    // generated temperatures and deviations will be saved
    val resourceDir = args(0)

    // Setup Spark environment
    val conf: SparkConf = new SparkConf()
      .setAppName("Observatory")
      .setMaster("local[6]")
      .set("spark.executor.memory", "4g")

    val sc: SparkContext = new SparkContext(conf)

    val lookupResource: DataSource.Lookup = (path: String) => {
      Source.fromFile(s"${resourceDir}/${path}")
    }

    val sparkExtractor = new DataExtractor(lookupResource)

    // Load data into RDDs
    val years: RDD[Int] = sc.parallelize(firstYear until lastYear, 32)

    val temperatures: RDD[(Int, Iterable[(Location, Double)])] = years.map((year: Int) => {
      println(s"Observatory: Loading data for year ${year}")
      (year, sparkExtractor.locationYearlyAverageRecords(sparkExtractor.locateTemperatures(year, "/stations.csv", s"/${year}.csv")))
    })

    val grids: RDD[(Int, Grid)] = temperatures.map({
      case (year: Int, temperatures: Iterable[(Location, Double)]) => {
        println(s"Observatory: Generating grid for year ${year}")
        (year, GridBuilder.fromIterable(temperatures))
      }
    }).cache  // caching result grids in memory

    // Calculate normals from 1975-1989
    // Broadcast result to all nodes
    val normalGridVar = sc.broadcast(averageGridRdd(grids.filter(_._1 < lastReferenceYear).map(_._2)))

    // Calculate anomalies for 1990-2015
    val anomalies: RDD[(Int, Grid)] = grids.filter({
      case (year: Int, g: Grid) => year >= lastReferenceYear
    }).map({
      case (year: Int, g: Grid) => (year, g.diff(normalGridVar.value))
    })

    // Create anomaly tiles
    makeTiles(anomalies, anomaliesColorScale, s"${resourceDir}/deviations")

    // Create normal tiles
    makeTiles(grids, temperaturesColorScale, s"${resourceDir}/temperatures")

  }
}

