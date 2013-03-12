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
import net.shrine.util.Util.time

/**
 * @author clint
 * @date Mar 5, 2013
 */
trait Scanner extends Loggable {
  
  //All of our dependencies are specified as abstract vals
  val reScanTimeout: Duration
  val adapterMappingsSource: AdapterMappingsSource
  val ontologyDao: OntologyDAO
  val shrineClient: ShrineClient
  
  def scan(): ScanResults = time("Scanning")(info(_)) {
    info("Shrine Scanner starting")
    
    val mappedNetworkTerms = adapterMappingsSource.load.networkTerms
    
    def allShrineOntologyTerms = ontologyDao.ontologyEntries.map(_.path).toSet
    
    val termsExpectedToBeUnmapped = allShrineOntologyTerms -- mappedNetworkTerms
    
    info(s"We expect ${ mappedNetworkTerms.size } to be mapped, and ${ termsExpectedToBeUnmapped.size } to be unmapped.")
    
    doScan(mappedNetworkTerms, termsExpectedToBeUnmapped)
  }
  
  private def doScan(mappedNetworkTerms: Set[String], termsExpectedToBeUnmapped: Set[String]): ScanResults = time("Scanning")(info(_)) {
    val resultsForMappedTerms = mappedNetworkTerms.map(query)
    
    val resultsForUnMappedTerms = termsExpectedToBeUnmapped.map(query)
    
    //Split query results into those that completed on the first try, and those that didn't
    val (finishedAndShouldHaveBeenMapped, didntFinishAndShouldHaveBeenMapped) = resultsForMappedTerms.partition(_.status.isDone)
    
    val (finishedAndShouldNotHaveBeenMapped, didntFinishAndShouldNotHaveBeenMapped) = resultsForUnMappedTerms.partition(_.status.isDone)
    
    //Terms that we expected to BE mapped, but were NOT mapped
    val shouldHaveBeenMapped = finishedAndShouldHaveBeenMapped.filter(_.status.isError)
    
    //Terms that we expected to NOT be mapped, but ARE mapped
    val shouldNotHaveBeenMapped = finishedAndShouldNotHaveBeenMapped.filterNot(_.status.isError)
    
    val reScanResults = reScan(didntFinishAndShouldHaveBeenMapped, didntFinishAndShouldNotHaveBeenMapped)

    val finalSouldHaveBeenMappedSet = toTermSet(shouldHaveBeenMapped) ++ reScanResults.shouldHaveBeenMapped
    
    val finalSouldNotHaveBeenMappedSet = toTermSet(shouldNotHaveBeenMapped) ++ reScanResults.shouldNotHaveBeenMapped
    
    ScanResults(finalSouldHaveBeenMappedSet, finalSouldNotHaveBeenMappedSet, reScanResults.neverFinished)
  }
  
  private def reScan(neverFinishedShouldHaveBeenMapped: Set[TermResult], neverFinishedShouldNotHaveBeenMapped: Set[TermResult]): ScanResults = time("Re-scanning")(info(_)) {
    if(neverFinishedShouldHaveBeenMapped.isEmpty && neverFinishedShouldNotHaveBeenMapped.isEmpty) { ScanResults.empty }
    else { 
      val total = neverFinishedShouldNotHaveBeenMapped.size + neverFinishedShouldNotHaveBeenMapped.size
      
      info(s"Sleeping for ${ reScanTimeout } before retreiving results for $total incomplete queries...")
      
      Thread.sleep(reScanTimeout.toMillis)
      
      val (doneShouldHaveBeenMapped, stillNotFinishedShouldHaveBeenMapped) = neverFinishedShouldHaveBeenMapped.map(attemptToRetrieve).partition(_.status.isDone)
      
      val (doneShouldNotHaveBeenMapped, stillNotFinishedShouldNotHaveBeenMapped) = neverFinishedShouldNotHaveBeenMapped.map(attemptToRetrieve).partition(_.status.isDone)
      
      val shouldHaveBeenMapped = doneShouldHaveBeenMapped.filter(_.status.isError)
      
      val shouldNotHaveBeenMapped = doneShouldNotHaveBeenMapped.filterNot(_.status.isError)
      
      val stillNotFinished = stillNotFinishedShouldHaveBeenMapped ++ stillNotFinishedShouldNotHaveBeenMapped
      
      ScanResults(toTermSet(shouldHaveBeenMapped), toTermSet(shouldNotHaveBeenMapped), toTermSet(stillNotFinished))
    }
  }
  
  private val shouldBroadcast = false
  
  private[scanner] def attemptToRetrieve(termResult: TermResult): TermResult = {
    val aggregatedResults = shrineClient.readQueryResult(termResult.networkQueryId, shouldBroadcast)
    
    info(s"Retrieving results for previously-incomplete query for '${ termResult.term }'")
    
    aggregatedResults.results.headOption match {
      case None => errorTermResult(aggregatedResults.queryId, termResult.term)
      case Some(queryResult) => TermResult(aggregatedResults.queryId, termResult.term, queryResult.statusType, queryResult.setSize)
    }
  }
  
  private[scanner] def query(term: String): TermResult = {
    import Scanner.QueryDefaults._
    
    info(s"Querying for '$term'")
    
    val aggregatedResults: AggregatedRunQueryResponse = shrineClient.runQuery(topicId, outputTypes, QueryDefinition("scanner query", Term(term)), shouldBroadcast)
    
    aggregatedResults.results.headOption match {
      case None => errorTermResult(aggregatedResults.queryId, term)
      case Some(queryResult) => TermResult(aggregatedResults.queryId, term, queryResult.statusType, queryResult.setSize)
    }
  }
  
  private def toTermSet(results: Set[TermResult]): Set[String] = results.map(_.term)
  
  private def errorTermResult(networkQueryId: Long, term: String): TermResult = TermResult(networkQueryId, term, QueryResult.StatusType.Error, -1L)
}

object Scanner {
  final object QueryDefaults {
    val topicId = "foo" //???
    val outputTypes = Set(ResultOutputType.PATIENT_COUNT_XML)
  }
}