package observatory

import observatory.grid.Grid
import org.apache.spark.rdd.RDD

import org.apache.spark.SparkContext._

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
    val grid = new Grid(360, 180, temperatures)
    grid.asFunction()
  }

  /**
    * @param temperaturess Sequence of known temperatures over the years (each element of the collection
    *                      is a collection of pairs of location and temperature)
    * @return A function that, given a latitude and a longitude, returns the average temperature at this location
    */
  def average(temperaturess: Iterable[Iterable[(Location, Temperature)]]): GridLocation => Temperature = {
    // Generate a grid for each year
    val gridPairs: Iterable[(Array[Double], Int)] = for {
      temps <- temperaturess
    } yield (new Grid(360, 180, temps).asArray(), 1)

    val reduced = gridPairs.reduce(mergeArrayPairs)

    val meanGrid = reduced match {
      case (grid, count) => grid.map(_ / count)
    }

    new Grid(360, 180, meanGrid).asFunction()
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

  def mergeArrayPairs(p1: (Array[Double], Int), p2: (Array[Double], Int)) = {
    (p1, p2) match {
      case ((g1, c1), (g2, c2)) => {
        val g3 = new Array[Double](g1.length)
        for (i <- 0 until g1.length) {
          g3(i) = g1(i) + g2(i)
        }
        (g3, c1 + c2)
      }
    }
  }

  /**
    * Spark implementation of the averaging function
    */
//  def averageGridRDD(temperatures: RDD[Grid]): Grid = {
//    val reduced: (Array[Double], Int) = temperatures.reduce(
//      (g1: Grid, g2: Grid) => mergeArrayPairs((g1.asArray(), 1), (g2.asArray(), 1))
//    )
//    val meanArray = reduced match {
//      case (grid, count) => grid.map(_ / count)
//    }
//    new Grid(360, 180, meanArray)
//  }

}

