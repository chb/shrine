package net.shrine.utilities.scanner

import net.shrine.ont.data.OntologyDAO
import net.shrine.config.AdapterMappingsSource
import net.shrine.client.ShrineClient
import net.shrine.protocol.ResultOutputType
import net.shrine.protocol.query.QueryDefinition
import net.shrine.protocol.query.Term
import net.shrine.protocol.AggregatedRunQueryResponse
import net.shrine.protocol.QueryResult

/**
 * @author clint
 * @date Mar 5, 2013
 */
final class Scanner(ontologyDao: OntologyDAO, adapterMappingsSource: AdapterMappingsSource, shrineClient: ShrineClient) {
  import Scanner._
  
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
    
    def toTermSet(results: Set[StatusAndCount]): Set[String] = results.map(_.term)
    
    ScanResults(toTermSet(shouldHaveBeenMapped), toTermSet(shouldNotHaveBeenMapped), toTermSet(neverFinished))
  }
  
  //TODO: Don't go through a ShrineClient perhaps?  Hit adapter directly?
  
  private[scanner] def query(term: String): Option[StatusAndCount] = {
    val topicId = "foo" //???
    val outputTypes = Set(ResultOutputType.PATIENT_COUNT_XML)
    
    //TODO: Log4J, etc
    println(s"Querying for '$term'")
    
    val aggregatedResults: AggregatedRunQueryResponse = shrineClient.runQuery(topicId, outputTypes, QueryDefinition("scanner query", Term(term)))
    
    for {
      queryResult <- aggregatedResults.results.headOption
      if queryResult.statusType.isDone
    } yield StatusAndCount(term, queryResult.statusType, queryResult.setSize)
  }
}

object Scanner {
  final case class StatusAndCount(term: String, status: QueryResult.StatusType, count: Long)
}