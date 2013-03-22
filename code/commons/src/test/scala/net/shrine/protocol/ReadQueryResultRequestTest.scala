package net.shrine.protocol

import junit.framework.TestCase
import org.scalatest.junit.ShouldMatchersForJUnit
import org.junit.Test
import net.shrine.util.XmlUtil

/**
 * @author clint
 * @date Nov 2, 2012
 */
final class ReadQueryResultRequestTest extends TestCase with ShouldMatchersForJUnit {

  private val authn = AuthenticationInfo("some-domain", "some-user", Credential("salkfa", false))

  private val req = ReadQueryResultRequest("some-project-id", 1000, authn, 123)

  @Test
  def testToXml {
    val expected = XmlUtil.stripWhitespace(
      <readQueryResult>
        <projectId>some-project-id</projectId>
        <waitTimeMs>1000</waitTimeMs>
        { authn.toXml }
        <queryId>123</queryId>
      </readQueryResult>).toString

    req.toXmlString should equal(expected)
  }

  @Test
  def testXmlRoundTrip {
    ReadQueryResultRequest.fromXml(req.toXml) should equal(req)
  }
}