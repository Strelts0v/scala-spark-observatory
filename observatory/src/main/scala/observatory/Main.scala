package observatory

import observatory.Extraction.{locateTemperatures, locationYearlyAverageRecords}

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
  }

  def doWeek3(): Unit = {
    val temperatures = locationYearlyAverageRecords(locateTemperatures(1975, "/stations.csv", "/1975.csv"))

    println("tile1")
    val tile = Interaction.tile(temperatures, colors, Tile(0, 0, 1))
    tile.output("./tile1.png")
  }

  doWeek3()

}
