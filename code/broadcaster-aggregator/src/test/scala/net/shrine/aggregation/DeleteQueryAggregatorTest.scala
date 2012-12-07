package net.shrine.aggregation

import org.scalatest.junit.{ShouldMatchersForJUnit, AssertionsForJUnit}
import org.junit.Test
import org.junit.Assert.{assertNotNull,assertEquals}
import net.shrine.protocol.DeleteQueryResponse

/**
 * @author Bill Simons
 * @date 8/16/11
 * @link http://cbmi.med.harvard.edu
 * @link http://chip.org
 *       <p/>
 *       NOTICE: This software comes with NO guarantees whatsoever and is
 *       licensed as Lgpl Open Source
 * @link http://www.gnu.org/licenses/lgpl.html
 */
final class DeleteQueryAggregatorTest extends AssertionsForJUnit with ShouldMatchersForJUnit{

  @Test
  def testAggregate {
    val queryId = 12345l
    val response = new DeleteQueryResponse(queryId)
    val result1 = new SpinResultEntry(response.toXmlString, null)
    val result2 = new SpinResultEntry(response.toXmlString, null)

    val aggregator = new DeleteQueryAggregator
    
    //TODO: test handling error responses
    val deleteQueryResponse = aggregator.aggregate(Vector(result1, result2), Nil).asInstanceOf[DeleteQueryResponse]
    
    assertNotNull(deleteQueryResponse)
    
    assertEquals(queryId, deleteQueryResponse.queryId)
  }
}