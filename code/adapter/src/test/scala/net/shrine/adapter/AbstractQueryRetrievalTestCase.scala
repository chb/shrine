package net.shrine.adapter

import scala.xml.NodeSeq
import org.scalatest.junit.ShouldMatchersForJUnit
import org.springframework.test.AbstractDependencyInjectionSpringContextTests
import ObfuscatorTest.within3
import javax.xml.datatype.XMLGregorianCalendar
import net.shrine.adapter.dao.AdapterDao
import net.shrine.config.HiveCredentials
import net.shrine.protocol.AuthenticationInfo
import net.shrine.protocol.BroadcastMessage
import net.shrine.protocol.Credential
import net.shrine.protocol.ErrorResponse
import net.shrine.protocol.I2b2ResultEnvelope
import net.shrine.protocol.QueryResult
import net.shrine.protocol.ReadInstanceResultsRequest
import net.shrine.protocol.ReadInstanceResultsResponse
import net.shrine.protocol.ReadResultRequest
import net.shrine.protocol.ReadResultResponse
import net.shrine.protocol.ResultOutputType.PATIENT_AGE_COUNT_XML
import net.shrine.protocol.ResultOutputType.PATIENT_COUNT_XML
import net.shrine.protocol.ResultOutputType.PATIENT_GENDER_COUNT_XML
import net.shrine.protocol.ShrineRequest
import net.shrine.protocol.ShrineResponse
import net.shrine.protocol.query.Term
import net.shrine.util.HttpClient
import net.shrine.util.Util.now
import net.shrine.util.XmlGcEnrichments.EnrichedLong
import net.shrine.util.XmlGcEnrichments.EnrichedXmlGc
import net.shrine.protocol.ResultOutputType
import net.shrine.util.Util
import net.shrine.util.XmlGcEnrichments
import net.shrine.protocol.CrcRequest

/**
 * @author clint
 * @date Nov 8, 2012
 */
abstract class AbstractQueryRetrievalTestCase[R <: ShrineResponse](
    makeAdapter: (AdapterDao, HttpClient) => Adapter, 
    makeRequest: (Long, AuthenticationInfo) => ShrineRequest, 
    extractor: R => Option[(Long, QueryResult)]) extends AbstractDependencyInjectionSpringContextTests with AdapterDbTest with ShouldMatchersForJUnit {
  
  private val authn = AuthenticationInfo("some-domain", "some-user", Credential("alskdjlkasd", false))
  
  def doTestProcessRequestMissingQuery {
    val adapter = makeAdapter(dao, MockHttpClient)
    
    val response = adapter.processRequest(null, BroadcastMessage(0L, makeRequest(-1L, authn)))
    
    response.isInstanceOf[ErrorResponse] should be(true)
  }
  
  def doTestProcessInvalidRequest {
    val adapter = makeAdapter(dao, MockHttpClient)
    
    intercept[ClassCastException] {
      //request must be a type of request we can handle
      adapter.processRequest(null, BroadcastMessage(0L, new AbstractQueryRetrievalTestCase.BogusRequest))
    }
  }
  
  private val localMasterId = "alksjdkalsdjlasdjlkjsad"
    
  private val shrineNetworkQueryId = 123L
    
  private val errorResponse = ErrorResponse("Query with id '" + shrineNetworkQueryId + "' not found")
  
  private def doGetResults(adapter: Adapter) = adapter.processRequest(null, BroadcastMessage(shrineNetworkQueryId, makeRequest(shrineNetworkQueryId, authn)))
  
  private def toMillis(xmlGc: XMLGregorianCalendar): Long = xmlGc.toGregorianCalendar.getTimeInMillis
  
  private val instanceId = 999L
  private val setSize = 12345L
  private val obfSetSize = setSize + 1
  private val queryExpr = Term("foo")
  
  def doTestProcessRequestIncompleteQuery = afterCreatingTables {
    
    val dbQueryId = dao.insertQuery(localMasterId, shrineNetworkQueryId, "some-query", authn, queryExpr)
    
    import ResultOutputType._
    import Util.now
    
    val breakdowns = Map(PATIENT_AGE_COUNT_XML -> I2b2ResultEnvelope(PATIENT_AGE_COUNT_XML, Map("a" -> 1L, "b" -> 2L)))
    				     
    val obfscBreakdowns = breakdowns.mapValues(_.mapValues(_ + 1))
    
    val startDate = now
    val elapsed = 100L
    
    val endDate = {
      import XmlGcEnrichments._
      
      startDate + elapsed.milliseconds
    }
    
    val incompleteCountResult = QueryResult(456L, instanceId, Some(PATIENT_COUNT_XML), setSize, Option(startDate), Option(endDate), Some("results from node X"), QueryResult.StatusType.Processing, None, breakdowns)
    
    val breakdownResult = breakdowns.head match { 
      case (resultType, data) => incompleteCountResult.withBreakdowns(Map(resultType -> data)).withResultType(resultType)
    }
    
    val queryStartDate = now
    				     
    val idsByResultType = dao.insertQueryResults(dbQueryId, incompleteCountResult :: breakdownResult :: Nil)
    
    final class AlwaysWorksMockHttpClient extends HttpClient {
      override def post(input: String, url: String): String = {
        val response = CrcRequest.fromI2b2(input) match {
          case req: ReadInstanceResultsRequest => {
            ReadInstanceResultsResponse(shrineNetworkQueryId, incompleteCountResult.copy(statusType = QueryResult.StatusType.Finished))
          }
          case req: ReadResultRequest => {
            ReadResultResponse(123L, breakdownResult, breakdowns.head._2)
          }
          case _ => sys.error("Unknown input: " + input)
        }
        
        response.toI2b2String
      }
    }
    
    val adapter = makeAdapter(dao, new AlwaysWorksMockHttpClient)
    
    def getResults = doGetResults(adapter)
    
    getResults.isInstanceOf[ErrorResponse] should be(true)
    
    dao.insertCountResult(idsByResultType(PATIENT_COUNT_XML).head, setSize, obfSetSize)
    
    dao.insertBreakdownResults(idsByResultType, breakdowns, obfscBreakdowns)
    
    //The query shouldn't be 'done', since its status is PROCESSING
    dao.findResultsFor(shrineNetworkQueryId).get.isDone should be(false)
    
    //Now, calling processRequest (via getResults) should cause the query to be re-retrieved from the CRC

    val result = getResults.asInstanceOf[R]
    
    //Which should casue the query to be re-stored with a 'done' status (since that's what our mock CRC returns)
    
    println(dao.findResultsFor(shrineNetworkQueryId).get)
    
    dao.findResultsFor(shrineNetworkQueryId).get.isDone should be(true)
    
    val Some((actualNetworkQueryId, actualQueryResult)) = extractor(result)
    
    actualNetworkQueryId should equal(shrineNetworkQueryId)
    
    import ObfuscatorTest.within3
    
    actualQueryResult.resultType should equal(Some(PATIENT_COUNT_XML))
    within3(setSize, actualQueryResult.setSize) should be(true)
    actualQueryResult.description should be(None) //TODO: This is probably wrong
    actualQueryResult.statusType should equal(QueryResult.StatusType.Finished)
    actualQueryResult.statusMessage should be(None)
    actualQueryResult.breakdowns.foreach { 
      case (rt, I2b2ResultEnvelope(_, data)) => {
        data.forall { case (key, value) => within3(value, breakdowns.get(rt).get.data.get(key).get) } 
      }
    }
    
    for {
      startDate <- actualQueryResult.startDate
      endDate <- actualQueryResult.endDate
    } {
      val actualElapsed = toMillis(endDate) - toMillis(startDate)
      
      actualElapsed should equal(elapsed)
    }
  }
  
  def doTestProcessRequest = afterCreatingTables {

    val adapter = makeAdapter(dao, MockHttpClient)
    
    def getResults = doGetResults(adapter)
    
    getResults should equal(errorResponse)
    
    val dbQueryId = dao.insertQuery(localMasterId, shrineNetworkQueryId, "some-query", authn, queryExpr)
    
    getResults should equal(errorResponse)
    
    import ResultOutputType._
    import Util.now
    
    val breakdowns = Map(PATIENT_AGE_COUNT_XML -> I2b2ResultEnvelope(PATIENT_AGE_COUNT_XML, Map("a" -> 1L, "b" -> 2L)),
    			 PATIENT_GENDER_COUNT_XML -> I2b2ResultEnvelope(PATIENT_GENDER_COUNT_XML, Map("x" -> 3L, "y" -> 4L)))
    				     
    val obfscBreakdowns = breakdowns.mapValues(_.mapValues(_ + 1))
    
    val startDate = now
    val elapsed = 100L
    
    val endDate = {
      import XmlGcEnrichments._
      
      startDate + elapsed.milliseconds
    }
    
    val countResult = QueryResult(456L, instanceId, Some(PATIENT_COUNT_XML), setSize, Option(startDate), Option(endDate), Some("results from node X"), QueryResult.StatusType.Finished, None, breakdowns)
    
    val breakdownResults = breakdowns.map { case (resultType, data) =>
      countResult.withBreakdowns(Map(resultType -> data)).withResultType(resultType)
    }.toSeq
    
    val queryStartDate = now
    				     
    val idsByResultType = dao.insertQueryResults(dbQueryId, countResult +: breakdownResults)
    		
    getResults.isInstanceOf[ErrorResponse] should be(true)
    
    dao.insertCountResult(idsByResultType(PATIENT_COUNT_XML).head, setSize, obfSetSize)
    
    dao.insertBreakdownResults(idsByResultType, breakdowns, obfscBreakdowns)
    
    val result = getResults.asInstanceOf[R]
    
    val Some((actualNetworkQueryId, actualQueryResult)) = extractor(result)
    
    actualNetworkQueryId should equal(shrineNetworkQueryId)
    
    actualQueryResult.resultType should equal(Some(PATIENT_COUNT_XML))
    actualQueryResult.setSize should equal(obfSetSize)
    actualQueryResult.description should be(None) //TODO: This is probably wrong
    actualQueryResult.statusType should equal(QueryResult.StatusType.Finished)
    actualQueryResult.statusMessage should be(None)
    actualQueryResult.breakdowns should equal(obfscBreakdowns)
    
    for {
      startDate <- actualQueryResult.startDate
      endDate <- actualQueryResult.endDate
    } {
      val actualElapsed = toMillis(endDate) - toMillis(startDate)
      
      actualElapsed should equal(elapsed)
    }
  }
}

object AbstractQueryRetrievalTestCase {
  val hiveCredentials = HiveCredentials("some-hive-domain", "hive-username", "hive-password", "hive-project")
  
  final class BogusRequest extends ShrineRequest("fooProject", 1000L, null) {
    override val requestType = null 
    
    protected override def i2b2MessageBody: NodeSeq = <foo></foo>

    override def toXml = <x></x>
  }
}