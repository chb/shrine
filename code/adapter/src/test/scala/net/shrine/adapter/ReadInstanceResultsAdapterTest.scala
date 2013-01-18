package net.shrine.adapter

import junit.framework.TestCase
import org.scalatest.junit.ShouldMatchersForJUnit
import org.junit.Test
import javax.annotation.Resource
import net.shrine.protocol.BroadcastMessage
import net.shrine.protocol.RunQueryRequest
import net.shrine.protocol.ShrineRequest
import net.shrine.protocol.CRCRequestType
import net.shrine.protocol.CRCRequestType
import scala.xml.NodeSeq
import net.shrine.protocol.ShrineResponse
import net.shrine.protocol.ShrineRequestHandler
import net.shrine.protocol.ReadInstanceResultsRequest
import net.shrine.protocol.AuthenticationInfo
import net.shrine.protocol.Credential
import net.shrine.protocol.query.Term
import net.shrine.protocol.RunQueryResponse
import net.shrine.util.Util
import net.shrine.protocol.query.QueryDefinition
import net.shrine.protocol.QueryResult
import net.shrine.protocol.ResultOutputType
import net.shrine.protocol.I2b2ResultEnvelope
import net.shrine.protocol.ErrorResponse
import net.shrine.protocol.ReadInstanceResultsResponse
import net.shrine.config.HiveCredentials
import org.springframework.test.AbstractDependencyInjectionSpringContextTests
import net.shrine.adapter.dao.scalaquery.ScalaQueryAdapterDao

/**
 * @author clint
 * @date Nov 7, 2012
 */
final class ReadInstanceResultsAdapterTest extends 
	AbstractQueryRetrievalTestCase(
	    (dao, httpClient) => new ReadInstanceResultsAdapter("", httpClient, AbstractQueryRetrievalTestCase.hiveCredentials, dao, true), 
	    (queryId, authn) => ReadInstanceResultsRequest("some-project-id", 1000L, authn, queryId), 
	    ReadInstanceResultsResponse.unapply) {
  @Test
  def testProcessInvalidRequest = doTestProcessInvalidRequest
  
  @Test
  def testProcessRequest = doTestProcessRequest
  
  @Test
  def testProcessRequestMissingQuery = doTestProcessRequestMissingQuery
  
  @Test
  def testProcessRequestIncompleteQuery = doTestProcessRequestIncompleteQuery
}
