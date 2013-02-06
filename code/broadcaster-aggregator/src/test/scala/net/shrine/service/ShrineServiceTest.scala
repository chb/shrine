package net.shrine.service

import scala.collection.JavaConverters.seqAsJavaListConverter
import org.easymock.EasyMock.{expect => invoke}
import org.easymock.EasyMock.isA
import org.easymock.EasyMock.same
import org.junit.Test
import org.scalatest.junit.AssertionsForJUnit
import org.scalatest.junit.ShouldMatchersForJUnit
import org.scalatest.mock.EasyMockSugar
import org.spin.client.AgentException
import org.spin.client.SpinAgent
import org.spin.tools.crypto.signature.CertID
import org.spin.tools.crypto.signature.Identity
import org.spin.tools.crypto.Envelope
import org.spin.tools.Interval
import net.shrine.aggregation.Aggregator
import net.shrine.aggregation.SpinResultEntry
import net.shrine.config.ShrineConfig
import net.shrine.protocol.AuthenticationInfo
import net.shrine.protocol.BroadcastMessage
import net.shrine.protocol.Credential
import net.shrine.protocol.DeleteQueryRequest
import net.shrine.protocol.ErrorResponse
import net.shrine.protocol.ShrineResponse
import org.spin.message.QueryInfo
import org.spin.message.QueryInput
import org.spin.message.AckNack
import org.spin.message.serializer.BasicSerializer
import org.spin.message.ResultSet
import org.spin.message.Failure
import org.spin.message.Result
import org.spin.message.StatusCode
import net.shrine.protocol.ReadQueryInstancesRequest
import net.shrine.protocol.ReadQueryInstancesResponse

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
  def testReadQueryInstances {
    val projectId = "foo"
    val queryId = 123L
    val authn = AuthenticationInfo("some-domain", "some-username", Credential("blarg", false))
    val req = ReadQueryInstancesRequest(projectId, 1L, authn, queryId)
    
    val service = new ShrineService(null, null, null, null, true, null, None)
    
    val response = service.readQueryInstances(req).asInstanceOf[ReadQueryInstancesResponse]
    
    response should not be(null)
    response.groupId should equal(projectId)
    response.queryMasterId should equal(queryId)
    response.userId should equal(authn.username)
    
    val Seq(instance) = response.queryInstances
    
    instance.startDate should not be(null)
    instance.endDate should not be(null)
    instance.startDate should equal(instance.endDate)
    instance.groupId should equal(projectId)
    instance.queryInstanceId should equal(queryId.toString)
    instance.queryMasterId should equal(queryId.toString)
    instance.userId should equal(authn.username)
  }
  
  @Test
  def testDeterminePeerGroup {
    val expectedPeerGroup = "alksjdlaksjdlaksfj"
    val projectId = "projectId"
      
    {
      val service = new ShrineService(null, null, null, null, true, null, None)
      
      service.determinePeergroup(projectId) should equal(projectId)
    }
    
    {
      val service = new ShrineService(null, null, null, expectedPeerGroup, true, null, None)
      
      service.determinePeergroup(projectId) should equal(expectedPeerGroup)
    }
  }

  @Test(expected = classOf[AgentException])
  def testBroadcastMessage {
    val mockAgent = mock[SpinAgent]
    val nodeId = new CertID("98345")
    val service = new ShrineService(null, null, null, null, true, mockAgent, None)
    val ackNack = new AckNack("error", nodeId, StatusCode.QueryFailure)
    val authn = new AuthenticationInfo("domain", "username", new Credential("passwd", false))
    val message = new BroadcastMessage(1L, new DeleteQueryRequest("projectId", 1L, authn, 1L))
    val queryInfo = new QueryInfo
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
  
  @Test
  def testAggregateHandlesFailures {
    import scala.collection.JavaConverters._
    
    val envelope = Envelope.unencrypted("jksahdjksahdjksadh")
    
    def toResult(description: Char) = new Result(new CertID("123456"), description.toString, envelope, Interval.milliseconds(500))
    
    def toFailure(description: Char) = new Failure(new CertID("123456"), "http://example.com/foo", description.toString)
  
    val results = "ab".map(toResult) ++ Seq(null, null) ++ "cde".map(toResult)
    
    val failures = Seq(toFailure('x'), null, toFailure('z'))
    
    val resultSetWithNulls = ResultSet.of("query-id", true, results.size + failures.size, results.asJava, failures.asJava)

    val shrineService = new ShrineService(null, null, null, null, true, new MockSpinAgent(resultSetWithNulls), None)
    
    val aggregator = new Aggregator {
      def aggregate(spinCacheResults: Seq[SpinResultEntry], errors: Seq[ErrorResponse]): ShrineResponse = ErrorResponse(spinCacheResults.size.toString + "," + errors.size.toString)
    }
    
    val aggregatedResult = shrineService.aggregate("", null, aggregator)
    
    aggregatedResult should equal(ErrorResponse("5,2"))
  }
  
  @Test
  def testAggregateHandlesNullResults {
    import scala.collection.JavaConverters._
    
    val envelope = Envelope.unencrypted("jksahdjksahdjksadh")
    
    def result(description: Char) = new Result(new CertID("123456"), description.toString, envelope, Interval.milliseconds(500))
  
    val results = "ab".map(result) ++ Seq(null, null) ++ "cde".map(result)
    
    val resultSetWithNulls = ResultSet.of("query-id", true, results.size, results.asJava, Seq.empty.asJava)

    val spinAgent = new MockSpinAgent(resultSetWithNulls)
    
    val shrineService = new ShrineService(null, null, null, null, true, spinAgent, None)
    
    val aggregator = new Aggregator {
      def aggregate(spinCacheResults: Seq[SpinResultEntry], errors: Seq[ErrorResponse]): ShrineResponse = ErrorResponse(spinCacheResults.size.toString)
    }
    
    val aggregatedResult = shrineService.aggregate("", null, aggregator)
    
    aggregatedResult should equal(ErrorResponse("5"))
  }
  
  private final class MockSpinAgent(toReturn: ResultSet) extends SpinAgent {
    override def send(queryInfo: QueryInfo, conditions: AnyRef): AckNack = null

    override def send(queryInfo: QueryInfo, conditions: AnyRef, recipient: CertID): AckNack = null

    override def send[Conditions](queryInfo: QueryInfo, conditions: Conditions, serializer: BasicSerializer[Conditions]): AckNack = null

    override def send[Conditions](queryInfo: QueryInfo, conditions: Conditions, serializer: BasicSerializer[Conditions], recipient: CertID): AckNack = null

    override def send(queryInfo: QueryInfo, conditions: String): AckNack = null

    override def send(queryInfo: QueryInfo, conditions: String, recipient: CertID): AckNack = null

    override def send(queryInfo: QueryInfo, queryInput: QueryInput): AckNack = null

    override def receive(queryID: String, requestorID: Identity): ResultSet = toReturn

    override def receive(queryID: String, requestorID: Identity, waitTime: Long): ResultSet = toReturn

    override def receive(queryID: String, requestorID: Identity, waitTime: Long, numExpectedResponses: java.lang.Integer): ResultSet = toReturn

    override def waitForQueryToComplete(queryID: String, maxWaitTime: Long, numExpectedResponses: java.lang.Integer): Unit = ()

    override def getResult(queryID: String, requestorID: Identity): ResultSet = null

    override def getResultNoDelete(queryID: String, requestorID: Identity): ResultSet = null

    override def isComplete(queryID: String): Boolean = true

    override def hasUpdate(queryID: String, numResponders: Int): Boolean = false
  }
}