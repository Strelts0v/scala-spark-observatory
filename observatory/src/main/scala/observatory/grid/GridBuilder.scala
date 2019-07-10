package observatory.grid

import observatory.Location
import observatory.calculation.InterpolationCalculations._
import observatory.constant.CalculationConstants._

object GridBuilder {

  def fromIterable(temperatures: Iterable[(Location, Double)]): Grid = {
    val grid = new Grid()

    for (y <- 0 until grid.height) {
      for (x <- 0 until grid.width) {
        val loc = Location(y - (grid.height / 2), x - (grid.width / 2))
        grid.buffer(y * grid.width + x) = inverseDistanceWeighting(temperatures, loc, interpolationPower)
      }
    }

    grid
  }

}
