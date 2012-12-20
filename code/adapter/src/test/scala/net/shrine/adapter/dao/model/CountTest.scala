package net.shrine.adapter.dao.model

import junit.framework.TestCase
import org.scalatest.junit.ShouldMatchersForJUnit
import org.junit.Test
import net.shrine.util.Util
import net.shrine.protocol.ResultOutputType
import net.shrine.protocol.QueryResult

/**
 * @author clint
 * @date Nov 1, 2012
 */
final class CountTest extends TestCase with ShouldMatchersForJUnit {
  @Test
  def testToQueryResult {
    val resultId = 123
    val localResultId = 99999L
    val orig = 42
    val obfsc = 43
    
    val queryResult = Count(1, resultId, localResultId, orig, obfsc, Util.now).toQueryResult
    
    queryResult.breakdowns.isEmpty should be(true)
    queryResult.description should be(None)
    queryResult.endDate should be(None)
    queryResult.startDate should be(None)
    queryResult.instanceId should equal(resultId)
    queryResult.isError should be(false)
    queryResult.resultId should equal(localResultId)
    queryResult.resultType should equal(Some(ResultOutputType.PATIENT_COUNT_XML))
    queryResult.setSize should equal(obfsc)
    queryResult.statusMessage should be(None)
    queryResult.statusType should equal(QueryResult.StatusType.Finished)
  }
}