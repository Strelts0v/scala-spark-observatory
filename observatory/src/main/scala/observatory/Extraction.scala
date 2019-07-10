package observatory

import observatory.resource.{DataExtractor, DataSource}

/**
  * Object for extraction of temperatures and stations data
  */
object Extraction extends DataExtractor(DataSource.resourceFileLookup) {
}
