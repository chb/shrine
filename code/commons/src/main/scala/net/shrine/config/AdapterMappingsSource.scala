package net.shrine.config

import java.io.Reader
import org.spin.tools.JAXBUtils.unmarshal


/**
 * @author clint
 * @date Mar 6, 2012
 */
trait AdapterMappingsSource {
  def load: AdapterMappings 
}