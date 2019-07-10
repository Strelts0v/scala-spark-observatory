package observatory.resource

import scala.io.{BufferedSource, Source}

class ResourceDataSource extends InputDataSource {

  def dataFileStream(path: String): BufferedSource = Source.fromInputStream(getClass.getResourceAsStream(path))

}
