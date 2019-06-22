package observatory.util

import observatory.{Location, StationId, StationTemperature}

import scala.io.Source
import scala.util.{Failure, Try}

object ParsingUtils {

  /**
    * Parse entire stations file
    * @param stationsFile path to the stations file
    * @return a map of StationId object and Location object
    */
  def parseStationsFile(stationsFile: String): Map[StationId, Location] = {
    val lineStream = Source.fromInputStream(getClass.getResourceAsStream(stationsFile)).getLines
    lineStream.flatMap(parseStationFromStr).toMap
  }

  /**
    * Parse entire temperatures file
    * @param temperaturesFile path to the stations file
    * @return a sequence containing StationTemperature objects
    */
  def parseTemperaturesFile(temperaturesFile: String) : Iterable[StationTemperature] = {
    val lineStream = Source.fromInputStream(getClass.getResourceAsStream(temperaturesFile)).getLines
    lineStream.flatMap(parseStationTemperatureFromStr).toIterable
  }

  /**
    * Parse a line from the stations file
    * @param str String of STN,WBAN,LAT,LON
    * @return Parsed line
    */
  private def parseStationFromStr(str: String): Option[(StationId, Location)] = {
    str.split(",") match {
      case Array(stn, wban, lat, lon) => Some((parseStationId(stn, wban), Location(lat.toDouble, lon.toDouble)))
      case _ => None
    }
  }

  /**
    * Parse a line from a temperatures file
    * @param str String of STN,WBAN,MONTH,DAY,TEMP
    * @return Parsed line
    */
  private def parseStationTemperatureFromStr(str: String): Option[StationTemperature] = {
    val tryRecord = str.split(",") match {
      case Array(stn, wban, month, day, temp) => {
        val id = parseStationId(stn, wban)
        for {
          month <- Try(month.toInt)
          day <- Try(day.toInt)
          temp <- Try(temp.toDouble)
        } yield StationTemperature(id, month, day, temp)
      }
      case _ => Failure(new RuntimeException("Parse failed"))
    }
    tryRecord.toOption
  }

  /**
    * Any invalid input results in the id component of type None
    * @param stnStr STN number as string or the empty string
    * @param wbanStr WBAN number as string or the empty string
    * @return StationId
    */
  def parseStationId(stnStr: String, wbanStr: String) = StationId(
    Try(stnStr.toInt).toOption,
    Try(wbanStr.toInt).toOption
  )

}
