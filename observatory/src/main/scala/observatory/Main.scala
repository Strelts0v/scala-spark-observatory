package observatory

import observatory.util.ParsingUtils
import org.apache.log4j.{Level, Logger}

object Main extends App {

  Logger.getLogger("org.apache.spark").setLevel(Level.WARN)

  val firstYear = 1975
  val lastYear = 2016

  println("Loading stations...")
  val stations = ParsingUtils.parseStationsFile("/stations.csv")
  println("Loading of stations has been accomplished!")
  println(s"Stations total size: ${stations.size}")
  val temperatures = (firstYear until lastYear) map { year => {
    println(s"Loading file with temperatures for year: $year")
    (year, ParsingUtils.parseTemperaturesFile(s"/$year.csv"))
  }}

  println(s"Temperatures total size: ${temperatures.length}")
}
