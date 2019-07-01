package observatory.resource

import observatory.InputDataSource

import scala.io.Source

class ResourceDataSource extends InputDataSource {

  def dataFileStream(path: String) = Source.fromInputStream(getClass.getResourceAsStream(path))

}
