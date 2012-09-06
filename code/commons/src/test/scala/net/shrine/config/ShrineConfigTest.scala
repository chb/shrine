package net.shrine.config

import junit.framework.TestCase
import org.scalatest.junit.AssertionsForJUnit
import org.scalatest.matchers.ShouldMatchers
import org.junit.Test
import org.spin.tools.JAXBUtils

/**
 * @author clint
 * @date Sep 6, 2012
 */
final class ShrineConfigTest extends TestCase with AssertionsForJUnit with ShouldMatchers {
  @Test
  def testJaxbRoundTrip {
    import JAXBUtils.{marshalToString, unmarshal}
   
    val config = new ShrineConfig("humanReadableNodeName", true, "broadcasterPeerGroupToQuery", 999, 123L, 456L, true, 9876, true, true, "identityServiceClass", "databasePropertiesFile", "realCRCEndpoint", "shrineEndpoint", "aggregatorEndpoint", "pmEndpoint", "ontEndpoint", "sheriffEndpoint", "translatorClass", "queryActionMapClassName", "adapterStatusQuery", true)
    
    val xml = marshalToString(config)
    
    val unmarshalled = unmarshal(xml, classOf[ShrineConfig])
    
    unmarshalled should equal(config)
  }
}