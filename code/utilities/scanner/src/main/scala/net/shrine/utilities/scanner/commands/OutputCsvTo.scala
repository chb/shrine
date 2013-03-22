package net.shrine.utilities.scanner.commands

import au.com.bytecode.opencsv.CSVWriter
import net.shrine.utilities.scanner.csv.CsvData
import java.io.FileWriter

/**
 * @author clint
 * @date Mar 21, 2013
 */
final case class OutputCsv(fileName: String) extends Command[CsvData, Unit] {
  override def apply(csvData: CsvData) {
    val csvWriter = new CSVWriter(new FileWriter(fileName))
    
    try {
      val rowsAsArraysOfFields = csvData.rows.map(_.toArray.map(String.valueOf))

      rowsAsArraysOfFields.foreach(csvWriter.writeNext)
    } finally {
      csvWriter.close()
    }
  }
}