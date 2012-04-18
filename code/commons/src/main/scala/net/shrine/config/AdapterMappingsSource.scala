package net.shrine.config

/**
 * @author clint
 * @date Mar 6, 2012
 */
trait AdapterMappingsSource {
  def load: AdapterMappings
}