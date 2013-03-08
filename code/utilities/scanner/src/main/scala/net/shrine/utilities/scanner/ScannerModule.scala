package net.shrine.utilities.scanner

import net.shrine.ont.data.OntologyDAO
import net.shrine.ont.data.ShrineSqlOntologyDAO
import net.shrine.config.AdapterMappingsSource
import net.shrine.config.ClasspathAdapterMappingsSource
import net.shrine.client.JerseyShrineClient
import net.shrine.client.ShrineClient
import scala.concurrent.duration.Duration
import scala.concurrent.duration._
import net.shrine.config.ClasspathAdapterMappingsSource
import java.io.FileInputStream
import net.shrine.protocol.AuthenticationInfo
import net.shrine.protocol.Credential

/**
 * @author clint
 * @date Mar 6, 2013
 */
final class ScannerModule(config: ScannerConfig) extends Scanner {
  
  override val reScanTimeout = config.reScanTimeout
  
  override val adapterMappingsSource = new ClasspathAdapterMappingsSource(config.adapterMappingsFile)
  
  override val ontologyDao = new ShrineSqlOntologyDAO(classpathStream(config.ontologySqlFile))
  
  override val shrineClient = new JerseyShrineClient(config.shrineUrl, config.projectId, config.authorization)
  
  private def classpathStream(fileName: String) = getClass.getClassLoader.getResourceAsStream(fileName)
}

object ScannerModule {
  def main(args: Array[String]) {
    
    val config = ScannerConfig("testAdapterMappings.xml", 
    						   "testShrineWithSyns.sql",
    						   10.seconds, 
    						   "https://shrine-dev1.chip.org:6060/shrine-cell/rest/", 
    						   "SHRINE", 
    						   AuthenticationInfo("HarvardDemo", "bsimons", Credential("testtest", false)))
    						   
    val scanner = new ScannerModule(config)
  }
}