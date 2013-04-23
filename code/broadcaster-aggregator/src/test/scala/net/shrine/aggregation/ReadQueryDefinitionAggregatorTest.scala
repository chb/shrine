package net.shrine.aggregation

import org.scalatest.junit.{ ShouldMatchersForJUnit, AssertionsForJUnit }
import org.junit.Test
import org.junit.Assert.assertNotNull
import org.spin.tools.NetworkTime
import net.shrine.protocol.ReadQueryDefinitionResponse
import net.shrine.util.Util

/**
 * @author Bill Simons
 * @date 6/14/11
 * @link http://cbmi.med.harvard.edu
 * @link http://chip.org
 *       <p/>
 *       NOTICE: This software comes with NO guarantees whatsoever and is
 *       licensed as Lgpl Open Source
 * @link http://www.gnu.org/licenses/lgpl.html
 */
final class ReadQueryDefinitionAggregatorTest extends AssertionsForJUnit with ShouldMatchersForJUnit {

  val aggregator = new ReadQueryDefinitionAggregator

  val queryId = 1L
  val userId = "userId"
  val queryName = "queryname"
  val queryDefinition = "<queryDef/>"

  @Test
  def testAggregate {
    val response1 = ReadQueryDefinitionResponse(Some(queryId), Some(queryName), Some(userId), Some(Util.now), Some(queryDefinition))

    val response2 = ReadQueryDefinitionResponse(Some(queryId), Some(queryName), Some(userId), Some(Util.now), Some(queryDefinition))

    doTestAggregate(response1, response2)
  }

  @Test
  def testAggregateSomeEmpty {
    doTestAggregate(ReadQueryDefinitionResponse.Empty, ReadQueryDefinitionResponse(Some(queryId), Some(queryName), Some(userId), Some(Util.now), Some(queryDefinition)))
    
    doTestAggregate(ReadQueryDefinitionResponse(Some(queryId), Some(queryName), Some(userId), Some(Util.now), Some(queryDefinition)), ReadQueryDefinitionResponse.Empty)
    
    doTestAggregate(ReadQueryDefinitionResponse.Empty, ReadQueryDefinitionResponse.Empty, ReadQueryDefinitionResponse.Empty) 
  }

  private def doTestAggregate(responses: ReadQueryDefinitionResponse*) {
    val results = responses.map(resp => SpinResultEntry(resp.toXmlString, null))
    
    //TODO: test handling error responses
    val actual = aggregator.aggregate(results, Nil).asInstanceOf[ReadQueryDefinitionResponse]

    assertNotNull(actual)

    actual.masterId should equal(responses.head.masterId)
    actual.name should equal(responses.head.name)
    actual.userId should equal(responses.head.userId)
    actual.createDate should equal(responses.head.createDate)
    actual.queryDefinition should equal(responses.head.queryDefinition)
  }
}
