package net.shrine.adapter

import org.scalatest.junit.ShouldMatchersForJUnit
import org.junit.Test
import net.shrine.protocol.BroadcastMessage
import net.shrine.protocol.RenameQueryRequest
import net.shrine.protocol.ReadQueryDefinitionRequest
import net.shrine.protocol.ReadQueryDefinitionResponse
import net.shrine.protocol.ErrorResponse
import net.shrine.protocol.query.Term
import net.shrine.protocol.query.QueryDefinition
import net.shrine.adapter.spring.AbstractShrineJUnitSpringTest

/**
 * @author clint
 * @date Nov 28, 2012
 */
final class ReadQueryDefinitionAdapterTest extends AbstractShrineJUnitSpringTest with AdapterDbTest with AdapterTestHelpers with ShouldMatchersForJUnit {
  @Test
  def testProcessRequest = afterCreatingTables {
    val name = "blarg"
    val expr = Term("foo")
      
    val adapter = new ReadQueryDefinitionAdapter(dao)
    
    //Should get error for non-existent query
    {
      val ErrorResponse(msg) = adapter.processRequest(id, new BroadcastMessage(123L, new ReadQueryDefinitionRequest("proj", 1000L, authn, queryId)))

      msg should not be(null)
    }

    //Add a query
    dao.insertQuery(localMasterId, queryId, name, authn, expr)

    //sanity check that it's there
    {
      val Some(query) = dao.findQueryByNetworkId(queryId)

      query.networkId should equal(queryId)
    }

    {
      //Should still get error for non-existent query
      val ErrorResponse(msg) = adapter.processRequest(id, new BroadcastMessage(123L, new ReadQueryDefinitionRequest("proj", 1000L, authn, bogusQueryId)))

      msg should not be(null)
    }
    
    {
      //try to read a real query
      val ReadQueryDefinitionResponse(rQueryId, rName, userId, createDate, queryDefinition) = adapter.processRequest(id, new BroadcastMessage(123L, new ReadQueryDefinitionRequest("proj", 1000L, authn, queryId)))

      rQueryId should equal(queryId)
      rName should equal(name)
      userId should equal(authn.username)
      createDate should not be(null) // :(
      queryDefinition should equal(QueryDefinition(name, expr).toI2b2String)
    }
  }
  
  @Test
  def testProcessRequestBadRequest {
    val adapter = new ReadQueryDefinitionAdapter(dao)

    intercept[Exception] {
      adapter.processRequest(id, new BroadcastMessage(123L, new RenameQueryRequest("proj", 1000L, authn, queryId, "foo")))
    }
  }
}