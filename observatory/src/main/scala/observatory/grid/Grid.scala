package observatory.grid

import observatory.{GridLocation, Location}

class Grid extends Serializable {
  val width = 360
  val height = 180
  val buffer: Array[Double] = new Array[Double](width * height)

  def xyToLocation(x: Int, y: Int): Location = Location((height / 2) - y, x - (width / 2))

  def asFunction(): GridLocation => Double = {
    gridLocation => {
      val x = gridLocation.lon + 180
      val y = 90 - gridLocation.lat
      buffer(y * width + x)
    }
  }

  def asArray(): Array[Double] = buffer

  def add(grid: Grid): Grid = {
    val newGrid = new Grid()
    for (i <- 0 until width * height) { newGrid.buffer(i) = this.buffer(i) + grid.buffer(i)}
    newGrid
  }

  def diff(grid: Grid): Grid = {
    val newGrid = new Grid()
    for (i <- 0 until width * height) { newGrid.buffer(i) = this.buffer(i) - grid.buffer(i)}
    newGrid
  }

  def scale(factor: Double): Grid = {
    val newGrid = new Grid()
    for (i <- 0 until width * height) { newGrid.buffer(i) = this.buffer(i) * factor }
    newGrid
  }

  def map(f: Double => Double): Grid = {
    val newGrid = new Grid()
    for (i <- 0 until width * height) { newGrid.buffer(i) = f(this.buffer(i)) }
    newGrid
  }
}