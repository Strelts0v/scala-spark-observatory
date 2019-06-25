package observatory.data

import observatory.Location
import org.scalacheck.Gen

object testLocations {

  val newYorkLocation = Location(40.730610, -73.935242)
  val parisLocation = Location(48.864716, 2.349014)
  val londonLocation = Location(51.508530, -0.076132)

  val locationGen = for {
    lat <- Gen.choose(-90.0, 90.0)
    lon <- Gen.choose(-180.0, 180.0)
  } yield Location(lat, lon)

}
