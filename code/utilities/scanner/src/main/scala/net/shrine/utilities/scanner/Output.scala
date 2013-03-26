package net.shrine.utilities.scanner

import net.shrine.utilities.scanner.commands.>>>
import net.shrine.utilities.scanner.commands.OutputCsv
import net.shrine.utilities.scanner.commands.CompoundCommand
import net.shrine.utilities.scanner.commands.ToCsvData
import net.shrine.utilities.scanner.commands.WriteToFile

/**
 * @author clint
 * @date Mar 21, 2013
 */
object Output {
  def to(fileName: String): ScanResults >>> Unit = {
    ToCsvData.andThen(OutputCsv).andThen(WriteToFile(fileName))
  }
}