package observatory.visualization

import com.sksamuel.scrimage.{Image, Pixel}
import observatory.{Color, Location, Visualization}
import observatory.calculation.InterpolationCalculations._
import observatory.constant.CalculationConstants._

/**
  * Defines contract for visualizing strategy
  */
trait Visualizer {
  val alpha: Int
  val width: Int
  val height: Int
  val colorMap: Array[(Double, Color)]

  /**
    * Converts custom Color object into Pixel scrimage library object
    */
  def colorToPixel(c: Color): Pixel = {
    Pixel.apply(c.red, c.green, c.blue, alpha)
  }

  /**
    * Converts x and y to concrete Location object
    */
  def xyToLocation(x: Int, y: Int): Location

  /**
    * Visualize method to build an image (using the scrimage library)
    * where each pixel shows the temperature corresponding to its location.
    * Implemented based on inverseDistanceWeighting method
    *
    * @param temperatures Temperatures for building an image
    * @return Image object of scrimage library
    */
  def visualize(temperatures: Iterable[(Location, Double)]): Image = {
    val buffer = new Array[Pixel](width * height)
    for (y <- 0 until height) {
      for (x <- 0 until width) {
        val temp = inverseDistanceWeighting(temperatures, xyToLocation(x, y), interpolationPower)
        buffer(y * width + x) = colorToPixel(Visualization.interpolateColor(colorMap, temp))
      }
    }
    Image(width, height, buffer)
  }
}
