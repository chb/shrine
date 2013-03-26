package net.shrine.config

import org.spin.tools.JAXBUtils.unmarshal
import java.io.InputStreamReader
import java.io.Reader

/**
 * @author clint
 * @date Mar 6, 2012
 */
final class ClasspathAdapterMappingsSource(mappingFileName: String) extends UnmarshallingAdapterMappingsSource {
  require(mappingFileName != null)
  
  //NB: Will blow up loudly if mapping file isn't found
  override protected def reader: Reader = {
    val mappingStream = getClass.getClassLoader.getResourceAsStream(mappingFileName)
    
    require(mappingStream != null, "Couldn't find adapter mapping file '" + mappingFileName + "' on the classpath")
    
    new InputStreamReader(mappingStream)
  }
}