package observatory.calculation

import observatory.constant.CalculationConstants._
import observatory.Location
import org.junit.runner.RunWith
import org.scalatest.FunSuite
import org.scalatest.junit.JUnitRunner
import org.scalatest.prop.GeneratorDrivenPropertyChecks
import org.scalacheck.Prop._

import observatory.data.testVisualizationData._


@RunWith(classOf[JUnitRunner])
class InterpolationCalculationsTest extends FunSuite with GeneratorDrivenPropertyChecks {

  test("Check commutativity law with London and Paris locations") {
    assert(
      InterpolationCalculations.greatCircleDistance(londonLocation, parisLocation) ===
      InterpolationCalculations.greatCircleDistance(parisLocation, londonLocation)
    )
  }

  test("Check commutativity law with random generated locations") {
    forAll (locationGen, locationGen) { (l1: Location, l2: Location) =>
      assert(InterpolationCalculations.greatCircleDistance(l1, l2) === InterpolationCalculations.greatCircleDistance(l2, l1))
    }
  }

  test("Check calculation of Great Circle Distance for London and New York locations") {
    assert(InterpolationCalculations.greatCircleDistance(londonLocation, newYorkLocation) - 5585.0 < 0.2)
  }

  test("Check calculation of Great Circle Distance for London and Paris locations") {
    assert(InterpolationCalculations.greatCircleDistance(londonLocation, parisLocation) - 344.0 < 0.2)
  }

  test("Check symmetric work of inverseDistanceWeighting algorithm") {
    val sample: List[(Location, Double)] =
      List(
        (Location(30, 0), 5.0),
        (Location(0, 30), 5.0),
        (Location(-30, 0), 5.0),
        (Location(0, -30), 5.0)
      )
    val loc = Location(0, 0)

    assert(InterpolationCalculations.inverseDistanceWeighting(sample, loc, inverseDistanceWeightingPower) === 5.0)
  }

  test("Check work of inverseDistanceWeighting algorithm with particular points") {
    val sample: List[(Location, Double)] =
      List(
        (Location(30, 0), 10.0),
        (Location(0, 30), 5.0),
        (Location(-30, 0), 15.0),
        (Location(0, -30), 20.0)
      )
    val loc = Location(-30, 0)

    assert(InterpolationCalculations.inverseDistanceWeighting(sample, loc, inverseDistanceWeightingPower) === 15.0)
  }

  test("Check work of inverseDistanceWeighting algorithm with random samples") {
    check(forAll (sampleGen, locationGen) { (sample: List[(Location, Double)], loc: Location) => {
      val result = InterpolationCalculations.inverseDistanceWeighting(sample, loc, inverseDistanceWeightingPower)
      (result <= 50.0) && (result >= -50.0)
    }})
  }

}
