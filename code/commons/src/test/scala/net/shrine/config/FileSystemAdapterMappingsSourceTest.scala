package net.shrine.config

import junit.framework.TestCase
import org.scalatest.junit.ShouldMatchersForJUnit
import org.junit.Test
import java.io.File

/**
 * @author clint
 * @date Mar 26, 2013
 */
final class FileSystemAdapterMappingsSourceTest extends TestCase with ShouldMatchersForJUnit {
  @Test
  def testLoad {
    def doTestLoadbadFileName(source: FileSystemAdapterMappingsSource) {
      intercept[Exception] { source.load }
    }
    
    doTestLoadbadFileName(new FileSystemAdapterMappingsSource("mksaldklasjdklasjd"))
    doTestLoadbadFileName(new FileSystemAdapterMappingsSource(new File("mksaldklasjdklasjd")))
    
    intercept[Exception] { new FileSystemAdapterMappingsSource(null: File) }
    
    intercept[Exception] { new FileSystemAdapterMappingsSource(null: String) }
    
    def doTestLoad(source: FileSystemAdapterMappingsSource) {
      val mappings = source.load
    
      mappings should not be(null)
    
      mappings.size should be(2)
    
      mappings.networkTerms should be(Set("""\\i2b2\i2b2\Demographics\Age\0-9 years old\""", """\\i2b2\i2b2\Demographics\"""))
    }
    
    doTestLoad(new FileSystemAdapterMappingsSource("src/test/resources/AdapterMappings_DEM_AGE_0_9.xml"))
    doTestLoad(new FileSystemAdapterMappingsSource(new File("src/test/resources/AdapterMappings_DEM_AGE_0_9.xml")))
  }
}