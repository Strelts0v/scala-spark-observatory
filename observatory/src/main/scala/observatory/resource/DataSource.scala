package observatory.resource

import scala.io.Source

object DataSource {

  type Lookup = String => Source

  val resourceFileLookup: Lookup =
    (path: String) => Source.fromInputStream(getClass.getResourceAsStream(path))
}