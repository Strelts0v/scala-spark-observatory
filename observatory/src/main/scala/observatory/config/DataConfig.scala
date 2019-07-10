package observatory.config

/**
  * Configuration for period analyzing
  */
object DataConfig {

  /** Year to begin analyzing */
  val firstYear = 1975

  /** Year to end analyzing */
  val lastYear = 2016

  /** firstYear value and this value create period for calculation average
    * normal temperatures to compute deviations after this year to lastYear*/
  val lastReferenceYear = 1990

}
