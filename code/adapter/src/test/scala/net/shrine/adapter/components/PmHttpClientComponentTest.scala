package net.shrine.adapter.components

import junit.framework.TestCase
import org.scalatest.junit.ShouldMatchersForJUnit
import org.junit.Test
import net.shrine.util.HttpClient

/**
 * @author clint
 * @date Apr 5, 2013
 */
final class PmHttpClientComponentTest extends TestCase with ShouldMatchersForJUnit {
  @Test
  def testUrlIsSupplied {
    val pmUrl = "lasdjlkasjdlkasjdlkasdjl"
    
    val component = new PmHttpClientComponent {
      val httpClient = new LazyMockHttpClient("")
      val pmEndpoint = pmUrl
    } 
    
    val payload = "foo"
    
    component.callPm(payload) should equal("")
    
    component.httpClient.inputParam should equal(Some(payload))
    
    component.httpClient.urlParam should equal(Some(pmUrl))
  }
}
