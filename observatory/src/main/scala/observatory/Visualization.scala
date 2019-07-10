package observatory

import com.sksamuel.scrimage.Image
import observatory.calculation.InterpolationCalculations._
import observatory.constant.CalculationConstants._
import observatory.visualization.MapVisualizer

/**
  * Object for basic visualization strategy
  */
object Visualization {

  /**
    * This method takes a sequence of known temperatures at the given locations,
    * and a location where we want to guess the temperature, and returns an estimate
    * based on the inverse distance weighting algorithm
    *
    * @param temperatures Known temperatures: pairs containing a location and the temperature at this location
    * @param location Location where to predict the temperature
    * @return The predicted temperature at `location`
    */
  def predictTemperature(temperatures: Iterable[(Location, Temperature)], location: Location): Temperature = {
    inverseDistanceWeighting(temperatures, location, interpolationPower)
  }

  /**
    * This method takes a sequence of reference temperature values and their associated color,
    * and a temperature value, and returns an estimate of the color corresponding to the given
    * value, by applying a linear interpolation algorithm.
    *
    * @param points Pairs containing a value and its associated color
    * @param value The value to interpolate
    * @return The color that corresponds to `value`, according to the color scale defined by `points`
    */
  def interpolateColor(points: Iterable[(Double, Color)], value: Double): Color = {
    // given points are not sorted in a particular order.
    val sortedPoints = points.toList.sortWith(_._1 < _._1).toArray
    interpolateColorAlgo(sortedPoints, value)
  }

  /**
    * This method takes a sequence of reference temperature values and their associated color,
    * and a temperature value, and returns an estimate of the color corresponding to the given
    * value, by applying a linear interpolation algorithm.
    *
    * @param sortedPoints Temperatures with their associated colors
    * @param value Temperature value to estimate
    * @return Color of the estimated temperature
    */
  def interpolateColorAlgo(sortedPoints: Array[(Double, Color)], value: Double): Color = {
    for (i <- 0 until sortedPoints.length - 1) {
      (sortedPoints(i), sortedPoints(i + 1)) match {
        case ((v1, Color(r1, g1, b1)), (v2, Color(r2, g2, b2))) =>
          if (v1 > value) {
            return Color(r1, g1, b1)
          }
          else if (v2 > value) {
            val ratio = (value - v1) / (v2 - v1)
            return Color(
              math.round(r1 + (r2 - r1) * ratio).toInt,
              math.round(g1 + (g2 - g1) * ratio).toInt,
              math.round(b1 + (b2 - b1) * ratio).toInt
            )
          }
      }
    }
    // In case when value is not within the colour map return maximum color
    sortedPoints(sortedPoints.length-1)._2
  }

  /**
    * Visualize method to build an image (using the scrimage library)
    * where each pixel shows the temperature corresponding to its location.
    *
    * @param temperatures Known temperatures
    * @param colors Color scale
    * @return A 360×180 image where each pixel shows the predicted temperature at its location
    */
  def visualize(temperatures: Iterable[(Location, Temperature)], colors: Iterable[(Temperature, Color)]): Image = {
    val visualizer = new MapVisualizer(colors)
    visualizer.visualize(temperatures)
  }

}

