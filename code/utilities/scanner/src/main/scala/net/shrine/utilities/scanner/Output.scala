package net.shrine.utilities.scanner

import net.shrine.utilities.scanner.commands.Command
import net.shrine.utilities.scanner.commands.OutputCsv
import net.shrine.utilities.scanner.commands.CompoundCommand
import net.shrine.utilities.scanner.commands.ToCsvData

/**
 * @author clint
 * @date Mar 21, 2013
 */
object Output {
  def to(fileName: String): Command[ScanResults, Unit] = {
    ToCsvData andThen OutputCsv(fileName)
  }
}