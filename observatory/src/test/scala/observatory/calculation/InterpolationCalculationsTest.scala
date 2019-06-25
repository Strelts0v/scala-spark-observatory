package observatory.calculation

import observatory.Location

import org.junit.runner.RunWith
import org.scalatest.FunSuite
import org.scalatest.junit.JUnitRunner
import org.scalatest.prop.GeneratorDrivenPropertyChecks

import observatory.data.testLocations._

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

}
