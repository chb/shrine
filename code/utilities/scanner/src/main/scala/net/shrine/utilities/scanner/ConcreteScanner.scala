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
import net.shrine.util.Loggable
import com.google.inject.Inject

/**
 * @author clint
 * @date Mar 5, 2013
 */
final class ConcreteScanner @Inject() (ontologyDao: OntologyDAO, adapterMappingsSource: AdapterMappingsSource, shrineClient: ShrineClient, config: ScannerConfig) extends Scanner with Loggable {
  import ConcreteScanner._
  
  private def toTermSet(results: Set[TermResult]): Set[String] = results.map(_.term)
  
  override def scan(): ScanResults = time("Scanning")(info(_)) {
    info("Shrine Scanner starting")
    
    val mappedNetworkTerms = adapterMappingsSource.load.networkTerms
    
    def allShrineOntologyTerms = ontologyDao.ontologyEntries.map(_.path).toSet
    
    val termsExpectedToBeUnmapped = allShrineOntologyTerms -- mappedNetworkTerms
    
    info(s"We expect ${ mappedNetworkTerms.size } to be mapped, and ${ termsExpectedToBeUnmapped.size } to be unmapped.")
    
    val resultsForMappedTerms = mappedNetworkTerms.map(query)
    
    //Terms that we expected to BE mapped, but were NOT mapped
    val shouldHaveBeenMapped = resultsForMappedTerms.filter(_.status.isError)
    
    //Terms that we expected to NOT be mapped, but ARE mapped
    val shouldNotHaveBeenMapped = termsExpectedToBeUnmapped.map(query).filterNot(_.status.isError)
    
    //Terms that never completed after some timeout period
    val neverFinished = resultsForMappedTerms.filterNot(_.status.isDone)
    
    val reScanResults = reScan(neverFinished)

    val finalSouldHaveBeenMappedSet = toTermSet(shouldHaveBeenMapped) ++ reScanResults.shouldHaveBeenMapped
    
    ScanResults(finalSouldHaveBeenMappedSet, toTermSet(shouldNotHaveBeenMapped), reScanResults.stillNotFinished)
  }
  
  override def reScan(neverFinished: Set[TermResult]): ReScanResults = time("Re-scanning")(info(_)) {
    if(neverFinished.isEmpty) { ReScanResults.empty }
    else { 
      info(s"Sleeping for ${ config.reScanTimeout } before retreiving results for ${ neverFinished.size } incomplete queries...")
      
      Thread.sleep(config.reScanTimeout.toMillis)
      
      val (done, stillNotFinished) = neverFinished.map(attemptToRetrieve).partition(_.status.isDone)
      
      val errors = done.filter(_.status.isError)
      
      ReScanResults(toTermSet(stillNotFinished), toTermSet(errors))
    }
  }
  
  private[scanner] def attemptToRetrieve(termResult: TermResult): TermResult = {
    val aggregatedResults = shrineClient.readQueryResult(termResult.networkQueryId)
    
    info(s"Retrieving results for previously-incomplete query for '${ termResult.term }'")
    
    aggregatedResults.results.headOption match {
      case None => errorTermResult(aggregatedResults.queryId, termResult.term)
      case Some(queryResult) => TermResult(aggregatedResults.queryId, termResult.term, queryResult.statusType, queryResult.setSize)
    }
  }
  
  private[scanner] def query(term: String): TermResult = {
    import QueryDefaults._
    
    info(s"Querying for '$term'")
    
    val aggregatedResults: AggregatedRunQueryResponse = shrineClient.runQuery(topicId, outputTypes, QueryDefinition("scanner query", Term(term)))
    
    aggregatedResults.results.headOption match {
      case None => errorTermResult(aggregatedResults.queryId, term)
      case Some(queryResult) => TermResult(aggregatedResults.queryId, term, queryResult.statusType, queryResult.setSize)
    }
  }
  
  private def errorTermResult(networkQueryId: Long, term: String): TermResult = TermResult(networkQueryId, term, QueryResult.StatusType.Error, -1L)
  
  private def time[T](taskName: String)(log: String => Unit)(f: => T): T = {
    val start = System.currentTimeMillis
    
    try { f } finally { 
      val elapsed = System.currentTimeMillis - start
    
      log(s"$taskName took $elapsed milliseconds.")
    }
  }
}

object ConcreteScanner {
  final object QueryDefaults {
    val topicId = "foo" //???
    val outputTypes = Set(ResultOutputType.PATIENT_COUNT_XML)
  }
}