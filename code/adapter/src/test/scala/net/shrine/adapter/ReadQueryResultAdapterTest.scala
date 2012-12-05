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
	    dao => new ReadQueryResultAdapter(dao, true), 
	    queryId => ReadQueryResultRequest("some-project-id", 1000L, null, queryId), 
	    ReadQueryResultResponse.unapply) {
  @Test
  def testProcessInvalidRequest = doTestProcessInvalidRequest
  
  @Test
  def testProcessRequest = doTestProcessRequest
}