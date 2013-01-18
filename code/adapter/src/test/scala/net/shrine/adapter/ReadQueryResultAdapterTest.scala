package net.shrine.adapter

import junit.framework.TestCase
import org.scalatest.junit.ShouldMatchersForJUnit
import net.shrine.protocol.ReadQueryResultRequest
import net.shrine.protocol.ReadQueryResultResponse
import org.junit.Test

/**
 * @author clint
 * @date Nov 7, 2012
 */
final class ReadQueryResultAdapterTest extends 
	AbstractQueryRetrievalTestCase(
	    (dao, httpClient) => new ReadQueryResultAdapter("", httpClient, AbstractQueryRetrievalTestCase.hiveCredentials, dao, true), 
	    (queryId, authn) => ReadQueryResultRequest("some-project-id", 1000L, authn, queryId), 
	    ReadQueryResultResponse.unapply) {
  @Test
  def testProcessInvalidRequest = doTestProcessInvalidRequest
  
  @Test
  def testProcessRequest = doTestProcessRequest
  
  @Test
  def testProcessRequestMissingQuery = doTestProcessRequestMissingQuery
  
  @Test
  def testProcessRequestIncompleteQuery = doTestProcessRequestIncompleteQuery
}