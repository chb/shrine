package net.shrine.config

import java.io.File
import java.io.FileReader
import java.io.Reader

/**
 * @author clint
 * @date Mar 26, 2013
 */
final class FileSystemAdapterMappingsSource(mappingFile: File) extends UnmarshallingAdapterMappingsSource {
  require(mappingFile != null)
  
  def this(mappingFileName: String) = this(new File(mappingFileName)) 

  //NB: Will blow up loudly if mapping file isn't found
  override protected def reader: Reader = {
    require(mappingFile.exists, s"Couldn't find adapter mapping file '${ mappingFile.getCanonicalPath }'")
    
    new FileReader(mappingFile)
  }
}