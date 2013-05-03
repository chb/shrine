package net.shrine.utilities.scanner

import junit.framework.TestCase
import org.scalatest.junit.ShouldMatchersForJUnit
import org.junit.Test
import com.typesafe.config.ConfigFactory
import com.typesafe.config.Config
import com.typesafe.config.ConfigException

/**
 * @author clint
 * @date Mar 8, 2013
 */
final class ScannerConfigTest extends TestCase with ShouldMatchersForJUnit {
  import scala.concurrent.duration._

  @Test
  def testFromConfig {
    val fromClassPath = ConfigFactory.load

    {
      val scannerConfig = ScannerConfig(fromClassPath)

      scannerConfig should not be (null)
      scannerConfig.adapterMappingsFile should be("testAdapterMappings.xml")
      scannerConfig.ontologySqlFile should be("testShrineWithSyns.sql")
      scannerConfig.reScanTimeout should be(99.seconds)
      scannerConfig.shrineUrl should be("https://example.com")
      scannerConfig.projectId should be("SHRINE-PROJECT")
      scannerConfig.authorization should not be (null)
      scannerConfig.authorization.domain should be("TestDomain")
      scannerConfig.authorization.username should be("testuser")
      scannerConfig.authorization.credential.value should be("testpassword")
      scannerConfig.authorization.credential.isToken should be(false)
      scannerConfig.outputFile should equal("foo.csv")
    }

    {
      val scannerConfig = ScannerConfig(fromClassPath.withoutPath(ScannerConfig.Keys.outputFile))

      scannerConfig.outputFile should equal(FileNameSource.nextOutputFileName)
    }
  }

  @Test
  def testGetReScanTimeout {
    intercept[ConfigException.Missing] {
      ScannerConfig.getReScanTimeout(ConfigFactory.empty())
    }

    import scala.collection.JavaConverters._

    import ScannerConfig.Keys.{ milliseconds, seconds, minutes }

    ScannerConfig.getReScanTimeout(ConfigFactory.parseMap(Map(milliseconds -> "123").asJava)) should be(123.milliseconds)

    ScannerConfig.getReScanTimeout(ConfigFactory.parseMap(Map(seconds -> "123").asJava)) should be(123.seconds)

    ScannerConfig.getReScanTimeout(ConfigFactory.parseMap(Map(minutes -> "123").asJava)) should be(123.minutes)

    //Priority should be millis > seconds > minutes

    ScannerConfig.getReScanTimeout(ConfigFactory.parseMap(Map(milliseconds -> "123", seconds -> "456", minutes -> "789").asJava)) should be(123.milliseconds)

    ScannerConfig.getReScanTimeout(ConfigFactory.parseMap(Map(seconds -> "456", minutes -> "789").asJava)) should be(456.seconds)
  }
}