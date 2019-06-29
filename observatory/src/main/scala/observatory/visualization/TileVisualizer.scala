package observatory.visualization

import observatory.Interaction.tileLocation
import observatory.{Color, Location, Tile}

import scala.math.pow

class TileVisualizer(colors: Iterable[(Double, Color)], tile: Tile) extends Visualizer {
  val alpha = 127
  val width = 256
  val height = 256
  val colorMap: Array[(Double, Color)] = colors.toList.sortWith(_._1 < _._1).toArray

  // Tile offset is in the zoom+8 coordinate system
  val x0: Int = pow(2.0, 8).toInt * tile.x
  val y0: Int = pow(2.0, 8).toInt * tile.y

  def xyToLocation(x: Int, y: Int): Location = {
    tileLocation(Tile(tile.zoom + 8, x0 + x, y0 + y))
  }
}
