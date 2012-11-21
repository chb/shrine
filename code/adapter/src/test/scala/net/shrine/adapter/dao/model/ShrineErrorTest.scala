package net.shrine.adapter.dao.model

import junit.framework.TestCase
import org.scalatest.junit.ShouldMatchersForJUnit
import net.shrine.protocol.QueryResult

/**
 * @author clint
 * @date Nov 1, 2012
 */
final class ShrineErrorTest extends TestCase with ShouldMatchersForJUnit {
  def testToQueryResult {
    val message = "something broke"
    
    val error = ShrineError(1, 123, message)  
    
    error.toQueryResult should equal(QueryResult.errorResult(Some(message), QueryResult.StatusType.Error.name))
  }
}