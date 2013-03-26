package net.shrine.utilities.scanner.commands

import au.com.bytecode.opencsv.CSVWriter
import net.shrine.utilities.scanner.csv.CsvData
import java.io.FileWriter
import java.io.StringWriter

/**
 * @author clint
 * @date Mar 21, 2013
 */
case object OutputCsv extends (CsvData >>> String) {
  override def apply(csvData: CsvData): String = {
    val stringWriter = new StringWriter
    
    val csvWriter = new CSVWriter(stringWriter)
    
    try {
      val rowsAsArraysOfFields = csvData.rows.map(_.toArray.map(String.valueOf))

      rowsAsArraysOfFields.foreach(csvWriter.writeNext)
    } finally {
      csvWriter.close()
    }
    
    stringWriter.toString
  }
}