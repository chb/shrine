package net.shrine.service

import org.scalatest.junit.{AssertionsForJUnit, ShouldMatchersForJUnit}
import org.junit.Test
import net.shrine.config.ShrineConfig
import org.scalatest.mock.EasyMockSugar
import org.easymock.EasyMock.{expect => invoke, _}
import org.spin.query.message.headers.QueryInfo
import net.shrine.protocol.{Credential, AuthenticationInfo, DeleteQueryRequest, BroadcastMessage}
import org.spin.query.message.serializer.BasicSerializer
import org.spin.node.acknack.AckNack
import org.spin.query.message.cache.StatusCode
import org.spin.query.message.agent.{AgentException, SpinAgent}

/**
 * @author Bill Simons
 * @date 3/30/11
 * @link http://cbmi.med.harvard.edu
 * @link http://chip.org
 *       <p/>
 *       NOTICE: This software comes with NO guarantees whatsoever and is
 *       licensed as Lgpl Open Source
 * @link http://www.gnu.org/licenses/lgpl.html
 */
class ShrineServiceTest extends AssertionsForJUnit with ShouldMatchersForJUnit with EasyMockSugar {

  @Test
  def testDeterminePeerGroup() = {
    val shrineConfig = new ShrineConfig();
    val service = new ShrineService(null, null, null, shrineConfig, null)
    val expectedPeerGroup = "alksjdlaksjdlaksfj"
    val projectId = "projectId"
    service.determinePeergroup(projectId) should equal(projectId)
    shrineConfig.setBroadcasterPeerGroupToQuery(expectedPeerGroup)
    service.determinePeergroup(projectId) should equal(expectedPeerGroup)
  }

  @Test(expected = classOf[AgentException])
  def testBroadcastMessage() {
    val mockAgent = mock[SpinAgent]
    val service = new ShrineService(null, null, null, null, mockAgent)
    val ackNack = new AckNack("error", StatusCode.QueryFailure)
    val authn = new AuthenticationInfo("domain", "username", new Credential("passwd", false))
    val message = new BroadcastMessage(1L, new DeleteQueryRequest("projectId", 1L, authn, 1L))
    val queryInfo = new QueryInfo()
    expecting {
      invoke(mockAgent.send(
        same(queryInfo),
        same(message),
        isA(classOf[BasicSerializer[BroadcastMessage]]))).andReturn(ackNack)
    }
    whenExecuting(mockAgent) {
      service.broadcastMessage(message, queryInfo)
    }
  }
}