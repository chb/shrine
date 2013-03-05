package net.shrine.spring

import junit.framework.TestCase
import org.scalatest.junit.ShouldMatchersForJUnit
import org.junit.Test
import org.spin.tools.config.EndpointType

/**
 * @author clint
 * @date Mar 4, 2013
 */
final class EndpointConfigMakerTest extends TestCase with ShouldMatchersForJUnit {
  import EndpointType._

  @Test
  def testApply {
    val url = "ksaljdlajsdkla"

    {
      val localMaker = new EndpointConfigMaker(Local)

      val localEndpointConfig = localMaker(url)

      localEndpointConfig should not be (null)
      localEndpointConfig.getAddress should equal(url)
      localEndpointConfig.getEndpointType should be(Local)
    }

    {
      val soapMaker = new EndpointConfigMaker(SOAP)

      val soapEndpointConfig = soapMaker(url)

      soapEndpointConfig should not be (null)
      soapEndpointConfig.getAddress should equal(url)
      soapEndpointConfig.getEndpointType should be(SOAP)
    }
  }
}