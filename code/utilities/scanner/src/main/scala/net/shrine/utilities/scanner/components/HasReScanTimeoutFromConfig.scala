package net.shrine.utilities.scanner.components

import net.shrine.utilities.scanner.Scanner

/**
 * @author clint
 * @date Mar 28, 2013
 */
trait HasReScanTimeoutFromConfig { self: Scanner with HasScannerConfig =>
  override lazy val reScanTimeout = config.reScanTimeout
}