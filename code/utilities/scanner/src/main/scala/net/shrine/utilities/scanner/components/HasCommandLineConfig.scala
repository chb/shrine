package net.shrine.utilities.scanner.components

import net.shrine.utilities.scanner.CommandLineScannerConfig

/**
 * @author clint
 * @date Mar 28, 2013
 */
trait HasCommandLineConfig { self: HasArgs =>
  val commandLineProps = {
    val argsToUse = if(args.isEmpty) Seq("--help") else args
    
    CommandLineScannerConfig(argsToUse)
  }
  
  def showVersionToggleEnabled = commandLineProps.showVersionToggle.isSupplied
  
  def showHelpToggleEnabled = commandLineProps.showHelpToggle.isSupplied
}