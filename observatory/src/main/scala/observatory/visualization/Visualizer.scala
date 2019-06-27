package observatory.visualization

import com.sksamuel.scrimage.{Image, Pixel}
import observatory.{Color, Location, Visualization}
import observatory.calculation.InterpolationCalculations._
import observatory.constant.CalculationConstants._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

trait Visualizer {
  val alpha: Int
  val width: Int
  val height: Int
  val colorMap: Array[(Double, Color)]

  def colorToPixel(c: Color): Pixel = {
    Pixel.apply(c.red, c.green, c.blue, alpha)
  }

  def xyToLocation(x: Int, y: Int): Location

  def visualize(temperatures: Iterable[(Location, Double)]): Future[Image] = {
    val tasks = for {y <- 0 until height} yield Future {
      val rowBuffer = new Array[Pixel](width)
      for (x <- 0 until width) {
        val temp = inverseDistanceWeighting(temperatures, xyToLocation(x, y), inverseDistanceWeightingPower)
        rowBuffer(x) = colorToPixel(Visualization.interpolateColor(colorMap, temp))
      }
      rowBuffer
    }

    Future.sequence(tasks).map(seq => Image(width, height, seq.reduce(_ ++ _)))
  }
}
