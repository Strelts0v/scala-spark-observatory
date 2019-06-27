package observatory.data

import observatory.{Color, Location}
import org.scalacheck.Gen

object testVisualizationData {

  val newYorkLocation = Location(40.730610, -73.935242)
  val parisLocation = Location(48.864716, 2.349014)
  val londonLocation = Location(51.508530, -0.076132)

  val locationGen: Gen[Location] = for {
    lat <- Gen.choose(-90.0, 90.0)
    lon <- Gen.choose(-180.0, 180.0)
  } yield Location(lat, lon)

  val colourGen: Gen[Color] = for {
    red <- Gen.choose(0, 255)
    green <- Gen.choose(0, 255)
    blue <- Gen.choose(0, 255)
  } yield Color(red, green, blue)

  val sampleGen: Gen[List[(Location, Double)]] = {
    val gen = for {
      loc <- locationGen
      value <- Gen.choose(-50.0, 50.0)
    } yield (loc, value)

    Gen.listOfN(10, gen)
  }

  val colourInterpolationSampleGen: Gen[(List[(Double, Color)], Double)] = for {
    value1 <- Gen.choose(-50.0, 50.0)
    colour1 <- colourGen
    value2 <- Gen.choose(-50.0, 50.0)
    colour2 <- colourGen
    value <- Gen.choose(value1 min value2, value1 max value2)
  } yield (List((value1, colour1), (value2, colour2)), value)
}
