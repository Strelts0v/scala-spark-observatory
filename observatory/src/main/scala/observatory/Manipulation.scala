package observatory

import observatory.grid.{Grid, GridBuilder}
import org.apache.spark.rdd.RDD

/**
  * 4th milestone: value-added information
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
    * @param temperatures Sequence of known temperatures over the years (each element of the collection
    *                      is a collection of pairs of location and temperature)
    * @return A function that, given a latitude and a longitude, returns the average temperature at this location
    */
  def average(temperatures: Iterable[Iterable[(Location, Temperature)]]): GridLocation => Temperature = {

    // TODO : Average over all years
    // TODO : Parallelism with reduce makes sense here or maybe Spark

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
  def averageGridRDD(temperatures: RDD[Grid]): Grid = {
    val reduced: (Grid, Int) = temperatures.map((_, 1)).reduce(
      (p1: (Grid, Int), p2: (Grid, Int)) => mergeArrayPairs(p1, p2)
    )

    reduced match {
      case (grid, count) => {
        grid.scale(1.0 / count)
      }
    }
  }

}