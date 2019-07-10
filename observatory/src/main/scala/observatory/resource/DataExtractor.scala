package observatory.resource

import java.time.LocalDate

import observatory.{Location, StationId, StationTemperature}
import observatory.util.TemperatureConversionUtils._

import scala.util.{Failure, Try}

/**
  * Abstract the source of input data.
  *
  * @param dataSource: A function for getting an IO Source from a relative path.
  */
class DataExtractor(dataSource: DataSource.Lookup) extends Serializable {

  /**
    * @param year Year number
    * @param stationsFile Path of the stations resource file to use (e.g. "/stations.csv")
    * @param temperaturesFile Path of the temperatures resource file to use (e.g. "/1975.csv")
    * @return A sequence containing triplets (date, location, temperature)
    */
  def locateTemperatures(year: Int, stationsFile: String, temperaturesFile: String)
    : Iterable[(LocalDate, Location, Double)] = {

    val stationsMap = parseStationsFile(stationsFile)
    parseTemperaturesFile(temperaturesFile).flatMap(toLocatedTemperature(year, stationsMap))
  }

  def toLocatedTemperature(year: Int, stationsMap: Map[StationId, Location])(t: StationTemperature)
    : Option[(LocalDate, Location, Double)] = {

    Try(
      (
        LocalDate.of(year, t.month, t.day),
        stationsMap(t.id), fahrenheitToCelsius(t.temp)
      )
    ).toOption
  }

  /**
    * @param records A sequence containing triplets (date, location, temperature)
    * @return A sequence containing, for each location, the average temperature over the year.
    */
  def locationYearlyAverageRecords(records: Iterable[(LocalDate, Location, Double)]): Iterable[(Location, Double)] = {

    case class Accumulator(count: Int, total: Double)

    val totalsMap = records.foldLeft[Map[Location, Accumulator]](Map.empty) { (accumulator, stationTemperature) =>
      stationTemperature match {
        case (date, location, temp) => {
          val accLoc = accumulator.getOrElse(location, Accumulator(0, 0.0))
          accumulator.updated(location, Accumulator(accLoc.count + 1, accLoc.total + temp))
        }
      }
    }
    totalsMap.mapValues(acc => acc.total / acc.count).toIterable
  }


  /**
    * Any Non-numeric input results in the key's component  being None
    *
    * @param stnStr STN number as string or the empty string
    * @param wbanStr WBAN number as string or the empty string
    * @return StationKey
    */
  def parseStationId(stnStr: String, wbanStr: String) =
    StationId(Try(stnStr.toInt).toOption,
    Try(wbanStr.toInt).toOption)

  /**
    * Parse a line from the stations file
    *
    * @param str String of STN,WBAN,LAT,LON
    * @return Parsed line
    */
  def parseStationsLine(str: String): Option[(StationId, Location)] = {
    str.split(",") match {
      case Array(stn, wban, lat, lon) => Some(
        (parseStationId(stn, wban), Location(lat.toDouble, lon.toDouble))
      )
      case _ => None
    }
  }

  /**
    * Parse a line from a temperatures file
    *
    * @param str String of STN,WBAN,MONTH,DAY,TEMP
    * @return Parsed line
    */
  def parseTemperaturesLine(str: String): Option[StationTemperature] = {
    val record = str.split(",") match {
      case Array(stn, wban, month, day, temp) => {
        val stationId = parseStationId(stn, wban)
        Try(StationTemperature(stationId, month.toInt, day.toInt, temp.toDouble))
      }
      case _ => Failure(new RuntimeException("Parse failed"))
    }
    record.toOption
  }

  /**
    * Parse a whole stations file
    *
    * @param stationsFile path to a stations file
    * @return Map of parsed stations
    */
  def parseStationsFile(stationsFile: String): Map[StationId, Location] = {
    val lineStream = dataSource(stationsFile).getLines
    lineStream.flatMap(parseStationsLine).toMap
  }

  /**
    * Parse a whole temperatures file
    *
    * @param temperaturesFile path to a temperatures file
    * @return Iterable of parsed station temperatures
    */
  def parseTemperaturesFile(temperaturesFile: String) : Iterable[StationTemperature] = {
    val lineStream = dataSource(temperaturesFile).getLines
    lineStream.flatMap(parseTemperaturesLine).toIterable
  }

}
