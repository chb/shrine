package net.shrine.utilities.scanner

import junit.framework.TestCase
import org.scalatest.junit.ShouldMatchersForJUnit
import org.junit.Test
import net.shrine.utilities.scanner.components.HasClasspathAndCommandLineScannerConfig
import net.shrine.utilities.scanner.components.HasCommandLineConfig
import net.shrine.utilities.scanner.components.HasArgs
import net.shrine.protocol.AuthenticationInfo
import net.shrine.protocol.Credential

/**
 * @author clint
 * @date May 2, 2013
 */
final class HasClasspathAndCommandLineAndScannerConfigTest extends TestCase with ShouldMatchersForJUnit {
  @Test
  def testCommandLineTrumpsConfigFile {
    final class TestHasClasspathAndCommandLineScannerConfig(override val args: Seq[String]) extends HasClasspathAndCommandLineScannerConfig with HasCommandLineConfig with HasArgs
    
    import scala.concurrent.duration._
    
    val adapterMappingsFile = "from-command-line.xml"
    val ontologySqlFile = "from-command-line.sql"
	val reScanTimeout = 42 minutes
	val rescanTimeoutForCommandLine = Seq("42", "minutes")
	val shrineUrl = "http://example.com/from-command-line"
	val projectId = "FROM_COMMAND_LINE"
	val authorization = AuthenticationInfo("command-line-domain", "command-line-user", Credential("command-line-password", false))
    val outputFile = "from-command-line.csv"
    
    val args = Seq("-a", adapterMappingsFile,
    			   "-s", ontologySqlFile,
    			   "-t") ++ rescanTimeoutForCommandLine ++ Seq(
    			   "-u", shrineUrl,
    			   "-p", projectId,
    			   "-c") ++ Seq(authorization.domain, authorization.username, authorization.credential.value) ++ Seq(
    			   "-o", outputFile)
      
    val module = new TestHasClasspathAndCommandLineScannerConfig(args)
    
    module.config.adapterMappingsFile should equal(adapterMappingsFile)
    module.config.ontologySqlFile should equal(ontologySqlFile)
    module.config.reScanTimeout should equal(reScanTimeout)
    module.config.shrineUrl should equal(shrineUrl)
    module.config.projectId should equal(projectId)
    module.config.authorization should equal(authorization)
    module.config.outputFile should equal(outputFile)
  }
}