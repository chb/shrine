package net.shrine.service

import junit.framework.TestCase
import org.scalatest.junit.ShouldMatchersForJUnit
import org.junit.Test
import javax.ws.rs.core.Response
import org.scalatest.mock.EasyMockSugar
import net.shrine.protocol.ShrineRequestHandler

/**
 * @author clint
 * @date Mar 25, 2013
 */
final class I2b2ResourceTest extends TestCase with ShouldMatchersForJUnit with EasyMockSugar {
  @Test
  def testHandleBadInput {
    def doTestHandleBanInput(resourceMethod: I2b2BroadcastResource => String => Response) {
      val resource = new I2b2BroadcastResource(mock[ShrineRequestHandler])
      
      val fourHundredResponse = Response.status(400).build()
      
      val resp = resourceMethod(resource)("sadlkhjksafhjksafhjkasgfgjskdfhsjkdfhgjsdfg") 
      
      resp.getStatus should equal(fourHundredResponse.getStatus)
      resp.getEntity should be(null)
    }
    
    doTestHandleBanInput(_.doPDORequest)
    
    doTestHandleBanInput(_.doRequest)
  }
}