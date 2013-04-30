package net.shrine.protocol

import junit.framework.TestCase
import org.scalatest.junit.ShouldMatchersForJUnit
import org.junit.Test

/**
 * @author clint
 * @date Apr 30, 2013
 */
final class NonI2b2ableRequestTest extends TestCase with ShouldMatchersForJUnit {
  object TestRequest extends ShrineRequest("", 0L, null) with NonI2b2ableRequest {
    def messageBody = i2b2MessageBody
    
    override val requestType = null
    
    override def toXml = ???
  }
  
  @Test
  def testI2b2MessageBody {
    intercept[Error] {
      TestRequest.messageBody
    }
  }
  
  @Test
  def testToI2b2 {
    intercept[Error] {
      TestRequest.toI2b2
    }
  }
}