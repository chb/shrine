package net.shrine.config

import org.spin.tools.JAXBUtils.unmarshal
import java.io.InputStreamReader

/**
 * @author clint
 * @date Mar 6, 2012
 */
final class ClasspathAdapterMappingsSource(mappingFileName: String) extends AdapterMappingsSource {
  require(mappingFileName != null)
  
  //NB: Will blow up loudly if mapping file isn't found
  override def load: AdapterMappings = {
    val mappingStream = getClass.getClassLoader.getResourceAsStream(mappingFileName)
    
    require(mappingStream != null, "Couldn't find adapter mapping file '" + mappingFileName + "' on the classpath")
    
    unmarshal(new InputStreamReader(mappingStream), classOf[AdapterMappings])
  }
}