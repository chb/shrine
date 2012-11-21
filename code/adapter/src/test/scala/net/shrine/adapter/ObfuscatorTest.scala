package net.shrine.adapter

import junit.framework.TestCase
import org.scalatest.matchers.ShouldMatchers
import org.scalatest.junit.AssertionsForJUnit
import org.junit.Test
import net.shrine.adapter.dao.MockLegacyAdapterDao
import net.shrine.protocol.QueryResult
import net.shrine.protocol.ResultOutputType

/**
 * @author clint
 * @date Oct 22, 2012
 */
object ObfuscatorTest {
  private def within(range: Long)(a: Long, b: Long) = scala.math.abs(b - a) <= range
  
  def within3 = within(3) _
  def within1 = within(1) _
}

final class ObfuscatorTest extends TestCase with ShouldMatchers with AssertionsForJUnit {
  import ObfuscatorTest._
  
  @Test
  def testObfuscateLong {
    val l = 12345L
    
    val obfuscated = Obfuscator.obfuscate(l)
    
    within3(l, obfuscated) should be(true)
  }
  
  @Test
  def testObfuscateSeqOfQueryResults {
    final class ExplicitMockLegacyAdapterDao(obfuscationAmount: Option[Int]) extends MockLegacyAdapterDao {
      var passedResultIds: Seq[String] = Seq.empty
      var passedObfcsAmounts: Seq[Int] = Seq.empty
      
      override def findObfuscationAmount(resultId: String) = obfuscationAmount
      
      override def updateObfuscationAmount(resultId: String, obfscAmount: Int) {
        passedResultIds :+= resultId
        passedObfcsAmounts :+= obfscAmount
      }
    }
    
    def queryResult(resultId: Long, setSize: Long) = {
      import ResultOutputType._
      
      QueryResult(resultId, 123L, Some(PATIENT_COUNT_XML), setSize, None, None, None, QueryResult.StatusType.Finished.name, None)
    }

    val resultId1 = 12345L
    val resultId2 = 99999L
    
    val setSize1 = 123L
    val setSize2 = 987L
    
    val results = Seq(queryResult(resultId1, setSize1), queryResult(resultId2, setSize2))  
    
    {
      val expectedObfuscationAmount = Some(1)
      
      val Seq(QueryResult(_, _, _, obfscSetSize1, _, _, _, _, _, _), 
              QueryResult(_, _, _, obfscSetSize2, _, _, _, _, _, _)) = Obfuscator.obfuscate(results)
              
      within3(setSize1, obfscSetSize1) should be(true)
      within3(setSize2, obfscSetSize2) should be(true)
    }
  }
}