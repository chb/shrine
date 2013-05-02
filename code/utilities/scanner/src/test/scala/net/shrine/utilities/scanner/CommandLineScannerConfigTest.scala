package net.shrine.utilities.scanner

import junit.framework.TestCase
import org.scalatest.junit.ShouldMatchersForJUnit
import org.junit.Test
import scala.concurrent.duration._
import net.shrine.protocol.AuthenticationInfo
import net.shrine.protocol.Credential
import ScannerConfig.Keys

/**
 * @author clint
 * @date Mar 26, 2013
 */
final class CommandLineScannerConfigTest extends TestCase with ShouldMatchersForJUnit {
  val url = "http://example.com"

  val authn = AuthenticationInfo("some-domain", "some-user", Credential("some-password", false))

  val projectId = "FOO"

  @Test
  def testParseRescanTimeout {
    def doTimeoutTest(short: Boolean, howMany: Int, timeUnit: String, toDuration: Int => Duration) {
      val args = Seq(if (short) "-t" else "--rescan-timeout", howMany.toString, timeUnit, "-c", authn.domain, authn.username, authn.credential.value, "-u", url)

      val conf = new CommandLineScannerConfig(args)

      conf.rescanTimeout() should equal(toDuration(howMany))

      conf.url() should equal(url)

      conf.credentials() should equal(authn)
    }

    doTimeoutTest(true, 42, "minutes", _.minutes)

    doTimeoutTest(false, 42, "minutes", _.minutes)

    doTimeoutTest(true, 17, "seconds", _.seconds)

    doTimeoutTest(false, 17, "seconds", _.seconds)

    doTimeoutTest(true, 123, "milliseconds", _.milliseconds)

    doTimeoutTest(false, 123, "milliseconds", _.milliseconds)
  }

  @Test
  def testParseAuthn {
    {
      val args = Seq("-c", authn.domain, authn.username, authn.credential.value, "-u", url, "-t", "123", "minutes")

      val conf = new CommandLineScannerConfig(args)

      conf.credentials() should equal(authn)
    }

    {
      val argsWithNoCredentials = Seq("-u", url, "-t", "123", "minutes")

      val confWithNoCredentials = new CommandLineScannerConfig(argsWithNoCredentials)

      confWithNoCredentials.credentials.get should be(None)
    }
  }

  private def allArgs(short: Boolean, showVersion: Boolean = true, showHelp: Boolean = true): Seq[String] = {
    Seq(
      (if (short) "-a" else "--adapter-mappings-file"), "foo.xml",
      (if (short) "-s" else "--ontology-sql-file"), "foo.sql",
      (if (short) "-t" else "--rescan-timeout"), "5", "seconds",
      (if (short) "-p" else "--project-id"), projectId,
      (if (short) "-c" else "--credentials"), authn.domain, authn.username, authn.credential.value,
      (if (short) "-u" else "--url"), url,
      (if (short) "-o" else "--output-file"), "blarg.csv") ++ 
      (if(showVersion) Seq((if (short) "-v" else "--version")) else Nil)  ++
      (if(showHelp) Seq((if (short) "-h" else "--help")) else Nil)
  }

  @Test
  def testToTypesafeConfig {
    //No Args should make an empty config
    {
      val confWithNoArgs = new CommandLineScannerConfig(Nil)

      val config = confWithNoArgs.toTypesafeConfig

      config.entrySet.isEmpty should be(true)
    }

    //Some args
    def doSomeArgsTest(timeout: Int, timeUnit: String, toDuration: Int => Duration) {
      val args = Seq("-t", timeout.toString, timeUnit, "-c", authn.domain, authn.username, authn.credential.value, "-u", url)

      val conf = new CommandLineScannerConfig(args)

      val config = conf.toTypesafeConfig

      conf.rescanTimeout() should equal(toDuration(timeout))

      config.getInt(s"${Keys.reScanTimeout}.$timeUnit") should equal(timeout)

      config.getString(s"${Keys.credentials}.${Keys.domain}") should equal(authn.domain)
      config.getString(s"${Keys.credentials}.${Keys.username}") should equal(authn.username)
      config.getString(s"${Keys.credentials}.${Keys.password}") should equal(authn.credential.value)

      config.getString(Keys.shrineUrl) should equal(url)
      
      intercept[Exception] {
        config.getString(Keys.outputFile)
      }
    }

    //All args
    def doAllArgsTest(short: Boolean) {
      val args = allArgs(short)

      val config = (new CommandLineScannerConfig(args)).toTypesafeConfig

      config.getString(Keys.adapterMappingsFile) should equal("foo.xml")
      config.getString(Keys.ontologySqlFile) should equal("foo.sql")
      config.getInt(s"${Keys.reScanTimeout}.seconds") should equal(5)
      config.getString(Keys.projectId) should equal(projectId)
      config.getString(s"${Keys.credentials}.${Keys.domain}") should equal(authn.domain)
      config.getString(s"${Keys.credentials}.${Keys.username}") should equal(authn.username)
      config.getString(s"${Keys.credentials}.${Keys.password}") should equal(authn.credential.value)
      config.getString(Keys.shrineUrl) should equal(url)
      config.getString(Keys.outputFile) should equal("blarg.csv")
    }

    doSomeArgsTest(123, Keys.milliseconds, _.milliseconds)
    doSomeArgsTest(42, Keys.seconds, _.seconds)
    doSomeArgsTest(99, Keys.minutes, _.minutes)
    
    doAllArgsTest(true)
    doAllArgsTest(false)
  }

  @Test
  def testParse {
    def doAllArgsTest(short: Boolean) {
      val args = allArgs(short)

      val config = new CommandLineScannerConfig(args)

      config.adapterMappingsFile() should equal("foo.xml")
      config.ontologySqlFile() should equal("foo.sql")
      config.rescanTimeout() should equal(5.seconds)
      config.projectId() should equal(projectId)
      config.credentials() should equal(authn)
      config.url() should equal(url)
      config.outputFile() should equal("blarg.csv")
    }

    doAllArgsTest(true)
    doAllArgsTest(false)

    //No Args
    {
      val config = new CommandLineScannerConfig(Nil)

      config.adapterMappingsFile.get should be(None)
      config.ontologySqlFile.get should be(None)
      config.rescanTimeout.get should be(None)
      config.projectId.get should be(None)
      config.credentials.get should be(None)
      config.url.get should be(None)
      config.outputFile.get should be(None)
    }
  }

  @Test
  def testVersionToggle {
    def doTestVersionToggle(showVersion: Boolean) {
      val args = allArgs(true, showVersion)

      val config = new CommandLineScannerConfig(args)
      
      config.showVersionToggle.isSupplied should be(showVersion)
    }
    
    doTestVersionToggle(true)
    doTestVersionToggle(false)
  }
  
  @Test
  def testHelpToggle {
    def doTestHelpToggle(help: Boolean) {
      val args = allArgs(true, showVersion = false, showHelp = help)

      val config = new CommandLineScannerConfig(args)
      
      config.showVersionToggle.isSupplied should be(false)
      config.showHelpToggle.isSupplied should be(help)
    }
    
    doTestHelpToggle(true)
    doTestHelpToggle(false)
  }
}