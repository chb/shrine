package net.shrine.protocol

import junit.framework.TestCase
import org.scalatest.junit.ShouldMatchersForJUnit
import org.junit.Test

/**
 * @author clint
 * @date Nov 2, 2012
 */
final class ReadPreviousQueryResultResponseTest extends TestCase with ShouldMatchersForJUnit {
  private val results = Seq(QueryResult.errorResult(Some("xyz"), "foo"), 
                             QueryResult(123L,
                                         456L,
                                         Some(ResultOutputType.PATIENT_COUNT_XML),
                                         999L,
                                         None,
                                         None,
                                         None,
                                         "FINISHED",
                                         None,
                                         Map(ResultOutputType.PATIENT_AGE_COUNT_XML -> I2b2ResultEnvelope(ResultOutputType.PATIENT_AGE_COUNT_XML, Map("x" -> 123, "y" -> 214)))))
  
  private val resp = ReadQueryResultResponse(123, results)
  
  @Test
  def testToXml {
    val expected = (<readPreviousQueryResultResponse><queryId>123</queryId><results>{results.map(_.toXml)}</results></readPreviousQueryResultResponse>).toString
    
    resp.toXmlString should equal(expected)
  }
  
  
  @Test
  def testXmlRoundTrip {
    ReadQueryResultResponse.fromXml(resp.toXml).get should equal(resp)
  }
}