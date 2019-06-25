package observatory.calculation

import observatory.constant.CalculationConstants.earthRadius
import observatory.Location

import observatory.constant.CalculationConstants._

import scala.annotation.tailrec
import scala.math.{acos, cos, pow, sin}

object InterpolationCalculations {

  def greatCircleDistance(fi1: Location, fi2: Location): Double = {
    val deltaLambda = ((fi1.lon max fi2.lon) - (fi1.lon min fi2.lon)) * radianConversionRate
    val sigma = acos(
      sin(fi1.lat * radianConversionRate) * sin(fi2.lat*radianConversionRate) +
      cos(fi1.lat * radianConversionRate) * cos(fi2.lat * radianConversionRate) * cos(deltaLambda)
    )
    earthRadius * sigma
  }

  def inverseDistanceWeighting(sampleValues: Iterable[(Location, Double)], location: Location, power: Double) = {

    @tailrec
    def tailRecHelper(values: Iterator[(Location, Double)], valSum: Double, weightSum: Double): Double = {
      values.next match {
        case (fi1, fi2) =>
          val arcDistance = greatCircleDistance(location, fi1)
          if (arcDistance < minArcDistance) fi2
          else {
            val weight = 1.0 / pow(arcDistance, power)
            if (values.hasNext) tailRecHelper(values, valSum + weight*fi2, weightSum + weight)
            else (valSum + weight*fi2) / (weightSum + weight)
          }
      }
    }

    tailRecHelper(sampleValues.toIterator, 0.0, 0.0)
  }

}
