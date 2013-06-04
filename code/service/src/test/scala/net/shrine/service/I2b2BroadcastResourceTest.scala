package net.shrine.service

import junit.framework.TestCase
import org.scalatest.junit.ShouldMatchersForJUnit
import org.junit.Test
import javax.ws.rs.core.Response
import org.scalatest.mock.EasyMockSugar
import net.shrine.protocol.ShrineRequestHandler
import net.shrine.protocol.ReadI2b2AdminPreviousQueriesRequest
import net.shrine.protocol.AuthenticationInfo
import net.shrine.protocol.Credential

/**
 * @author clint
 * @date Mar 25, 2013
 */
final class I2b2BroadcastResourceTest extends TestCase with ShouldMatchersForJUnit with EasyMockSugar {
  @Test
  def testHandleBadInput {
    def doTestHandleBadInput(resourceMethod: I2b2BroadcastResource => String => Response) {
      val resource = new I2b2BroadcastResource(mock[ShrineRequestHandler])

      val fiveHundredResponse = Response.status(500).build()

      //Just junk data
      {
        val resp = resourceMethod(resource)("sadlkhjksafhjksafhjkasgfgjskdfhsjkdfhgjsdfg")

        resp.getStatus should equal(400)
        resp.getEntity should be(null)
      }
      
      //A correctly-serialized request that we can't handle
      {
        val authn = AuthenticationInfo("d", "u", Credential("p", false))
        
        val resp = resourceMethod(resource)(ReadI2b2AdminPreviousQueriesRequest("p", 123L, authn, "foo", 20).toI2b2String)

        resp.getStatus should equal(500)
        resp.getEntity should be(null)
      }
    }

    doTestHandleBadInput(_.doPDORequest)

    doTestHandleBadInput(_.doRequest)
  }
}