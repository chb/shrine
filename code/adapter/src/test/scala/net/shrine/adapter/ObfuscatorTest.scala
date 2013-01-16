package net.shrine.adapter

import junit.framework.TestCase
import org.scalatest.matchers.ShouldMatchers
import org.scalatest.junit.AssertionsForJUnit
import org.junit.Test
import net.shrine.protocol.QueryResult
import net.shrine.protocol.ResultOutputType
import net.shrine.protocol.I2b2ResultEnvelope

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
  def testObfuscateQueryResult {
    import ResultOutputType._
    
    val breakdowns = Map(PATIENT_AGE_COUNT_XML -> I2b2ResultEnvelope(PATIENT_AGE_COUNT_XML, Map("x" -> 1, "y" -> 42)), 
    		  			 PATIENT_GENDER_COUNT_XML -> I2b2ResultEnvelope(PATIENT_GENDER_COUNT_XML, Map("a" -> 123, "b" -> 456)))
    
    def queryResult(resultId: Long, setSize: Long) = QueryResult(resultId, 123L, Some(PATIENT_COUNT_XML), setSize, None, None, None, QueryResult.StatusType.Finished, None, breakdowns)

    val resultId1 = 12345L
    
    val setSize1 = 123L
    
    {
      val QueryResult(_, _, _, obfscSetSize1, _, _, _, _, _, obfscBreakdowns) = Obfuscator.obfuscate(queryResult(resultId1, setSize1))
              
      within3(setSize1, obfscSetSize1) should be(true)
      
      breakdowns.keySet should equal(obfscBreakdowns.keySet)
      
      for {
        (resultType, obfscEnv) <- obfscBreakdowns
        env <- breakdowns.get(resultType) 
      } {
        env.data.keySet should equal(obfscEnv.data.keySet)
        
        for {
          (key, value) <- env.data
          obfscValue <- obfscEnv.data.get(key)
        } {
          within3(value, obfscValue) should be(true)
        }
      }
    }
  }
}