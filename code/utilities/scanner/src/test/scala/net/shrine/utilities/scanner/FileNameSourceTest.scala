package net.shrine.utilities.scanner

import junit.framework.TestCase
import org.scalatest.junit.ShouldMatchersForJUnit
import org.junit.Test
import java.io.File
import org.spin.tools.RandomTool

/**
 * @author clint
 * @date Apr 29, 2013
 */
final class FileNameSourceTest extends TestCase with ShouldMatchersForJUnit {
  @Test
  def testExists {
    import FileNameSource.exists

    exists("salkfjalgfhalghlsdg.txt") should be(false)

    withFile { file =>
      exists(file.getName) should be(true)
    }
  }

  @Test
  def testNumberOf {
    import FileNameSource.numberOf

    numberOf("foo.csv") should be(None)
    numberOf("foo.1csv") should be(None)
    numberOf("foo1.csv") should be(None)

    numberOf("foo.1.csv") should be(Some(1))
    numberOf("foo.0.csv") should be(Some(0))
    numberOf("foo.99.csv") should be(Some(99))
    numberOf("foo.1000.csv") should be(Some(1000))
  }

  @Test
  def testNumberedFiles {
    import FileNameSource.numberedFiles

    numberedFiles should equal(Nil)

    val f0 = new File("foo.0.csv")
    val f1 = new File("foo.1.csv")
    val f99 = new File("foo.99.csv")

    val files = Seq(f0, f1, f99)

    files.foreach { f =>
      f.createNewFile()

      f.exists should be(true)
    }

    try {
      numberedFiles.map(_.getCanonicalPath).toSet should equal(files.map(_.getCanonicalPath).toSet)
    } finally {
      files.foreach { f =>
        f.delete()

        f.exists should be(false)
      }
    }
  }

  @Test
  def testNextOutputFileName {
    import FileNameSource.nextOutputFileName

    nextOutputFileName.startsWith(FileNameSource.base + "-") should be(true)

    def endsWithDotNumberDotCsv(fileName: String) = FileNameSource.endingNumberRegex.pattern.matcher(fileName).matches

    endsWithDotNumberDotCsv(nextOutputFileName) should be(false)

    {
      val file0 = new File(nextOutputFileName)

      withFile(file0) { _ =>
        val nextName = nextOutputFileName
        
        nextName.startsWith(FileNameSource.base + "-") should be(true)
        
        FileNameSource.numberOf(nextName) should be(Some(0))

        val file1 = new File(nextName)

        withFile(file1) { _ =>
          val nextNextName = nextOutputFileName
          
          nextNextName.startsWith(FileNameSource.base + "-") should be(true)
          
          FileNameSource.numberOf(nextNextName) should be(Some(1))
        }
      }
    }
  }

  private def withFile(file: File)(f: File => Any) {
    try {
      file.createNewFile()

      file.exists() should be(true)

      f(file)
    } finally {
      file.delete()

      file.exists should be(false)
    }
  }

  private def withFile(f: File => Any): Unit = withFile(new File(RandomTool.randomString))(f)
}