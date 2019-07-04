package observatory.resource

import scala.io.Source

class ResourceDataSource extends InputDataSource {

  def dataFileStream(path: String) = Source.fromInputStream(getClass.getResourceAsStream(path))

}
