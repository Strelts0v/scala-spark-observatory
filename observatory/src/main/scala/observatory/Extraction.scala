package observatory

import observatory.resource.{DataExtractor, DataSource}


/**
  * 1st milestone: data extraction
  */
object Extraction extends DataExtractor(DataSource.resourceFileLookup) {

}
