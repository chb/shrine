package net.shrine.utilities.scanner

import net.shrine.util.Loggable
import scala.concurrent.Future
import org.spin.client.SpinClient
import org.spin.tools.config.DefaultPeerGroups
import net.shrine.protocol.CRCRequestType
import net.shrine.protocol.RunQueryRequest
import org.spin.client.Credentials
import net.shrine.protocol.AuthenticationInfo
import net.shrine.protocol.Credential
import net.shrine.protocol.BroadcastMessage
import net.shrine.protocol.BroadcastMessage
import scala.concurrent.ExecutionContext
import java.util.concurrent.Executors
import net.shrine.protocol.RunQueryResponse
import ScannerClient._
import scala.collection.JavaConverters._
import net.shrine.protocol.ReadQueryResultRequest
import net.shrine.protocol.ReadQueryResultResponse
import net.shrine.utilities.scanner.components.HasSingleThreadExecutionContextComponent

/**
 * @author clint
 * @date Mar 12, 2013
 */
final case class SpinApiScannerClient(val projectId: String, val spinClient: SpinClient) extends ScannerClient with HasSingleThreadExecutionContextComponent with Loggable {
  private val peerGroupToQuery = DefaultPeerGroups.LOCAL.name
  
  private val waitTimeMs = 10000
  
  private[scanner] lazy val authorization = {
    val credentials = spinClient.config.credentials
    
    AuthenticationInfo(credentials.domain, credentials.username, Credential(credentials.password, false))
  }

  override def query(term: String): Future[TermResult] = {
    import Scanner.QueryDefaults._

    info(s"Querying for '$term'")

    val queryId = BroadcastMessage.Ids.next
    
    val message = BroadcastMessage(queryId, RunQueryRequest(projectId, waitTimeMs, authorization, queryId, topicId, outputTypes, toQueryDef(term)))

    val futureResultSet = spinClient.query(queryType(message), message, peerGroupToQuery)

    for {
      spinResultSet <- futureResultSet
    } yield {
      val termResultOption = for {
        spinResult <- spinResultSet.getResults.asScala.headOption
        runQueryResponse = RunQueryResponse.fromXml(spinResult.getPayload.getData)
        shrineQueryResult <- runQueryResponse.results.headOption
      } yield TermResult(runQueryResponse.queryId, term, shrineQueryResult.statusType, shrineQueryResult.setSize)
      
      //TODO: Is this the right query id to use here?
      termResultOption.getOrElse(errorTermResult(queryId, term))
    }
  }
  
  override def retrieveResults(termResult: TermResult): Future[TermResult] = {
    info(s"Retrieving results for previously-incomplete query for '${termResult.term}'")
      
    val message = BroadcastMessage(ReadQueryResultRequest(projectId, waitTimeMs, authorization, termResult.networkQueryId))
    
    val futureResultSet = spinClient.query(queryType(message), message, peerGroupToQuery)
    
    for {
      spinResultSet <- futureResultSet
    } yield {
      val termResultOption = for {
        spinResult <- spinResultSet.getResults.asScala.headOption
        readQueryResultResponse = ReadQueryResultResponse.fromXml(spinResult.getPayload.getData)
        shrineQueryResult <- readQueryResultResponse.results.headOption
      } yield TermResult(readQueryResultResponse.queryId, termResult.term, shrineQueryResult.statusType, shrineQueryResult.setSize)
      
      //TODO: Is this the right query id to use here?
      termResultOption.getOrElse(errorTermResult(termResult.networkQueryId, termResult.term))
    }
  }
  
  private def queryType(message: BroadcastMessage) = message.request.requestType.name
}