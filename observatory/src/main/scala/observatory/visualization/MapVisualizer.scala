package observatory.visualization

import observatory.{Color, Location}

/**
  * Implementation of Visualizer contract for global map
  * @param colors Sequence of colors user for visualization
  */
class MapVisualizer(colors: Iterable[(Double, Color)]) extends Visualizer {
  val alpha = 255
  val width = 360
  val height = 180
  val colorMap: Array[(Double, Color)] = colors.toList.sortWith(_._1 < _._1).toArray

  def xyToLocation(x: Int, y: Int): Location = Location(90 - y, x - 180)
}
