package net.shrine.adapter

import org.springframework.test.AbstractDependencyInjectionSpringContextTests
import org.scalatest.junit.ShouldMatchersForJUnit
import net.shrine.protocol.QueryResult
import scala.xml.NodeSeq
import net.shrine.protocol.ShrineRequestHandler
import net.shrine.protocol.ShrineRequest
import net.shrine.protocol.CRCRequestType
import net.shrine.protocol.ShrineResponse
import net.shrine.protocol.BroadcastMessage
import org.spin.tools.crypto.signature.Identity
import net.shrine.serialization.XmlMarshaller
import net.shrine.adapter.dao.AdapterDao
import org.junit.Test
import net.shrine.protocol.ErrorResponse
import net.shrine.protocol.AuthenticationInfo
import net.shrine.protocol.Credential
import net.shrine.protocol.query.Term
import net.shrine.protocol.ResultOutputType
import net.shrine.util.Util
import net.shrine.protocol.I2b2ResultEnvelope
import net.shrine.protocol.RunQueryResponse
import net.shrine.protocol.RawCrcRunQueryResponse
import net.shrine.protocol.query.QueryDefinition
import net.shrine.config.HiveCredentials
import javax.xml.datatype.XMLGregorianCalendar
import net.shrine.util.XmlGcEnrichments

/**
 * @author clint
 * @date Nov 8, 2012
 */
abstract class AbstractQueryRetrievalTestCase[R <: ShrineResponse](
    makeAdapter: AdapterDao => Adapter, 
    makeRequest: Long => ShrineRequest, 
    extractor: R => Option[(Long, QueryResult)]) extends AbstractDependencyInjectionSpringContextTests with AdapterDbTest with ShouldMatchersForJUnit {
  
  lazy val adapter = makeAdapter(dao)
  
  def doTestProcessInvalidRequest {
    intercept[ClassCastException] {
      //request must be a type of request we can handle
      adapter.processRequest(null, new BroadcastMessage(0L, new AbstractQueryRetrievalTestCase.BogusRequest))
    }
  }
  
  def doTestProcessRequest = afterCreatingTables {

    val localMasterId = "alksjdkalsdjlasdjlkjsad"
    
    val shrineNetworkQueryId = 123L
    
    val errorResponse = ErrorResponse("Query with id '" + shrineNetworkQueryId + "' not found")
    
    def getResults = adapter.processRequest(null, new BroadcastMessage(shrineNetworkQueryId, makeRequest(shrineNetworkQueryId)))
    
    getResults should equal(errorResponse)
    
    val instanceId = 999L
    val setSize = 12345L
    val obfSetSize = setSize + 1
    val authn = AuthenticationInfo("some-domain", "some-user", Credential("alskdjlkasd", false))
    val queryExpr = Term("foo")
    
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
    
    def toMillis(xmlGc: XMLGregorianCalendar): Long = xmlGc.toGregorianCalendar.getTimeInMillis
    
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
    override protected def i2b2MessageBody: NodeSeq = <foo></foo>

    override def handle(handler: ShrineRequestHandler): ShrineResponse = null

    override val requestType: CRCRequestType = CRCRequestType.GetRequestXml

    override def toXml = <x></x>
  }
}