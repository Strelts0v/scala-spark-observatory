package observatory

import java.io.File

import com.sksamuel.scrimage.Image
import observatory.grid.{Grid, GridBuilder}
import org.apache.spark.rdd.RDD

import scala.math.pow

/**
  * object for computation of temperature evolution
  */
object Manipulation {

  /**
    * @param temperatures Known temperatures
    * @return A function that, given a latitude in [-89, 90] and a longitude in [-180, 179],
    *         returns the predicted temperature at this location
    */
  def makeGrid(temperatures: Iterable[(Location, Temperature)]): GridLocation => Temperature = {
    val grid: Grid = GridBuilder.fromIterable(temperatures)
    grid.asFunction()
  }

  /**
    * This method takes a sequence of temperature data over several years (each “temperature data”
    * for one year being a sequence of pairs of average yearly temperature and location),
    * and returns a grid containing the average temperature over the given years at each location.
    *
    * @param temperatures Sequence of known temperatures over the years (each element of the collection
    *                      is a collection of pairs of location and temperature)
    * @return A function that, given a latitude and a longitude, returns the average temperature at this location
    */
  def average(temperatures: Iterable[Iterable[(Location, Temperature)]]): GridLocation => Temperature = {

    // Generate a grid for each year
    val gridPairs: Iterable[(Grid, Int)] = for {
      temps <- temperatures
    } yield (GridBuilder.fromIterable(temps), 1)

    val reduced = gridPairs.reduce(mergeArrayPairs)

    val meanGrid: Grid = reduced match {
      case (grid, count) => grid.map(_ / count)
    }

    meanGrid.asFunction()
  }

  /**
    * This method takes temperature data and a grid containing normal temperatures,
    * and returns a grid containing temperature deviations from the normals.
    *
    * @param temperatures Known temperatures
    * @param normals A grid containing the “normal” temperatures
    * @return A grid containing the deviations compared to the normal temperatures
    */
  def deviation(temperatures: Iterable[(Location, Temperature)], normals: GridLocation => Temperature): GridLocation => Temperature = {
    val grid = makeGrid(temperatures)
    gridLocation => {
      grid(gridLocation) - normals(gridLocation)
    }
  }

  def mergeArrayPairs(p1: (Grid, Int), p2: (Grid, Int)): (Grid, Int) = {
    (p1, p2) match {
      case ((g1, c1), (g2, c2)) => {
        (g1.add(g2), c1 + c2)
      }
    }
  }

  /**
    * Spark implementation of the averaging function
    */
  def averageGridRdd(temperatures: RDD[Grid]): Grid = {
    val reduced: (Grid, Int) = temperatures.map((_, 1)).reduce(
      (p1: (Grid, Int), p2: (Grid, Int)) => mergeArrayPairs(p1, p2)
    )

    reduced match {
      case (grid, count) => {
        grid.scale(1.0 / count)
      }
    }
  }

  /**
    * Create a set of tiles using Spark from an RDD of grids
    */
  def makeTiles(gridRDD: RDD[(Int, Grid)], colorScale: List[(Double, Color)], pathPrefix: String): Unit = {
    val tileParams = gridRDD.flatMap({
      case (year: Int, grid: Grid) => for {
        zoom <- 0 until 4
        y <- 0 until pow(2.0, zoom).toInt
        x <- 0 until pow(2.0, zoom).toInt
      } yield (year, zoom, x, y, grid)
    })

    tileParams.foreach({
      case (year: Int, zoom: Int, x: Int, y: Int, grid: Grid) => {
        val tileDir = new File(s"${pathPrefix}/${year}/$zoom")
        tileDir.mkdirs()
        val tileFile = new File(tileDir, s"$x-$y.png")

        if (tileFile.exists()) {
          println(s"$pathPrefix: Tile for $year $zoom:$x:$y already exists")
        }
        else {
          println(s"$pathPrefix: Generating tile for $year $zoom:$x:$y")
          val tile: Image = Visualization2.visualizeGrid(
            grid.asFunction(),
            colorScale,
            Tile(x, y, zoom)
          )
          println(s"$pathPrefix: Done tile $zoom:$x:$y for $year")
          tile.output(tileFile)
        }

        ()
      }
    })
  }

}