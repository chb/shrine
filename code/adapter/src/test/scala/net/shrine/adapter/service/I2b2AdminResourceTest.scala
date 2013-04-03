package net.shrine.adapter.service

import junit.framework.TestCase
import org.scalatest.junit.ShouldMatchersForJUnit
import org.junit.Test
import javax.ws.rs.core.Response
import org.scalatest.mock.EasyMockSugar
import net.shrine.protocol.AuthenticationInfo
import net.shrine.protocol.I2b2AdminRequestHandler
import net.shrine.protocol.Credential
import net.shrine.protocol.ReadI2b2AdminPreviousQueriesRequest
import net.shrine.protocol.DeleteQueryRequest

/**
 * @author clint
 * @date Apr 3, 2013
 */
final class I2b2AdminResourceTest extends TestCase with ShouldMatchersForJUnit with EasyMockSugar {
  @Test
  def testHandleBadInput {
    def doTestHandleBadInput(resourceMethod: I2b2AdminResource => String => Response) {
      val resource = new I2b2AdminResource(mock[I2b2AdminRequestHandler])

      val fourHundredResponse = Response.status(400).build()

      //Just junk data
      {
        val resp = resourceMethod(resource)("sadlkhjksafhjksafhjkasgfgjskdfhsjkdfhgjsdfg")

        resp.getStatus should equal(fourHundredResponse.getStatus)
        resp.getEntity should be(null)
      }
      
      //A correctly-serialized request that we can't handle
      {
        val authn = AuthenticationInfo("d", "u", Credential("p", false))
        
        val resp = resourceMethod(resource)(DeleteQueryRequest("p", 123L, authn, 99L).toI2b2String)

        resp.getStatus should equal(fourHundredResponse.getStatus)
        resp.getEntity should be(null)
      }
    }

    doTestHandleBadInput(_.doRequest)
  }
}