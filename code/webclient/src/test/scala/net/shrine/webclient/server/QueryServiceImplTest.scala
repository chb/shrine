package net.shrine.webclient.server

import junit.framework.TestCase
import org.scalatest.junit.AssertionsForJUnit
import org.scalatest.matchers.ShouldMatchers
import org.junit.Test
import net.shrine.service.ShrineClient
import net.shrine.protocol.ReadQueryDefinitionResponse
import net.shrine.protocol.ResultOutputType
import net.shrine.protocol.ReadPdoResponse
import net.shrine.protocol.ReadInstanceResultsResponse
import net.shrine.protocol.RenameQueryResponse
import net.shrine.protocol.ReadQueryInstancesResponse
import net.shrine.protocol.query.QueryDefinition
import net.shrine.protocol.DeleteQueryResponse
import net.shrine.protocol.RunQueryResponse
import net.shrine.protocol.ReadPreviousQueriesResponse
import scala.xml.NodeSeq
import net.shrine.protocol.ReadApprovedQueryTopicsResponse
import org.spin.tools.NetworkTime
import java.util.Date
import net.shrine.protocol.QueryResult
import net.shrine.protocol.query.Or
import net.shrine.protocol.query.Term
import net.shrine.protocol.query.And

/**
 * @author clint
 * @date May 22, 2012
 */
final class QueryServiceImplTest extends TestCase with AssertionsForJUnit with ShouldMatchers {
  @Test
  def testQueryForBreakdown {
    val toReturn = Map("fooInst" -> 123, "barInst" -> 42)
    
    val mockClient = new MockShrineClient(toReturn)
    
    val queryService = new QueryServiceImpl(mockClient)
    val queryExpr = And(Term("nuh"), Or(Term("foo"), Term("Bar")))
    
    val queryResult = queryService.queryForBreakdown(queryExpr.toXmlString)
    
    mockClient.queryDefinition.expr should equal(queryExpr)
    
    queryResult.size should equal(toReturn.size)
    
    toReturn.map { case (instName, count) =>
      queryResult.contains(instName) should be(true)
      queryResult.get(instName).get should equal(count)
    }
  }
}
