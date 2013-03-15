package net.shrine.broadcaster

import net.shrine.protocol.ShrineRequest
import scala.concurrent.Future
import net.shrine.protocol.ShrineResponse
import net.shrine.aggregation.Aggregator
import net.shrine.protocol.BroadcastMessage
import net.shrine.protocol.RunQueryRequest
import net.shrine.aggregation.RunQueryAggregator
import net.shrine.aggregation.ReadQueryResultAggregator

/**
 * @author clint
 * @date Mar 13, 2013
 */
trait BroadcastService {
  def sendAndAggregate(message: BroadcastMessage, aggregator: Aggregator, shouldBroadcast: Boolean): Future[ShrineResponse]
  
  def sendAndAggregate(request: ShrineRequest, aggregator: Aggregator, shouldBroadcast: Boolean): Future[ShrineResponse] = {
    val (queryId, requestToSend) = addQueryId(request)
    
    val broadcastMessage = BroadcastMessage(queryId.getOrElse(newQueryId), requestToSend)
    
    val aggregatorWithCorrectQueryId = addQueryId(broadcastMessage, aggregator)
    
    sendAndAggregate(broadcastMessage, aggregatorWithCorrectQueryId, shouldBroadcast)
  }
  
  protected[broadcaster] def addQueryId(request: ShrineRequest): (Option[Long], ShrineRequest) = request match {
    case runQueryReq: RunQueryRequest => {
      val queryId = newQueryId

      (Some(queryId), runQueryReq.withNetworkQueryId(queryId))
    }
    case _ => (None, request)
  }
  
  protected[broadcaster] def addQueryId(message: BroadcastMessage, aggregator: Aggregator): Aggregator = aggregator match {
    case runQueryAggregator: RunQueryAggregator => runQueryAggregator.withQueryId(message.requestId)
    case readQueryResultAggregator: ReadQueryResultAggregator => readQueryResultAggregator.withShrineNetworkQueryId(message.requestId) 
    case _ => aggregator
  }
  
  private def newQueryId = BroadcastMessage.Ids.next
}