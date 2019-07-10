package observatory.visualization

import observatory.{Color, Interaction, Location, Tile}

import scala.math.pow

/**
  * Implementation of Visualizer contract for map tile
  * @param colors Sequence of colors user for visualization
  */
class TileVisualizer(colors: Iterable[(Double, Color)], tile: Tile) extends Visualizer {
  val alpha = 127
  val width = 256
  val height = 256
  val colorMap: Array[(Double, Color)] = colors.toList.sortWith(_._1 < _._1).toArray

  // Tile offset is in the zoom+8 coordinate system
  val x0: Int = pow(2.0, 8).toInt * tile.x
  val y0: Int = pow(2.0, 8).toInt * tile.y

  def xyToLocation(x: Int, y: Int): Location = {
    Interaction.tileLocation(Tile(x0 + x, y0 + y, tile.zoom + 8))
  }
}
