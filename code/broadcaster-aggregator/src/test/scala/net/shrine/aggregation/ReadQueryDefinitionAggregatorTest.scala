package net.shrine.aggregation

import org.scalatest.junit.{ShouldMatchersForJUnit, AssertionsForJUnit}
import org.junit.Test
import org.junit.Assert.assertNotNull
import org.spin.tools.NetworkTime
import net.shrine.protocol.ReadQueryDefinitionResponse

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

  @Test
  def testAggregate {
    val queryId = 1L
    val userId = "userId"
    val queryName = "queryname"
    val queryDefinition = "<queryDef/>"
    
    val response1 = new ReadQueryDefinitionResponse(queryId, queryName, userId, new NetworkTime().getXMLGregorianCalendar, queryDefinition)
    
    val result1 = new SpinResultEntry(response1.toXmlString, null)
    
    val response2 = new ReadQueryDefinitionResponse(queryId, queryName, userId, new NetworkTime().getXMLGregorianCalendar, queryDefinition)
    
    val result2 = new SpinResultEntry(response2.toXmlString, null)
    
    //TODO: test handling error responses
    val actual = aggregator.aggregate(Vector(result1, result2), Nil).asInstanceOf[ReadQueryDefinitionResponse]
    
    assertNotNull(actual)
    
    actual.masterId should equal(queryId)
    actual.name should equal(queryName)
    actual.userId should equal(userId)
    actual.queryDefinition should equal(queryDefinition)
  }
}
