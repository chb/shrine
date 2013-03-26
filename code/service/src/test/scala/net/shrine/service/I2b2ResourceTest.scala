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
    def doTestHandleBanInput(resourceMethod: I2b2Resource => String => Response) {
      val resource: I2b2Resource = new I2b2Resource(mock[ShrineRequestHandler])
      
      val fourHundredResponse = Response.status(400).build()
      
      val resp = resourceMethod(resource)("sadlkhjksafhjksafhjkasgfgjskdfhsjkdfhgjsdfg") 
      
      resp.getStatus should equal(fourHundredResponse.getStatus)
      resp.getEntity should be(null)
    }
    
    doTestHandleBanInput(_.doPDORequest)
    
    doTestHandleBanInput(_.doRequest)
  }
}