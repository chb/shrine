package net.shrine.utilities.scanner

import net.shrine.util.Versions
import net.shrine.utilities.scanner.components.HasArgs
import net.shrine.utilities.scanner.components.HasClasspathAndCommandLineScannerConfig
import net.shrine.utilities.scanner.components.HasCommandLineConfig
import net.shrine.utilities.scanner.components.HasFileSystemAdapterMappingsSource
import net.shrine.utilities.scanner.components.HasFileSystemShrineSqlOntologyDao
import net.shrine.utilities.scanner.components.HasReScanTimeoutFromConfig
import net.shrine.utilities.scanner.components.HasSpinBroadcastServiceScannerClient

/**
 * @author clint
 * @date Mar 6, 2013
 */
final class ScannerModule(val args: Seq[String]) extends Scanner with HasArgs with HasCommandLineConfig with HasClasspathAndCommandLineScannerConfig with HasReScanTimeoutFromConfig with HasFileSystemAdapterMappingsSource with HasFileSystemShrineSqlOntologyDao with HasSpinBroadcastServiceScannerClient

object ScannerModule {
  def printVersionInfo() {
    println(s"Shrine Scanner version: ${Versions.version}")
    println(s"Built on ${Versions.buildDate}")
    println(s"SCM branch: ${Versions.scmBranch}")
    println(s"SCM revision: ${Versions.scmRevision}")
    println()
  }

  def main(args: Array[String]) {

    val scanner = new ScannerModule(args)

    if (scanner.showVersionToggleEnabled) {
      printVersionInfo()
    }

    if (scanner.showHelpToggleEnabled) {
      println("Usage: scanner [options]")

      scanner.commandLineProps.printHelp()

      System.exit(0)
    }

    val scanResults = scanner.scan()

    val command = Output.to(FileNameSource.nextOutputFileName)

    command(scanResults)
  }
}