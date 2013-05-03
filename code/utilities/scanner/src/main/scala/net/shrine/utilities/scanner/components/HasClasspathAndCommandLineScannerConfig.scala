package net.shrine.utilities.scanner.components

import com.typesafe.config.ConfigFactory
import net.shrine.utilities.scanner.ScannerConfig
import com.typesafe.config.Config

/**
 * @author clint
 * @date Mar 28, 2013
 */
trait HasClasspathAndCommandLineScannerConfig extends HasScannerConfig { self: HasCommandLineConfig =>
  override lazy val config = {
    val fromConfigFiles = ConfigFactory.load
    
    val fromCommandLine = commandLineProps.toTypesafeConfig
    
    def hasDuration(config: Config) = config.hasPath(ScannerConfig.Keys.reScanTimeout) && !config.getConfig(ScannerConfig.Keys.reScanTimeout).isEmpty
      
    //
    //If both configs have rescan timeout info, remove the one from the classpath to avoid clashes, whcih can occur when merging things like
    // scanner.reScanTimeout.seconds = 42
    //and 
    //scanner.reScanTimeout.minutes = 42
    //which produces
    //scanner.reScanTimeout {
    //  seconds = 42
    //  minutes = 42
    //}
    val mungedFromConfigFiles = {
      if(hasDuration(fromCommandLine) && hasDuration(fromConfigFiles)) {
	    fromConfigFiles.withoutPath(ScannerConfig.Keys.reScanTimeout)
	  } else {
	    fromConfigFiles
	  }
    }
    
    ScannerConfig(fromCommandLine.withFallback(mungedFromConfigFiles))
  }
}