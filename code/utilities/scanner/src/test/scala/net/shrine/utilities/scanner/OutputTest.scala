package net.shrine.utilities.scanner

import junit.framework.TestCase
import org.scalatest.junit.ShouldMatchersForJUnit
import org.junit.Test
import net.shrine.utilities.scanner.commands.ToCsvData
import net.shrine.utilities.scanner.commands.OutputCsv
import net.shrine.utilities.scanner.commands.CompoundCommand
import net.shrine.utilities.scanner.commands.WriteToFile
import net.shrine.utilities.scanner.csv.CsvData

/**
 * @author clint
 * @date Mar 25, 2013
 */
final class OutputTest extends TestCase with ShouldMatchersForJUnit {
  @Test
  def testTo {
    val fileName = "kslajdklasjd"
    
    val command = Output.to(fileName)
    
    val CompoundCommand(toString, writeToFile) = command
    
    val WriteToFile(actualFileName: String) = writeToFile.asInstanceOf[WriteToFile]
    
    actualFileName should equal(fileName)
    
    val CompoundCommand(toCsvData, outputCsv) = toString
    
    toCsvData should be(ToCsvData)
    
    outputCsv should be(OutputCsv)
  }
}