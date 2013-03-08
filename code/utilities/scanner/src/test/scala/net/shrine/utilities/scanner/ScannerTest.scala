package net.shrine.utilities.scanner

import junit.framework.TestCase
import org.scalatest.junit.ShouldMatchersForJUnit
import org.junit.Test
import net.shrine.client.ShrineClient
import net.shrine.protocol.ResultOutputType
import net.shrine.protocol.AggregatedRunQueryResponse
import net.shrine.protocol.AggregatedReadQueryResultResponse
import net.shrine.protocol.QueryResult
import net.shrine.protocol.query.QueryDefinition
import net.shrine.protocol.query.Term
import scala.util.Random
import net.shrine.util.Util
import net.shrine.config.AdapterMappings
import net.shrine.config.AdapterMappingsSource
import net.shrine.ont.data.OntologyDAO
import net.shrine.ont.messaging.Concept

/**
 * @author clint
 * @date Mar 7, 2013
 */
final class ScannerTest extends TestCase with ShouldMatchersForJUnit {
  import ScannerTest._
  
  private val mappings = Map("network1" -> Set("local1"), "network2" -> Set("local2", "local3"))
  
  private val terms = Set("foo", "bar", "baz")
  
  import scala.concurrent.duration._
  
  @Test
  def testScanNoErrorsNoRescanning {
    val scanner = new Scanner {
      override val reScanTimeout = 0.seconds
      override val adapterMappingsSource = literalAdapterMappingsSource(mappings)
      override val ontologyDao = literalOntologyDao(terms)
      override val shrineClient = AllQueriesCompleteShrineClient
    }
    
    val scanResults = scanner.scan()
    
    scanResults should not be(null)
    scanResults.neverFinished.isEmpty should be(true)
    scanResults.shouldNotHaveBeenMapped should be(terms)
    scanResults.shouldHaveBeenMapped.isEmpty should be(true)
  }
  
  @Test
  def testScanAllErrorsNoRescanning {
    val scanner = new Scanner {
      override val reScanTimeout = 0.seconds
      override val adapterMappingsSource = literalAdapterMappingsSource(mappings)
      override val ontologyDao = literalOntologyDao(terms)
      override val shrineClient = AllQueriesErrorShrineClient
    }
    
    val scanResults = scanner.scan()
    
    scanResults should not be(null)
    scanResults.neverFinished.isEmpty should be(true)
    scanResults.shouldNotHaveBeenMapped.isEmpty should be(true)
    scanResults.shouldHaveBeenMapped should be(mappings.keySet)
  }
  
  @Test
  def testScanSomeProblemsNoRescanning {
    val scanner = new Scanner {
      override val reScanTimeout = 0.seconds
      override val adapterMappingsSource = literalAdapterMappingsSource(mappings)
      override val ontologyDao = literalOntologyDao(terms)
      override val shrineClient = someQueriesWorkShrineClient(Set("network1", "foo"), Set("network2", "bar", "baz"), Set.empty)
    }
    
    val scanResults = scanner.scan()
    
    scanResults should not be(null)
    scanResults.neverFinished.isEmpty should be(true)
    scanResults.shouldNotHaveBeenMapped should be(Set("foo"))
    scanResults.shouldHaveBeenMapped should be(Set("network2"))
  }
  
  @Test
  def testScanSomeProblemsRescanningSucceeds {
    val scanner = new Scanner {
      override val reScanTimeout = 0.seconds
      override val adapterMappingsSource = literalAdapterMappingsSource(mappings)
      override val ontologyDao = literalOntologyDao(terms)
      override val shrineClient = someQueriesWorkShrineClient(Set.empty, Set("network2", "bar", "baz"), Set.empty, Set("network1", "foo"))
    }
    
    val scanResults = scanner.scan()
    
    scanResults should not be(null)
    scanResults.neverFinished.isEmpty should be(true)
    scanResults.shouldNotHaveBeenMapped should be(Set("foo"))
    scanResults.shouldHaveBeenMapped should be(Set("network2"))
  }
  
  @Test
  def testScanSomeProblemsRescanningDoesntGetResults {
    val scanner = new Scanner {
      override val reScanTimeout = 0.seconds
      override val adapterMappingsSource = literalAdapterMappingsSource(mappings)
      override val ontologyDao = literalOntologyDao(terms)
      override val shrineClient = someQueriesWorkShrineClient(Set("bar"), Set("network2", "baz"), Set("network1", "foo"))
    }
    
    val scanResults = scanner.scan()
    
    scanResults should not be(null)
    scanResults.neverFinished should be(Set("network1", "foo"))
    scanResults.shouldNotHaveBeenMapped should be(Set("bar"))
    scanResults.shouldHaveBeenMapped should be(Set("network2"))
  }
}

object ScannerTest {
  private val random = new Random
  
  private def literalOntologyDao(terms: Iterable[String]): OntologyDAO = new OntologyDAO {
    override val ontologyEntries = terms.map(t => Concept(t, None, None))
  }
  
  private def literalAdapterMappingsSource(mappings: Map[String, Set[String]]): AdapterMappingsSource = new AdapterMappingsSource {
    override val load = new AdapterMappings(mappings) 
  }
  
  import QueryResult.StatusType
  
  private object AllQueriesCompleteShrineClient extends ShrineClientAdapter {
    override def runQuery(topicId: String, outputTypes: Set[ResultOutputType], queryDefinition: QueryDefinition): AggregatedRunQueryResponse = {
      aggregatedRunQueryResponse(random.nextLong, queryDefinition, StatusType.Finished)
    }
  }
  
  private object AllQueriesErrorShrineClient extends ShrineClientAdapter {
    override def runQuery(topicId: String, outputTypes: Set[ResultOutputType], queryDefinition: QueryDefinition): AggregatedRunQueryResponse = {
      aggregatedRunQueryResponse(random.nextLong, queryDefinition, StatusType.Error)
    }
  }
  
  private def someQueriesWorkShrineClient(termsThatShouldWork: Set[String], termsThatShouldNotWork: Set[String], termsThatShouldNeverFinish: Set[String], termsThatShouldFinishAfter1Retry: Set[String] = Set.empty): ShrineClient = new ShrineClientAdapter {
    var timedOutTerms = Map.empty[Long, String]
    
    override def runQuery(topicId: String, outputTypes: Set[ResultOutputType], queryDefinition: QueryDefinition): AggregatedRunQueryResponse = {
      val Term(term) = queryDefinition.expr
      
      val queryId = random.nextLong
      
      val statusType = if(termsThatShouldWork.contains(term)) { StatusType.Finished }
      				   else if(termsThatShouldNotWork.contains(term)) { StatusType.Error }
      				   else {
      				     if(termsThatShouldFinishAfter1Retry.contains(term)) {
      				       timedOutTerms += (queryId -> term)
      				     }
      				     
      				     StatusType.Processing 
      				   }
      
      aggregatedRunQueryResponse(queryId, queryDefinition, statusType)
    }
    
    override def readQueryResult(queryId: Long): AggregatedReadQueryResultResponse = {
      val status = timedOutTerms.get(queryId) match {
        case Some(_) => StatusType.Finished
        case None => StatusType.Processing
      }
      
      AggregatedReadQueryResultResponse(queryId, Seq(queryResult(status)))
    }
  } 
  
  private def queryResult(status: QueryResult.StatusType): QueryResult = {
    val resultType = if(status.isError) None else Some(ResultOutputType.PATIENT_COUNT_XML)
    
    QueryResult(random.nextLong, random.nextLong, resultType, 99, Some(Util.now), Some(Util.now), None, status, None)
  }
  
  private def aggregatedRunQueryResponse(queryId: Long, queryDefinition: QueryDefinition, status: QueryResult.StatusType): AggregatedRunQueryResponse = {
    AggregatedRunQueryResponse(queryId, Util.now, "some-userId", "some-groupId", queryDefinition, random.nextLong, Seq(queryResult(status)))
  }
}
