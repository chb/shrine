package net.shrine.utilities.scanner.components

import net.shrine.utilities.scanner.Scanner
import net.shrine.config.FileSystemAdapterMappingsSource

/**
 * @author clint
 * @date Mar 28, 2013
 */
trait HasFileSystemAdapterMappingsSource { self: HasScannerConfig with Scanner =>
  override lazy val adapterMappingsSource = new FileSystemAdapterMappingsSource(config.adapterMappingsFile)
}