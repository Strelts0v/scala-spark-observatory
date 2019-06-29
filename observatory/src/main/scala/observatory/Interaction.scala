package observatory

import com.sksamuel.scrimage.Image
import observatory.visualization.TileVisualizer

import scala.math.{Pi, atan, pow, sinh}

import scala.concurrent.{Await, Future}
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global

/**
  * 3rd milestone: interactive visualization
  */
object Interaction {

  /**
    * @param tile Tile coordinates
    * @return The latitude and longitude of the top-left corner of the tile, as per http://wiki.openstreetmap.org/wiki/Slippy_map_tilenames
    */
  def tileLocation(tile: Tile): Location = {
    val n = pow(2.0, tile.zoom)
    val lon = (tile.x.toDouble / n) * 360.0 - 180.0
    val lat = atan(sinh(Pi * (1.0 - 2.0 * tile.y / n))).toDegrees

    Location(lat, lon)
  }

  /**
    * @param temperatures Known temperatures
    * @param colors Color scale
    * @param tile Tile coordinates
    * @return A 256×256 image showing the contents of the given tile
    */
  def tile(temperatures: Iterable[(Location, Temperature)], colors: Iterable[(Temperature, Color)], tile: Tile): Image = {
    val visualizer = new TileVisualizer(colors, tile)
    visualizer.visualize(temperatures)
  }

  /**
    * Generates all the tiles for zoom levels 0 to 3 (included), for all the given years.
    * @param yearlyData Sequence of (year, data), where `data` is some data associated with
    *                   `year`. The type of `data` can be anything.
    * @param generateImage Function that generates an image given a year, a zoom level, the x and
    *                      y coordinates of the tile and the data to build the image from
    */
  def generateTiles[Data](
    yearlyData: Iterable[(Year, Data)],
    generateImage: (Year, Tile, Data) => Unit
  ): Unit = {
    val tileTasks = for {
      (year, data) <- yearlyData
      zoom <- 0 until 4
      y <- 0 until pow(2.0, zoom).toInt
      x <- 0 until pow(2.0, zoom).toInt
    } yield Future { generateImage(year, Tile(x, y, zoom), data) }

    Await.result(Future.sequence(tileTasks), Duration.Inf)
    ()
  }

}
