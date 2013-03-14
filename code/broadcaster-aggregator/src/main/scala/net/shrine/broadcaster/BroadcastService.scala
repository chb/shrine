package net.shrine.broadcaster

import net.shrine.protocol.ShrineRequest
import scala.concurrent.Future
import net.shrine.protocol.ShrineResponse
import net.shrine.aggregation.Aggregator
import net.shrine.protocol.BroadcastMessage

/**
 * @author clint
 * @date Mar 13, 2013
 */
trait BroadcastService {
  def sendAndAggregate(message: BroadcastMessage, aggregator: Aggregator, shouldBroadcast: Boolean): Future[ShrineResponse]
  
  def sendAndAggregate(request: ShrineRequest, aggregator: Aggregator, shouldBroadcast: Boolean): Future[ShrineResponse] = {
    sendAndAggregate(BroadcastMessage(request), aggregator, shouldBroadcast)
  }
}