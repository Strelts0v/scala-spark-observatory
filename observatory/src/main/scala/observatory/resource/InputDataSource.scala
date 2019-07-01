package observatory.resource

import observatory.{Location, StationId, StationTemperature}

import scala.io.Source
import scala.util.{Failure, Try}

/**
  * Abstract the source of input data.
  */
abstract class InputDataSource {

  /**
    * @param path path of an input file relative to some context implemented in subclasses
    */
  def dataFileStream(path: String): Source

  /**
    * Any Non-numeric input results in the key's component being None
    * @param stnStr STN number as string or the empty string
    * @param wbanStr WBAN number as string or the empty string
    * @return StationId
    */
  def parseStationId(stnStr: String, wbanStr: String) = StationId(Try(stnStr.toInt).toOption,
    Try(wbanStr.toInt).toOption)

  /**
    * Parse a line from the stations file
    * @param str String of STN,WBAN,LAT,LON
    * @return Parsed line
    */
  def parseStationsLine(str: String): Option[(StationId, Location)] = {
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
  def parseTempsLine(str: String): Option[StationTemperature] = {
    val tryRecord = str.split(",") match {
      case Array(stn, wban, month, day, temp) =>
        val id = parseStationId(stn, wban)
        Try(StationTemperature(id, month.toInt, day.toInt, temp.toDouble))
      case _ => Failure(new RuntimeException("Parse failed"))
    }
    tryRecord.toOption
  }

  /**
    * Parsing whole file
    */
  def parseStationsFile(stationsFile: String): Map[StationId, Location] = {
    val lineStream = dataFileStream(stationsFile).getLines
    lineStream.flatMap(parseStationsLine).toMap
  }

  def parseTemperatureFile(temperaturesFile: String) : Iterable[StationTemperature] = {
    val lineStream = dataFileStream(temperaturesFile).getLines
    lineStream.flatMap(parseTempsLine).toIterable
  }

}