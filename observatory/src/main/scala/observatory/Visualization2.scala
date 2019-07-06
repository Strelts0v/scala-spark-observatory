package observatory

import com.sksamuel.scrimage.{Image, Pixel}

import scala.math.pow

/**
  * 5th milestone: value-added information visualization
  */
object Visualization2 {

  /**
    * @param point (x, y) coordinates of a point in the grid cell
    * @param d00 Top-left value
    * @param d01 Bottom-left value
    * @param d10 Top-right value
    * @param d11 Bottom-right value
    * @return A guess of the value at (x, y) based on the four known values, using bilinear interpolation
    *         See https://en.wikipedia.org/wiki/Bilinear_interpolation#Unit_Square
    */
  def bilinearInterpolation(
    point: CellPoint,
    d00: Temperature,
    d01: Temperature,
    d10: Temperature,
    d11: Temperature
  ): Temperature = {

    (d00 * (1.0 - point.x) * (1.0 - point.y)) +
      (d10 * point.x * (1.0 - point.y)) +
      (d01 * (1.0 - point.x) * point.y) +
      (d11 * point.x * point.y)
  }

  /**
    * @param grid Grid to visualize
    * @param colors Color scale to use
    * @param tile Tile coordinates to visualize
    * @return The image of the tile at (x, y, zoom) showing the grid using the given color scale
    */
  def visualizeGrid(
    grid: GridLocation => Temperature,
    colors: Iterable[(Temperature, Color)],
    tile: Tile
  ): Image = {
    val alpha = 127
    val width = 256
    val height = 256
    val colorMap = colors.toList.sortWith(_._1 < _._1).toArray

    def colorToPixel(c: Color): Pixel = {
      Pixel.apply(c.red, c.green, c.blue, alpha)
    }

    // Tile offset of this tile in the zoom+8 coordinate system
    val x0 = pow(2.0, 8).toInt * tile.x
    val y0 = pow(2.0, 8).toInt * tile.y
    val buffer = new Array[Pixel](width * height)

    for (y <- 0 until height) {
      for (x <- 0 until width) {
        val location = Interaction.tileLocation(Tile(x0 + x, y0 + y, tile.zoom + 8))

        val lonFloor = location.lon.floor.toInt
        val lonCeil = location.lon.ceil.toInt
        val latFloor = location.lat.floor.toInt
        val latCeil = location.lon.ceil.toInt

        try {
          val d00 = grid(GridLocation(latFloor, lonFloor))
          val d01 = grid(GridLocation(latCeil, lonFloor))
          val d10 = grid(GridLocation(latFloor, lonCeil))
          val d11 = grid(GridLocation(latCeil, lonCeil))

          val xDelta = location.lon - lonFloor
          val yDelta = location.lat - latFloor

          val interpolation = bilinearInterpolation(CellPoint(xDelta, yDelta), d00, d01, d10, d11)
          buffer(y * width + x) = colorToPixel(Visualization.interpolateColor(colorMap, interpolation))
        } catch {
          case e: ArrayIndexOutOfBoundsException => {
            println(s"Index error location : $location, $x0 $x $y0 $y")
            throw e
          }
        }

      }
    }
    Image(width, height, buffer)
  }

}
