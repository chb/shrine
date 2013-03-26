package net.shrine.utilities.scanner.commands

import java.io.FileWriter

/**
 * @author clint
 * @date Mar 25, 2013
 */
final case class WriteToFile(fileName: String) extends (String >>> Unit) {
  override def apply(dataToBeWritten: String) {
    val writer = new FileWriter(fileName)
    
    try {
      writer.write(dataToBeWritten)
    } finally {
      writer.close()
    }
  }
}