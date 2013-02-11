package net.shrine.spring

import org.spin.tools.config.EndpointConfig
import org.spin.tools.config.EndpointType

/**
 * @author clint
 * @date Feb 11, 2013
 * 
 * Simple function class to help Spring map() over an Option bean 
 */
final class EndpointConfigMaker(endpointType: EndpointType) extends (String => EndpointConfig) {
  override def apply(url: String): EndpointConfig = new EndpointConfig(endpointType, url)
}