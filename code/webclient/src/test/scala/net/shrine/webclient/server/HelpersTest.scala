package net.shrine.webclient.server

import junit.framework.TestCase
import org.scalatest.junit.AssertionsForJUnit
import org.scalatest.matchers.ShouldMatchers
import org.junit.Test
import net.shrine.protocol.ResultOutputType
import net.shrine.protocol.I2b2ResultEnvelope
import net.shrine.protocol.I2b2ResultEnvelope.Column
import net.shrine.webclient.shared.domain.Breakdown
import com.sun.org.apache.xalan.internal.xsltc.compiler.ValueOf
import net.shrine.protocol.QueryResult

/**
 * @author clint
 * @date Sep 17, 2012
 */
final class HelpersTest extends TestCase with AssertionsForJUnit with ShouldMatchers {
  
  import scala.collection.JavaConverters._

  private def toColumn(i: Int) = ("col_" + i,  i.toLong)
  
  private val cols = (1 to 4).map(toColumn).toMap
  
  private val envs = ResultOutputType.breakdownTypes.map(rt => I2b2ResultEnvelope(rt, cols))
  
  private def toLong(l: Long) = java.lang.Long.valueOf(l)
  
  val breakdown = new Breakdown(cols.mapValues(toLong).asJava)
  
  private val expectedBreakdowns = ResultOutputType.breakdownTypes.map(rt => rt.name -> breakdown).toMap
  
  @Test
  def testMakeBreakdownsByTypeMap {
        
    val result = Helpers.makeBreakdownsByTypeMap(envs)
    
    result.asScala should equal(expectedBreakdowns)
  }
  
  @Test
  def testMakeSingleInstitutionQueryResult {
    
    val setSize = 12345L 
    
    val queryResult = QueryResult(123L, 999L, Some(ResultOutputType.PATIENT_COUNT_XML), setSize, None, None, None, QueryResult.StatusType.Finished, None)
    
    val actual = Helpers.makeSingleInstitutionQueryResult(queryResult)
    
    actual.getCount should equal(setSize)
    actual.getBreakdowns.isEmpty should be(true)
    
    val queryResultWithBreakdowns = queryResult.copy(breakdowns = envs.map(e => e.resultType -> e).toMap)
    
    val actualWithBreakdowns = Helpers.makeSingleInstitutionQueryResult(queryResultWithBreakdowns)
    
    actual.getBreakdowns.isEmpty should be(true)
    actualWithBreakdowns.getBreakdowns.asScala should equal(expectedBreakdowns)
  }
  
  @Test
  def testMakeBreakdown {
    val env = envs.head
    
    val actual = Helpers.makeBreakdown(env)
    
    actual should equal(breakdown)
  }
}