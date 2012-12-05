package net.shrine.protocol

import junit.framework.TestCase
import org.scalatest.junit.ShouldMatchersForJUnit
import org.junit.Test

/**
 * @author clint
 * @date Nov 2, 2012
 */
final class ReadQueryResultResponseTest extends TestCase with ShouldMatchersForJUnit {
  private val result = QueryResult(
    123L,
    456L,
    Some(ResultOutputType.PATIENT_COUNT_XML),
    999L,
    None,
    None,
    None,
    "FINISHED",
    None,
    Map(ResultOutputType.PATIENT_AGE_COUNT_XML -> I2b2ResultEnvelope(ResultOutputType.PATIENT_AGE_COUNT_XML, Map("x" -> 123, "y" -> 214))))

  private val resp = ReadQueryResultResponse(123, result)

  @Test
  def testToXml {
    val expected = (<readQueryResultResponse><queryId>123</queryId><results>{ result.toXml }</results></readQueryResultResponse>).toString

    resp.toXmlString should equal(expected)
  }

  @Test
  def testXmlRoundTrip {
    ReadQueryResultResponse.fromXml(resp.toXml) should equal(resp)
  }
}