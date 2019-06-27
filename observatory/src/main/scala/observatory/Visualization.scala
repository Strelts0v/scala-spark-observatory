package observatory

import com.sksamuel.scrimage.{Image, Pixel}
import observatory.calculation.InterpolationCalculations._
import observatory.constant.CalculationConstants._
import observatory.visualization.BasicVisualizer

import scala.concurrent.Await
import scala.concurrent.duration._

/**
  * 2nd milestone: basic visualization
  */
object Visualization {

  /**
    * @param temperatures Known temperatures: pairs containing a location and the temperature at this location
    * @param location Location where to predict the temperature
    * @return The predicted temperature at `location`
    */
  def predictTemperature(temperatures: Iterable[(Location, Temperature)], location: Location): Temperature = {
    inverseDistanceWeighting(temperatures, location, inverseDistanceWeightingPower)
  }

  /**
    * @param points Pairs containing a value and its associated color
    * @param value The value to interpolate
    * @return The color that corresponds to `value`, according to the color scale defined by `points`
    */
  def interpolateColor(points: Iterable[(Double, Color)], value: Double): Color = {
    val sortedPoints = points.toList.sortWith(_._1 < _._1).toArray
    interpolateColorAlgo(sortedPoints, value)
  }

  def interpolateColorAlgo(sortedPoints: Array[(Double, Color)], value: Double): Color = {
    for (i <- 0 until sortedPoints.length - 1) {
      (sortedPoints(i), sortedPoints(i + 1)) match {
        case ((v1, Color(r1, g1, b1)), (v2, Color(r2, g2, b2))) => {
          if (v1 > value) v1
          else if (v2 > value) {
            val ratio = (value - v1) / (v2 - v1)
            return Color(
              (r1 + (r2 - r1) * ratio).toInt,
              (g1 + (g2 - g1) * ratio).toInt,
              (b1 + (b2 - b1) * ratio).toInt
            )
          }
        }
      }
    }
    // In case when value is not within the colour map. Return maximum color
    sortedPoints(sortedPoints.length-1)._2
  }

  /**
    * @param temperatures Known temperatures
    * @param colors Color scale
    * @return A 360×180 image where each pixel shows the predicted temperature at its location
    */
  def visualize(temperatures: Iterable[(Location, Temperature)], colors: Iterable[(Temperature, Color)]): Image = {
    val visualizer = new BasicVisualizer(colors)
    Await.result(visualizer.visualize(temperatures), 20.minutes)
  }

}

