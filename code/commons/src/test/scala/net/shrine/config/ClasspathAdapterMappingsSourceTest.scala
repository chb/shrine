package net.shrine.config

import org.scalatest.junit.AssertionsForJUnit
import org.scalatest.junit.ShouldMatchersForJUnit
import org.junit.Test

/**
 * @author clint
 * @date Mar 9, 2012
 * 
 */
final class ClasspathAdapterMappingsSourceTest extends AssertionsForJUnit with ShouldMatchersForJUnit {
  @Test
  def testLoad {
    {
      val shouldNotThrowWhenInvoked = new ClasspathAdapterMappingsSource("askjdklasd")
      
      intercept[Exception] {
        shouldNotThrowWhenInvoked.load
      }
    }
    
    intercept[Exception] {
      new ClasspathAdapterMappingsSource(null)
    }
    
    val mappings = (new ClasspathAdapterMappingsSource("AdapterMappings_DEM_AGE_0_9.xml")).load
    
    mappings should not be(null)
    
    mappings.size should be(2)
    
    import scala.collection.JavaConverters._
    
    mappings.networkTerms should be(Set("""\\i2b2\i2b2\Demographics\Age\0-9 years old\""", """\\i2b2\i2b2\Demographics\"""))
  }
}