package net.shrine.broadcaster.spin

import junit.framework.TestCase
import org.scalatest.junit.ShouldMatchersForJUnit
import net.shrine.protocol.AuthenticationInfo
import net.shrine.protocol.Credential
import org.junit.Test
import org.spin.tools.crypto.Envelope
import org.spin.message.Result
import org.spin.message.ResultSet
import org.spin.tools.crypto.signature.CertID
import org.spin.tools.Interval
import net.shrine.aggregation.Aggregator
import net.shrine.aggregation.SpinResultEntry
import net.shrine.protocol.ErrorResponse
import net.shrine.protocol.ShrineResponse
import org.spin.client.SpinClient
import org.spin.message.serializer.Stringable
import org.spin.client.Credentials
import scala.concurrent.Future
import org.spin.tools.config.DefaultPeerGroups
import net.shrine.protocol.BroadcastMessage
import net.shrine.protocol.DeleteQueryRequest
import org.spin.message.Failure
import org.spin.client.SpinClientConfig
import org.spin.client.Credentials

/**
 * @author clint
 * @date Mar 13, 2013
 */
final class SpinBroadcastServiceTest extends TestCase with ShouldMatchersForJUnit {
  import SpinBroadcastServiceTest._
  
  @Test
  def testToCredentials {
    val domain = "kalsjdkalsd"
    val username = "kalsjdlkjsadljsadl"
    val password = "ksdfhjksdfyksuyeh"
      
    val authn = AuthenticationInfo(domain, username, Credential(password, false))
    
    val credentials = SpinBroadcastService.toCredentials(authn)
    
    credentials should not be(null)
    credentials.domain should equal(domain)
    credentials.username should equal(username)
    credentials.password should equal(password)
  }
  
  @Test
  def testAggregateHandlesNullResults {
    import scala.collection.JavaConverters._

    val envelope = Envelope.unencrypted("jksahdjksahdjksadh")

    def result(description: Char) = new Result(new CertID("123456"), description.toString, envelope, Interval.milliseconds(500))

    val results = "ab".map(result) ++ Seq(null, null) ++ "cde".map(result)

    val resultSetWithNulls = ResultSet.of("query-id", true, results.size, results.asJava, Seq.empty.asJava)

    val spinAgent = new MockSpinClient(resultSetWithNulls, None)

    val broadcastService = new SpinBroadcastService(spinAgent)

    val aggregator = new Aggregator {
      def aggregate(spinCacheResults: Seq[SpinResultEntry], errors: Seq[ErrorResponse]): ShrineResponse = ErrorResponse(spinCacheResults.size.toString)
    }

    val aggregatedResult = broadcastService.aggregate(resultSetWithNulls, aggregator)

    aggregatedResult should equal(ErrorResponse("5"))
  }
  
  @Test
  def testDeterminePeerGroup {
    val expectedPeerGroup = "alksjdlaksjdlaksfj"
    val projectId = "projectId"

    //shouldBroadcast = true
    {
      val service = new SpinBroadcastService(new MockSpinClient(null, None))

      service.determinePeergroup(projectId, true) should equal(projectId)
    }

    {
      val service = new SpinBroadcastService(new MockSpinClient(null, Option(expectedPeerGroup)))

      service.determinePeergroup(projectId, true) should equal(expectedPeerGroup)
    }
    
    //shouldBroadcast = false
    {
      val service = new SpinBroadcastService(new MockSpinClient(null, None))

      service.determinePeergroup(projectId, false) should equal(DefaultPeerGroups.LOCAL.name)
    }

    {
      val service = new SpinBroadcastService(new MockSpinClient(null, Option(expectedPeerGroup)))

      service.determinePeergroup(projectId, false) should equal(DefaultPeerGroups.LOCAL.name)
    }
  }

  @Test
  def testSendMessage {
    val projectId = "projectId"
    val peerGroupToQuery = projectId
    val mockClient = new MockSpinClient(null, Some(peerGroupToQuery))
    val nodeId = new CertID("98345")
    val service = new SpinBroadcastService(mockClient)
    val authn = new AuthenticationInfo("domain", "username", new Credential("passwd", false))
    val message = new BroadcastMessage(1L, new DeleteQueryRequest(projectId, 1L, authn, 1L))
    val queryType = message.request.requestType.name
    val credentials = SpinBroadcastService.toCredentials(authn)
    	
    service.sendMessage(message, true)
    
    mockClient.queryTypeParam.get should be(queryType)
    mockClient.inputParam.get should be(message)
    mockClient.peerGroupParam.get should be(peerGroupToQuery)
    mockClient.credentialsParam.get should be(credentials)
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

    val broadcastService = new SpinBroadcastService(new MockSpinClient(resultSetWithNulls, None))

    val aggregator = new Aggregator {
      def aggregate(spinCacheResults: Seq[SpinResultEntry], errors: Seq[ErrorResponse]): ShrineResponse = ErrorResponse(spinCacheResults.size.toString + "," + errors.size.toString)
    }

    val aggregatedResult = broadcastService.aggregate(resultSetWithNulls, aggregator)

    aggregatedResult should equal(ErrorResponse("5,2"))
  }
}

object SpinBroadcastServiceTest {
  private final class MockSpinClient(toReturn: ResultSet, peerGroupToQuery: Option[String]) extends SpinClient {
    override val config = SpinClientConfig.Default.withPeerGroupToQuery(peerGroupToQuery.orNull)

    var queryTypeParam: Option[String] = None
    var inputParam: Option[Any] = None
    var peerGroupParam: Option[String] = None
    var credentialsParam: Option[Credentials] = None
    
    override def query[T : Stringable](queryType: String, input: T, peerGroup: String, credentials: Credentials): Future[ResultSet] = {
      queryTypeParam = Some(queryType)
      inputParam = Some(input)
      peerGroupParam = Some(peerGroup)
      credentialsParam = Some(credentials)
      
      Future.successful(toReturn)
    }
  }
}