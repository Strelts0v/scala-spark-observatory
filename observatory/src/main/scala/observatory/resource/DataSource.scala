package observatory.resource

import scala.io.Source

/**
  * Object for resource extraction
  */
object DataSource {

  type Lookup = String => Source

  val resourceFileLookup: Lookup =
    (path: String) => Source.fromInputStream(getClass.getResourceAsStream(path))
}