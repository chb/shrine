package net.shrine.utilities.scanner

import net.shrine.ont.data.OntologyDAO
import net.shrine.config.AdapterMappingsSource
import net.shrine.client.ShrineClient
import net.shrine.protocol.ResultOutputType
import net.shrine.protocol.query.QueryDefinition
import net.shrine.protocol.query.Term
import net.shrine.protocol.AggregatedRunQueryResponse
import net.shrine.protocol.QueryResult
import scala.concurrent.duration.Duration

/**
 * @author clint
 * @date Mar 5, 2013
 */
final class Scanner(ontologyDao: OntologyDAO, adapterMappingsSource: AdapterMappingsSource, shrineClient: ShrineClient, timeout: Duration) {
  import Scanner._
  
  private def toTermSet(results: Set[TermResult]): Set[String] = results.map(_.term)
  
  def scan(): ScanResults = {
    val mappedNetworkTerms = adapterMappingsSource.load.networkTerms
    
    def allShrineOntologyTerms = ontologyDao.ontologyEntries.map(_.path).toSet
    
    val termsExpectedToBeUnmapped = allShrineOntologyTerms -- mappedNetworkTerms
    
    //TODO: Don't flatmap, actually care about Nones (but under what circumstances could they come back?)
    val resultsForMappedTerms = mappedNetworkTerms.flatMap(query)
    
    //Terms that we expected to BE mapped, but were NOT mapped
    val shouldHaveBeenMapped = resultsForMappedTerms.filter(_.status.isError)
    
    //Terms that we expected to NOT be mapped, but ARE mapped
    val shouldNotHaveBeenMapped = termsExpectedToBeUnmapped.flatMap(query).filterNot(_.status.isError)
    
    //Terms that never completed after some timeout period
    val neverFinished = resultsForMappedTerms.filterNot(_.status.isDone)
    
    val reScanResults = reScan(neverFinished)

    val finalSouldHaveBeenMappedSet = toTermSet(shouldHaveBeenMapped) ++ reScanResults.shouldHaveBeenMapped
    
    ScanResults(finalSouldHaveBeenMappedSet, toTermSet(shouldNotHaveBeenMapped), reScanResults.stillNotFinished)
  }
  
  def reScan(neverFinished: Set[TermResult]): ReQueryResults = {
    if(neverFinished.isEmpty) { ReQueryResults(toTermSet(neverFinished)) }
    else { 
      Thread.sleep(timeout.toMillis)
      
      val (done, stillNotFinished) = neverFinished.flatMap(attemptToRetrieve).partition(_.status.isDone)
      
      val errors = done.filter(_.status.isError)
      
      ReQueryResults(toTermSet(stillNotFinished), toTermSet(errors))
    }
  }
  
  private[scanner] def attemptToRetrieve(termResult: TermResult): Option[TermResult] = {
    val aggregatedResults = shrineClient.readQueryResult(termResult.networkQueryId)
    
    for {
      queryResult <- aggregatedResults.results.headOption
    } yield TermResult(aggregatedResults.queryId, termResult.term, queryResult.statusType, queryResult.setSize)
  }
  
  private[scanner] def query(term: String): Option[TermResult] = {
    import QueryDefaults._
    
    log(s"Querying for '$term'")
    
    val aggregatedResults: AggregatedRunQueryResponse = shrineClient.runQuery(topicId, outputTypes, QueryDefinition("scanner query", Term(term)))
    
    for {
      queryResult <- aggregatedResults.results.headOption
    } yield TermResult(aggregatedResults.queryId, term, queryResult.statusType, queryResult.setSize)
  }

  //TODO: Log4J, etc
  private def log(s: String) = println(s)
}

object Scanner {
  final object QueryDefaults {
    val topicId = "foo" //???
    val outputTypes = Set(ResultOutputType.PATIENT_COUNT_XML)
  }
  
  final case class TermResult(networkQueryId: Long, term: String, status: QueryResult.StatusType, count: Long)
  
  final case class ReQueryResults(stillNotFinished: Set[String], shouldHaveBeenMapped: Set[String] = Set.empty)
}