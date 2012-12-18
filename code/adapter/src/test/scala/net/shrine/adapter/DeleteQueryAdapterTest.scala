package net.shrine.adapter

import org.springframework.test.AbstractDependencyInjectionSpringContextTests
import org.scalatest.junit.ShouldMatchersForJUnit
import org.junit.Test
import org.spin.tools.crypto.signature.Identity
import net.shrine.protocol.BroadcastMessage
import net.shrine.protocol.DeleteQueryRequest
import net.shrine.protocol.AuthenticationInfo
import net.shrine.protocol.Credential
import net.shrine.protocol.ErrorResponse
import net.shrine.protocol.DeleteQueryResponse
import net.shrine.protocol.query.Term
import net.shrine.protocol.RenameQueryRequest

/**
 * @author clint
 * @date Nov 27, 2012
 */
final class DeleteQueryAdapterTest extends AbstractDependencyInjectionSpringContextTests with AdapterDbTest with AdapterTestHelpers with ShouldMatchersForJUnit {
  @Test
  def testProcessRequest = afterCreatingTables {
    
    val adapter = new DeleteQueryAdapter(dao)

    {
      val DeleteQueryResponse(returnedId) = adapter.processRequest(id, new BroadcastMessage(123L, new DeleteQueryRequest("proj", 1000L, authn, queryId)))

      returnedId should equal(queryId)
    }

    //Add a query
    dao.insertQuery(localMasterId, queryId, "some-query", authn, Term("foo"))

    //sanity check that it's there
    {
      val Some(query) = dao.findQueryByNetworkId(queryId)

      query.networkId should equal(queryId)
    }

    {
      //try to delete a bogus query
      val DeleteQueryResponse(returnedId) = adapter.processRequest(id, new BroadcastMessage(123L, new DeleteQueryRequest("proj", 1000L, authn, bogusQueryId)))

      returnedId should equal(bogusQueryId)

      //Query in the DB should be unchanged
      val Some(query) = dao.findQueryByNetworkId(queryId)

      query.networkId should equal(queryId)
    }
    
    {
      //try to delete a real query
      val DeleteQueryResponse(returnedId) = adapter.processRequest(id, new BroadcastMessage(123L, new DeleteQueryRequest("proj", 1000L, authn, queryId)))

      returnedId should equal(queryId)

      //Query in the DB should be gone
      dao.findQueryByNetworkId(queryId) should be(None)
    }
  }
  
  @Test
  def testProcessRequestBadRequest {
    val adapter = new DeleteQueryAdapter(dao)

    intercept[Exception] {
      adapter.processRequest(id, new BroadcastMessage(123L, new RenameQueryRequest("proj", 1000L, authn, queryId, "foo")))
    }
  }
}