package net.shrine.config

import java.io.File
import java.io.FileReader
import org.spin.tools.JAXBUtils.unmarshal

/**
 * @author clint
 * @date Mar 26, 2013
 */
final class FileSystemAdapterMappingsSource(mappingFile: File) extends AdapterMappingsSource {
  require(mappingFile != null)
  
  def this(mappingFileName: String) = this(new File(mappingFileName)) 

  //NB: Will blow up loudly if mapping file isn't found
  override def load: AdapterMappings = {
    require(mappingFile.exists, s"Couldn't find adapter mapping file '${ mappingFile.getCanonicalPath }'")
    
    AdapterMappings(unmarshal(mappingFile, classOf[JaxbableAdapterMappings]))
  }
}