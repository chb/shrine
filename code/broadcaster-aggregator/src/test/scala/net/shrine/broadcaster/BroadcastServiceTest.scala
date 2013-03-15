package net.shrine.broadcaster

import junit.framework.TestCase
import org.scalatest.junit.ShouldMatchersForJUnit
import org.junit.Test
import net.shrine.protocol.DeleteQueryRequest
import net.shrine.protocol.BroadcastMessage
import net.shrine.protocol.ShrineResponse
import net.shrine.aggregation.Aggregator
import scala.concurrent.Future
import net.shrine.protocol.RunQueryRequest
import net.shrine.protocol.AuthenticationInfo
import net.shrine.protocol.Credential
import net.shrine.protocol.query.QueryDefinition
import net.shrine.protocol.query.Term
import net.shrine.aggregation.DeleteQueryAggregator
import net.shrine.aggregation.RunQueryAggregator

/**
 * @author clint
 * @date Mar 14, 2013
 */
final class BroadcastServiceTest extends TestCase with ShouldMatchersForJUnit {
  import BroadcastServiceTest._
  
  private val authn = AuthenticationInfo("some-domain", "some-user", Credential("some-password", false))
  
  private val queryDef = QueryDefinition("yo", Term("foo"))
  
  @Test
  def testSendAndAggregateShrineRequest {
    val service = new TestBroadcastService
    
    {
      val req = DeleteQueryRequest("projectId", 1L, authn, 123L)
      
      val aggregator = new DeleteQueryAggregator
      
      service.sendAndAggregate(req, aggregator, true)
      
      service.args.shouldBroadcast should be(Some(true))
      
      service.sendAndAggregate(req, aggregator, false)
      
      service.args.shouldBroadcast should be(Some(false))
      service.args.aggregator should be(aggregator)
      service.args.message.request should be(req)
      (service.args.message.requestId > 0) should be(true)
    }
    
    {
      val invalidQueryId = -1L
      
      val req = RunQueryRequest("projectId", 1L, authn, invalidQueryId, "topicId", Set.empty, queryDef)
      
      val aggregator = new RunQueryAggregator(invalidQueryId, authn.username, authn.domain, queryDef, true)
      
      service.sendAndAggregate(req, aggregator, true)
      
      service.args.shouldBroadcast should be(Some(true))
      
      (service.args.message.requestId > 0) should be(true)
      service.args.message.request should not be(req)
      service.args.message.request.asInstanceOf[RunQueryRequest].networkQueryId should be(service.args.message.requestId)
      
      service.args.aggregator should not be(aggregator)
      service.args.aggregator.asInstanceOf[RunQueryAggregator].queryId should be(service.args.message.requestId)
    }
  }
  
  @Test
  def testAddQueryIdAggregator {
    val service = new TestBroadcastService
    
    {
      val aggregator = new DeleteQueryAggregator
      
      val munged = service.addQueryId(null, aggregator)
      
      (munged eq aggregator) should be(true)
    }
    
    {
      val aggregator = new RunQueryAggregator(-1L, authn.username, authn.domain, queryDef, true)
      
      val message = BroadcastMessage(999L, null)
      
      val munged = service.addQueryId(message, aggregator).asInstanceOf[RunQueryAggregator]
      
      munged.queryId should be(message.requestId)
      
      munged.userId should equal(aggregator.userId)
      munged.groupId should equal(aggregator.groupId)
      munged.requestQueryDefinition should equal(aggregator.requestQueryDefinition)
      munged.addAggregatedResult should equal(aggregator.addAggregatedResult)
    }
  }
  
  @Test
  def testAddQueryIdShrineRequest {
    val service = new TestBroadcastService
    
    {
      val req = DeleteQueryRequest("projectId", 1L, authn, 123L)
      
      val (queryIdOption, transformedReq) = service.addQueryId(req)
      
      queryIdOption should be(None)
      
      transformedReq should be(req)
    }
    
    {
      val req = RunQueryRequest("projectId", 1L, authn, -1L, "topicId", Set.empty, QueryDefinition("yo", Term("foo")))
      
      val (queryIdOption, transformedReq: RunQueryRequest) = service.addQueryId(req)
      
      queryIdOption should not be(None)
      
      (queryIdOption.get > 0) should be(true) 
      
      transformedReq.networkQueryId should be(queryIdOption.get)
      
      transformedReq.projectId should be(req.projectId)
      transformedReq.waitTimeMs should be(req.waitTimeMs)
      transformedReq.authn should be(req.authn)
      transformedReq.topicId should be(req.topicId)
      transformedReq.outputTypes should be(req.outputTypes)
      transformedReq.queryDefinition should be(req.queryDefinition)
    }
  }
}

object BroadcastServiceTest {
  private final class TestBroadcastService extends BroadcastService {
    object args {
      var message: BroadcastMessage = _
      var aggregator: Aggregator = _
      var shouldBroadcast: Option[Boolean] = None
    }
    
    override def sendAndAggregate(message: BroadcastMessage, aggregator: Aggregator, shouldBroadcast: Boolean): Future[ShrineResponse] = {
      args.message = message
      args.aggregator = aggregator
      args.shouldBroadcast = Some(shouldBroadcast)
      
      Future.successful(null)
    }
  }
}