package net.shrine.broadcaster.spin

import net.shrine.broadcaster.BroadcastService
import net.shrine.protocol.BroadcastMessage
import net.shrine.aggregation.Aggregator
import scala.concurrent.Future
import net.shrine.util.Util
import net.shrine.util.Loggable
import org.spin.message.ResultSet
import org.spin.tools.config.DefaultPeerGroups
import org.spin.client.SpinClient
import net.shrine.protocol.ShrineResponse
import scala.concurrent.ExecutionContext
import scala.concurrent.blocking
import org.spin.message.Response
import net.shrine.protocol.AuthenticationInfo
import org.spin.client.Credentials
import org.spin.tools.crypto.Envelope
import org.spin.tools.crypto.PKCryptor
import scala.util.Try
import net.shrine.aggregation.SpinResultEntry
import net.shrine.protocol.ErrorResponse

/**
 * @author clint
 * @date Mar 13, 2013
 */
final class SpinBroadcastService(val spinClient: SpinClient) extends BroadcastService with Loggable {
  import ExecutionContext.Implicits.global
  
  private lazy val broadcasterPeerGroupToQuery: Option[String] = Option(spinClient.config.peerGroupToQuery)

  override def sendAndAggregate(message: BroadcastMessage, aggregator: Aggregator, shouldBroadcast: Boolean): Future[ShrineResponse] = {
    def toShrineResponse(resultSet: ResultSet): ShrineResponse = Util.time("Aggregating")(debug(_)) {
      val result = aggregate(resultSet, aggregator)

      debug("Aggregated into a " + result.getClass.getName)

      result
    }

    sendMessage(message, shouldBroadcast).map(blocking(toShrineResponse))
  }

  import SpinBroadcastService._

  private[spin] def sendMessage(message: BroadcastMessage, shouldBroadcast: Boolean): Future[ResultSet] = {
    val queryType = message.request.requestType.name

    val credentials = toCredentials(message.request.authn)

    val peerGroupToQuery = determinePeergroup(message.request.projectId, shouldBroadcast)

    Util.time("Broadcasting via Spin")(debug(_)) {
      spinClient.query(queryType, message, peerGroupToQuery, credentials)
    }
  }

  private[spin] def aggregate(spinResults: ResultSet, aggregator: Aggregator): ShrineResponse = {

    def toDescription(response: Response): String = Option(response).map(_.getDescription).getOrElse("Unknown")

    import scala.collection.JavaConverters._

    val (results, failures, nullResponses) = {
      val (results, nullResults) = spinResults.getResults.asScala.partition(_ != null)

      val (failures, nullFailures) = spinResults.getFailures.asScala.partition(_ != null)

      (results, failures, nullResults ++ nullFailures)
    }

    if (!failures.isEmpty) {
      warn("Received " + failures.size + " failures. descriptions:")

      failures.map("  " + _.getDescription).foreach(this.warn(_))
    }

    if (!nullResponses.isEmpty) {
      error("Received " + nullResponses.size + " null results.  Got non-null results from " + (results.size + failures.size) + " nodes: " + (results ++ failures).map(toDescription))
    }

    def decrypt(envelope: Envelope) = {
      if (envelope.isEncrypted) (new PKCryptor).decrypt(envelope)
      else envelope.getData
    }

    def toHostName(url: String): Option[String] = Try(new java.net.URL(url).getHost).toOption

    val spinResultEntries = results.map(result => new SpinResultEntry(decrypt(result.getPayload), result))

    //TODO: Make something better here, using the failing node's human-readable name.  
    //Using the failing node's hostname is the best we can do for now.
    val errorResponses = for {
      failure <- failures
      hostname <- toHostName(failure.getOriginUrl)
    } yield ErrorResponse(hostname)

    aggregator.aggregate(spinResultEntries.toSeq, errorResponses.toSeq)
  }

  private[spin] def determinePeergroup(projectId: String, broadcastDesired: Boolean): String = {
    if (broadcastDesired) { broadcasterPeerGroupToQuery.getOrElse(projectId) }
    else { DefaultPeerGroups.LOCAL.name }
  }
}

object SpinBroadcastService {
  private[spin] def toCredentials(authn: AuthenticationInfo): Credentials = Credentials(authn.domain, authn.username, authn.credential.value)
}