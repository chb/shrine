package net.shrine.protocol

import junit.framework.TestCase
import org.scalatest.junit.ShouldMatchersForJUnit
import org.junit.Test
import net.shrine.protocol.handlers.DeleteQueryHandler

/**
 * @author clint
 * @date Apr 2, 2013
 */
final class DoubleDispatchingShrineRequestTest extends TestCase with ShouldMatchersForJUnit {
  @Test
  def testIsHandledBy {
    val projectId = "ASJKLFHDkj"
    val waitTimeMs = 21345L
    val authn = AuthenticationInfo("d", "u", Credential("p", false))
    
    val req = DeleteQueryRequest(projectId, waitTimeMs, authn, 123456789L)
    
    req.isHandledBy[ShrineRequestHandler] should be(true)
    req.isHandledBy[DeleteQueryHandler] should be(true)
  }
}