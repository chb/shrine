package net.shrine.config

import org.spin.tools.JAXBUtils.unmarshal
import java.io.Reader

/**
 * @author clint
 * @date Mar 26, 2013
 */
trait UnmarshallingAdapterMappingsSource extends AdapterMappingsSource {
  override def load: AdapterMappings = AdapterMappings(unmarshal(reader, classOf[JaxbableAdapterMappings]))
  
  protected def reader: Reader
}