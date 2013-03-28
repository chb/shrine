package net.shrine.utilities.scanner.components

import com.typesafe.config.ConfigFactory
import net.shrine.utilities.scanner.ScannerConfig

/**
 * @author clint
 * @date Mar 28, 2013
 */
trait HasClasspathAndCommandLineScannerConfig extends HasScannerConfig{ self: HasCommandLineConfig =>
  override lazy val config = {
    val fromConfigFiles = ConfigFactory.load
    
    val fromCommandLine = commandLineProps.toTypesafeConfig
      
    ScannerConfig(fromCommandLine.withFallback(fromConfigFiles))
  }
}