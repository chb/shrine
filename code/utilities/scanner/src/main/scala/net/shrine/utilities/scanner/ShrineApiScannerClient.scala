package net.shrine.utilities.scanner

import net.shrine.client.ShrineClient
import scala.concurrent.Future
import scala.concurrent.blocking
import scala.concurrent.ExecutionContext
import net.shrine.util.Loggable
import net.shrine.protocol.query.Term
import net.shrine.protocol.QueryResult
import net.shrine.protocol.query.QueryDefinition
import net.shrine.protocol.AggregatedRunQueryResponse
import java.util.concurrent.Executors
import ScannerClient._
import net.shrine.utilities.scanner.components.HasSingleThreadExecutionContextComponent

/**
 * @author clint
 * @date Mar 12, 2013
 */
final case class ShrineApiScannerClient(val shrineClient: ShrineClient) extends ScannerClient with HasSingleThreadExecutionContextComponent with Loggable {
  private val shouldBroadcast = false

  override def query(term: String): Future[TermResult] = Future {
    //blocking {
      import Scanner.QueryDefaults._

      info(s"Querying for '$term'")

      val aggregatedResults: AggregatedRunQueryResponse = shrineClient.runQuery(topicId, outputTypes, toQueryDef(term), shouldBroadcast)

      aggregatedResults.results.headOption match {
        case None => errorTermResult(aggregatedResults.queryId, term)
        case Some(queryResult) => TermResult(aggregatedResults.queryId, term, queryResult.statusType, queryResult.setSize)
      }
    //}
  }

  override def retrieveResults(termResult: TermResult): Future[TermResult] = Future {
    //blocking {
      info(s"Retrieving results for previously-incomplete query for '${termResult.term}'")
      
      val aggregatedResults = shrineClient.readQueryResult(termResult.networkQueryId, shouldBroadcast)

      aggregatedResults.results.headOption match {
        case None => errorTermResult(aggregatedResults.queryId, termResult.term)
        case Some(queryResult) => TermResult(aggregatedResults.queryId, termResult.term, queryResult.statusType, queryResult.setSize)
      }
    //}
  }
}