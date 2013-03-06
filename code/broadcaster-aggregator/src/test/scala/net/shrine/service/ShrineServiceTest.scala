package net.shrine.service

import scala.collection.JavaConverters._
import org.easymock.EasyMock.{ expect => invoke }
import org.easymock.EasyMock.isA
import org.easymock.EasyMock.same
import org.junit.Test
import org.scalatest.junit.AssertionsForJUnit
import org.scalatest.junit.ShouldMatchersForJUnit
import org.scalatest.mock.EasyMockSugar
import org.spin.client.AgentException
import org.spin.tools.crypto.signature.CertID
import org.spin.tools.crypto.signature.Identity
import org.spin.tools.crypto.Envelope
import org.spin.tools.Interval
import net.shrine.aggregation.Aggregator
import net.shrine.aggregation.SpinResultEntry
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
import org.spin.client.SpinClient
import scala.concurrent.Future
import org.spin.message.serializer.Stringable
import org.spin.client.Credentials

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
  def testToCredentials {
    val domain = "kalsjdkalsd"
    val username = "kalsjdlkjsadljsadl"
    val password = "ksdfhjksdfyksuyeh"
      
    val authn = AuthenticationInfo(domain, username, Credential(password, false))
    
    val credentials = ShrineService.toCredentials(authn)
    
    credentials should not be(null)
    credentials.domain should equal(domain)
    credentials.username should equal(username)
    credentials.password should equal(password)
  }
  
  @Test
  def testReadQueryInstances {
    val projectId = "foo"
    val queryId = 123L
    val authn = AuthenticationInfo("some-domain", "some-username", Credential("blarg", false))
    val req = ReadQueryInstancesRequest(projectId, 1L, authn, queryId)

    val service = new ShrineService(null, null, None, true, null)

    val response = service.readQueryInstances(req).asInstanceOf[ReadQueryInstancesResponse]

    response should not be (null)
    response.groupId should equal(projectId)
    response.queryMasterId should equal(queryId)
    response.userId should equal(authn.username)

    val Seq(instance) = response.queryInstances

    instance.startDate should not be (null)
    instance.endDate should not be (null)
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
      val service = new ShrineService(null, null, None, true, null)

      service.determinePeergroupFallingBackTo(projectId) should equal(projectId)
    }

    {
      val service = new ShrineService(null, null, Option(expectedPeerGroup), true, null)

      service.determinePeergroupFallingBackTo(projectId) should equal(expectedPeerGroup)
    }
  }

  @Test
  def testBroadcastMessage {
    val mockClient = mock[SpinClient]
    val nodeId = new CertID("98345")
    val service = new ShrineService(null, null, None, true, mockClient)
    val authn = new AuthenticationInfo("domain", "username", new Credential("passwd", false))
    val projectId = "projectId"
    val message = new BroadcastMessage(1L, new DeleteQueryRequest(projectId, 1L, authn, 1L))
    val queryType = message.request.requestType.name
    val peerGroupToQuery = projectId
    val credentials = ShrineService.toCredentials(authn)
    	
    import scala.collection.JavaConverters._

    val futureResultSet = Future.successful(ResultSet.of("some-query-id", true, 0, Seq.empty[Result].asJava, Seq.empty[Failure].asJava))

    expecting {
      invoke(mockClient.query(queryType, message, peerGroupToQuery, credentials)).andReturn(futureResultSet)
    }
    whenExecuting(mockClient) {
      service.broadcastMessage(message)
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

    val shrineService = new ShrineService(null, null, None, true, new MockSpinClient(resultSetWithNulls))

    val aggregator = new Aggregator {
      def aggregate(spinCacheResults: Seq[SpinResultEntry], errors: Seq[ErrorResponse]): ShrineResponse = ErrorResponse(spinCacheResults.size.toString + "," + errors.size.toString)
    }

    val aggregatedResult = shrineService.aggregate(resultSetWithNulls, aggregator)

    aggregatedResult should equal(ErrorResponse("5,2"))
  }

  @Test
  def testAggregateHandlesNullResults {
    import scala.collection.JavaConverters._

    val envelope = Envelope.unencrypted("jksahdjksahdjksadh")

    def result(description: Char) = new Result(new CertID("123456"), description.toString, envelope, Interval.milliseconds(500))

    val results = "ab".map(result) ++ Seq(null, null) ++ "cde".map(result)

    val resultSetWithNulls = ResultSet.of("query-id", true, results.size, results.asJava, Seq.empty.asJava)

    val spinAgent = new MockSpinClient(resultSetWithNulls)

    val shrineService = new ShrineService(null, null, None, true, spinAgent)

    val aggregator = new Aggregator {
      def aggregate(spinCacheResults: Seq[SpinResultEntry], errors: Seq[ErrorResponse]): ShrineResponse = ErrorResponse(spinCacheResults.size.toString)
    }

    val aggregatedResult = shrineService.aggregate(resultSetWithNulls, aggregator)

    aggregatedResult should equal(ErrorResponse("5"))
  }

  private final class MockSpinClient(toReturn: ResultSet) extends SpinClient {
    override def config = ???

    override def query[T : Stringable](queryType: String, input: T, peerGroup: String, credentials: Credentials): Future[ResultSet] = Future.successful(toReturn)
  }
}