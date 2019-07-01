package observatory

import java.time.LocalDate

import observatory.resource.ResourceDataSource

import scala.io.Source
import observatory.util.TemperatureConversionUtils._
import observatory.util.ParsingUtils._

import scala.util.Try

/**
  * 1st milestone: data extraction
  */
object Extraction {

  val dataFileSource = new ResourceDataSource()

  /**
    * @param year year number
    * @param stationsFile path of the stations resource file to use (e.g. "/stations.csv")
    * @param temperaturesFile path of the temperatures resource file to use (e.g. "/1975.csv")
    * @return A sequence containing triplets (date, location, temperature)
    */
  def locateTemperatures(year: Year, stationsFile: String, temperaturesFile: String)
    : Iterable[(LocalDate, Location, Temperature)] = {

    val stationsMap = dataFileSource.parseStationsFile(stationsFile)
    dataFileSource.parseTemperatureFile(temperaturesFile).flatMap(toLocatedTemperature(year, stationsMap))
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
  def locationYearlyAverageRecords(records: Iterable[(LocalDate, Location, Temperature)])
    : Iterable[(Location, Temperature)] = {

    case class Avg(count: Int, totalTemp: Double)

    val resultMap = records.foldLeft[Map[Location, Avg]](Map.empty) { (avg, record) =>
      record match {
        case (date, location, temperature) =>
          val data = avg.getOrElse(location, Avg(0, 0.0))
          avg.updated(location, Avg(data.count + 1, data.totalTemp + temperature))
      }
    }
    resultMap.mapValues(a => a.totalTemp / a.count)
  }
}
