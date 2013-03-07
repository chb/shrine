package net.shrine.utilities.scanner

import com.google.inject.AbstractModule
import net.codingwell.scalaguice.ScalaModule
import net.shrine.ont.data.OntologyDAO
import net.shrine.ont.data.ShrineSqlOntologyDAO
import net.shrine.config.AdapterMappingsSource
import net.shrine.config.ClasspathAdapterMappingsSource
import net.shrine.client.JerseyShrineClient
import net.shrine.client.ShrineClient
import scala.concurrent.duration.Duration
import scala.concurrent.duration._
import com.google.inject.Provider
import net.shrine.config.ClasspathAdapterMappingsSource
import java.io.FileInputStream
import net.shrine.protocol.AuthenticationInfo
import net.shrine.protocol.Credential
import net.codingwell.scalaguice.InjectorExtensions.ScalaInjector
import com.google.inject.Guice
import com.google.inject.Inject

/**
 * @author clint
 * @date Mar 6, 2013
 */
final class ScannerModule(config: ScannerConfig) extends AbstractModule with ScalaModule {
  import ScannerModule._
  
  override def configure {
    bind[ScannerConfig].toInstance(config)
    
    bind[AdapterMappingsSource].toProvider[ClasspathAdapterMappingsSourceProvider]

    bind[OntologyDAO].toProvider[ShrineSqlOntologyDaoProvider]
    
    bind[ShrineClient].toProvider[JerseyShrineClientProvider]

    bind[Scanner].to[ConcreteScanner]
  }
}

object ScannerModule {
  private final class ClasspathAdapterMappingsSourceProvider @Inject() (config: ScannerConfig) extends 
  	AbstractProvider(new ClasspathAdapterMappingsSource(config.adapterMappingsFile))
  
  private final class ShrineSqlOntologyDaoProvider @Inject() (config: ScannerConfig) extends
  	AbstractProvider(new ShrineSqlOntologyDAO(getClass.getClassLoader.getResourceAsStream(config.ontologySqlFile)))
  
  private final class JerseyShrineClientProvider @Inject() (config: ScannerConfig) extends
  	AbstractProvider(new JerseyShrineClient(config.shrineUrl, config.projectId, config.authorization, true))
  
  private abstract class AbstractProvider[T](constructor: => T) extends Provider[T] {
    override def get = constructor
  }
  
  def main(args: Array[String]) {
    import scala.concurrent.duration._
    
    val config = ScannerConfig("testAdapterMappings.xml", 
    						   "testShrineWithSyns.sql",
    						   10.seconds, 
    						   "https://shrine-dev1.chip.org:6060/shrine-cell/rest/", 
    						   "SHRINE", AuthenticationInfo("HarvardDemo", "bsimons", Credential("testtest", false)))
    						   
    val injector = new ScalaInjector(Guice.createInjector(new ScannerModule(config)))
    
    val scanner = injector.instance[Scanner]
    
    scanner.scan()
  }
}