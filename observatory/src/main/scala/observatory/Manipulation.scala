package observatory

import observatory.grid.Grid
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
    val grid = new Grid(360, 180, temperatures)
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
    } yield (new Grid(360, 180, temps), 1)

    val reduced = gridPairs.reduce(mergeArrayPairs)

    val meanGrid: Grid = reduced match {
      case (grid, count) => new Grid(360, 180, grid.asArray().map(_ / count))
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
        val a1 = g1.asArray()
        val a2 = g2.asArray()
        val a3 = new Array[Double](a1.length)
        for (i <- 0 until a1.length) {
          a3(i) = a1(i) + a2(i)
        }
        (new Grid(360, 180, a3), c1 + c2)
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

    val meanArray: Array[Double] = reduced match {
      case (grid, count) => grid.asArray().map(_ / count)
    }

    new Grid(360, 180, meanArray)
  }

}