package observatory

import java.time.LocalDate

import scala.io.Source

import observatory.util.OptionUtils._

/**
  * 1st milestone: data extraction
  */
object Extraction {

  /**
    * @param year             Year number
    * @param stationsFile     Path of the stations resource file to use (e.g. "/stations.csv")
    * @param temperaturesFile Path of the temperatures resource file to use (e.g. "/1975.csv")
    * @return A sequence containing triplets (date, location, temperature)
    */
  def locateTemperatures(year: Year, stationsFile: String, temperaturesFile: String): Iterable[(LocalDate, Location, Temperature)] = {
    ???
  }

  /**
    * @param records A sequence containing triplets (date, location, temperature)
    * @return A sequence containing, for each location, the average temperature over the year.
    */
  def locationYearlyAverageRecords(records: Iterable[(LocalDate, Location, Temperature)]): Iterable[(Location, Temperature)] = {
    ???
  }

  def parseStationsStream(stationsFile: String): Map[StationIdentifier, Location] = {
    val lineStream = Source.fromInputStream(getClass.getResourceAsStream(stationsFile)).getLines
    val stationsDataStream = lineStream.map((str: String) => str.split(",")).map {
      case Array(stn, wban, lat, lon) => Some(
        StationIdentifier(optInt(stn), optInt(wban)) -> Location(lat.toDouble, lon.toDouble))
      case _ => None
    }
    stationsDataStream.flatten.toMap
  }

}
