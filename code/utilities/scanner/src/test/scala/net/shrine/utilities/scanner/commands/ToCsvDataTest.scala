package net.shrine.utilities.scanner.commands

import junit.framework.TestCase
import org.scalatest.junit.ShouldMatchersForJUnit
import org.junit.Test
import net.shrine.utilities.scanner.csv.CsvData
import net.shrine.utilities.scanner.csv.CsvRow
import net.shrine.utilities.scanner.Disposition
import net.shrine.utilities.scanner.ScanResults

/**
 * @author clint
 * @date Mar 25, 2013
 */
final class ToCsvDataTest extends TestCase with ShouldMatchersForJUnit {
  @Test
  def testApply {
    import Disposition.{ ShouldHaveBeenMapped, ShouldNotHaveBeenMapped, NeverFinished }

    val shouldHaveBeenMapped = Set("""\\SHRINE\SHRINE\Diagnoses\Diseases of the respiratory system\Respiratory infections\Pneumonia (except that caused by TB or STD)\Other bacterial pneumonia\Pneumonia due to Klebsiella pneumoniae\""")
    val shouldNOTHaveBeenMapped = Set("""\\SHRINE\SHRINE\Diagnoses\Diseases of the respiratory system\Respiratory infections\Pneumonia (except that caused by TB or STD)\Other bacterial pneumonia\Pneumonia due to Klebsiella pneumoniae\""", """\\SHRINE\SHRINE\Diagnoses\Diseases of the skin and subcutaneous tissue\Other inflammatory condition of skin\Unspecified erythematous condition\""")
    val neverFinished = Set("""\\SHRINE\SHRINE\Diagnoses\Diseases of the respiratory system\Respiratory failure, insufficiency, arrest (adult)\""")

    val scanResults = ScanResults(shouldHaveBeenMapped, shouldNOTHaveBeenMapped, neverFinished)

    val expected = CsvData(Seq(
      CsvRow(ShouldHaveBeenMapped, """\\SHRINE\SHRINE\Diagnoses\Diseases of the respiratory system\Respiratory infections\Pneumonia (except that caused by TB or STD)\Other bacterial pneumonia\Pneumonia due to other gram-negative bacteria\"""),
      CsvRow(ShouldNotHaveBeenMapped, """\\SHRINE\SHRINE\Diagnoses\Diseases of the respiratory system\Respiratory infections\Pneumonia (except that caused by TB or STD)\Other bacterial pneumonia\Pneumonia due to Klebsiella pneumoniae\"""),
      CsvRow(ShouldNotHaveBeenMapped, """\\SHRINE\SHRINE\Diagnoses\Diseases of the skin and subcutaneous tissue\Other inflammatory condition of skin\Unspecified erythematous condition\"""),
      CsvRow(NeverFinished, """\\SHRINE\SHRINE\Diagnoses\Diseases of the respiratory system\Respiratory failure, insufficiency, arrest (adult)\""")))

    val actual = ToCsvData(scanResults)

    actual.rows.collect { case CsvRow(disp, term) if disp == ShouldHaveBeenMapped => term }.toSet should equal(shouldHaveBeenMapped)
    actual.rows.collect { case CsvRow(disp, term) if disp == ShouldNotHaveBeenMapped => term }.toSet should equal(shouldNOTHaveBeenMapped)
    actual.rows.collect { case CsvRow(disp, term) if disp == NeverFinished => term }.toSet should equal(neverFinished)
  }
}