package observatory

import org.scalacheck.Prop.{all, forAll}
import org.scalatest.FunSuite
import org.scalatest.prop.Checkers
import observatory.data.testVisualizationData._

class SimpleVisualizationSuite extends FunSuite with Checkers {

  test("Check colour within bounds") {
    check(forAll(colourInterpolationSampleGen) {
      case (bounds: List[(Double, Color)], value: Double) => {
        val result = Visualization.interpolateColor(bounds.toIterable, value)
        all(
          result.red >= bounds.map(_._2.red).min,
          result.red <= bounds.map(_._2.red).max,
          result.green >= bounds.map(_._2.green).min,
          result.green <= bounds.map(_._2.green).max,
          result.blue >= bounds.map(_._2.blue).min,
          result.blue <= bounds.map(_._2.blue).max
        )
      }
    })
  }

}
