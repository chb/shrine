package net.shrine.adapter.dao.model

import junit.framework.TestCase
import org.scalatest.junit.ShouldMatchersForJUnit
import org.junit.Test
import net.shrine.util.Util
import net.shrine.protocol.ResultOutputType
import net.shrine.protocol.QueryResult
import net.shrine.adapter.dao.scalaquery.rows.CountRow
import net.shrine.adapter.dao.scalaquery.rows.QueryResultRow
import net.shrine.util.XmlGcEnrichments

/**
 * @author clint
 * @date Nov 1, 2012
 */
final class CountTest extends TestCase with ShouldMatchersForJUnit {
  val now = Util.now
  
  @Test
  def testToQueryResult {
    val resultId = 123
    val localResultId = 99999L
    val orig = 42
    val obfsc = 43
    
    val startDate = now
    val endDate = {
      import XmlGcEnrichments._
      
      now + 100L.milliseconds
    }
    
    val queryResult = {
      
      
      Count(1, resultId, localResultId, QueryResult.StatusType.Processing, orig, obfsc, now, startDate, endDate).toQueryResult
    }
    
    queryResult.breakdowns.isEmpty should be(true)
    queryResult.description should be(None)
    queryResult.startDate should be(Some(startDate))
    queryResult.endDate should be(Some(endDate))
    queryResult.instanceId should equal(resultId)
    queryResult.isError should be(false)
    queryResult.resultId should equal(localResultId)
    queryResult.resultType should equal(Some(ResultOutputType.PATIENT_COUNT_XML))
    queryResult.setSize should equal(obfsc)
    queryResult.statusMessage should be(None)
    queryResult.statusType should equal(QueryResult.StatusType.Processing)
  }
  
  @Test
  def testFromRows {
    val countRow = CountRow(123, 456, 19L, 20L, now)
    
    val localResultId = 789L
    
    val elapsed = 100L
    
    val resultRow = QueryResultRow(987, localResultId, 1, ResultOutputType.PATIENT_COUNT_XML, QueryResult.StatusType.Finished, Some(elapsed), now)
    
    val count = Count.fromRows(resultRow, countRow)
    
    count should not be(null)
    count.creationDate should equal(countRow.creationDate)
    count.id should equal(countRow.id)
    count.localId should equal(localResultId)
    count.obfuscatedValue should equal(countRow.obfuscatedValue)
    count.originalValue should equal(countRow.originalValue)
    count.resultId should equal(countRow.resultId)
    count.startDate should equal(countRow.creationDate)
    
    import XmlGcEnrichments._
    
    count.endDate should equal(countRow.creationDate + elapsed.milliseconds)
  }
}