package observatory.calculation

import observatory.constant.CalculationConstants.earthRadius
import observatory.Location

import observatory.constant.CalculationConstants._

import scala.annotation.tailrec
import scala.math.{acos, cos, pow, sin}

/**
  * Interpolation calculation utility object
  */
object InterpolationCalculations {

  /**
    * This method helps to approximate the distance between two locations using
    * great circle distance formula which is is the shortest distance between
    * two points on the surface of a sphere, measured along the surface of the sphere
    *
    * @param fi1 Point #1
    * @param fi2 Point #2
    * @return The shortest distance between two points on the surface of a sphere
    */
  def greatCircleDistance(fi1: Location, fi2: Location): Double = {
    val deltaLambda = ((fi1.lon max fi2.lon) - (fi1.lon min fi2.lon)) * radianConversionRate
    val sigma = acos(
      sin(fi1.lat * radianConversionRate) * sin(fi2.lat * radianConversionRate) +
      cos(fi1.lat * radianConversionRate) * cos(fi2.lat * radianConversionRate) * cos(deltaLambda)
    )
    earthRadius * sigma
  }

  /**
    * Inverse distance weighting (IDW) is a type of interpolation with a known
    * scattered set of points. The assigned values to unknown points are calculated
    * with a weighted average of the values available at the known points.
    *
    * @param sampleValues Known points included of location and temperature
    * @param location Location where we want to predict tempreture
    * @param power Constant value for calculation (should greater or equal to 2)
    * @return Predicted temperature value
    */
  def inverseDistanceWeighting(sampleValues: Iterable[(Location, Double)], location: Location, power: Double) : Double = {

    @tailrec
    def tailRecHelper(values: Iterator[(Location, Double)], valSum: Double, weightSum: Double): Double = {
      values.next match {
        case (fi1, fi2) =>
          val arcDistance = greatCircleDistance(location, fi1)
          if (arcDistance < minArcDistance) fi2
          else {
            val weight = 1.0 / pow(arcDistance, power)
            if (values.hasNext) tailRecHelper(values, valSum + weight * fi2, weightSum + weight)
            else (valSum + weight*fi2) / (weightSum + weight)
          }
      }
    }

    tailRecHelper(sampleValues.toIterator, 0.0, 0.0)
  }

}
