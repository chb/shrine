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
import net.shrine.protocol.query.QueryDefinition

/**
 * @author clint
 * @date Nov 8, 2012
 */
abstract class AbstractQueryRetrievalTestCase[R <: ShrineResponse](
    makeAdapter: AdapterDao => Adapter, 
    makeRequest: Long => ShrineRequest, 
    emptyResponse: Long => R,
    extractor: R => Option[(Long, Seq[QueryResult])]) extends AbstractDependencyInjectionSpringContextTests with AdapterDbTest with ShouldMatchersForJUnit {
  
  lazy val adapter = makeAdapter(dao)
  
  def doTestProcessInvalidRequest {
    intercept[ClassCastException] {
      //request must be a type of request we can handle
      adapter.processRequest(null, new BroadcastMessage(0L, new AbstractQueryRetrievalTestCase.BogusRequest))
    }
  }
  
  def doTestProcessRequest = afterCreatingTables {

    val shrineNetworkQueryId = 123L
    
    val errorResponse = ErrorResponse("Query with id '" + shrineNetworkQueryId + "' not found")
    
    def getResults = adapter.processRequest(null, new BroadcastMessage(shrineNetworkQueryId, makeRequest(shrineNetworkQueryId)))
    
    getResults should equal(errorResponse)
    
    val instanceId = 999L
    val setSize = 12345L
    val obfSetSize = setSize + 1
    val authn = AuthenticationInfo("some-domain", "some-user", Credential("alskdjlkasd", false))
    val queryExpr = Term("foo")
    
    val dbQueryId = dao.insertQuery(shrineNetworkQueryId, "some-query", authn, queryExpr)
    
    getResults should equal(errorResponse)
    
    import ResultOutputType._
    import Util.now
    
    val breakdowns = Map(PATIENT_AGE_COUNT_XML -> I2b2ResultEnvelope(PATIENT_AGE_COUNT_XML, Map("a" -> 1L, "b" -> 2L)),
    				     PATIENT_GENDER_COUNT_XML -> I2b2ResultEnvelope(PATIENT_GENDER_COUNT_XML, Map("x" -> 3L, "y" -> 4L)))
    				     
    val obfscBreakdowns = breakdowns.mapValues(_.mapValues(_ + 1))
    
    val countResult = QueryResult(456L, instanceId, Some(PATIENT_COUNT_XML), setSize, Option(now), Option(now), Some("results from node X"), QueryResult.StatusType.Finished.name, None, breakdowns)
    
    val breakdownResults = breakdowns.map { case (resultType, data) =>
      countResult.withBreakdowns(Map(resultType -> data)).withResultType(resultType)
    }.toSeq
    				     
    val idsByResultType = dao.insertQueryResults(
        dbQueryId, 
    	RunQueryResponse(
    	    shrineNetworkQueryId, 
    	    now, 
    		authn.username, 
    		authn.domain, 
    		QueryDefinition("some-query", queryExpr), 
    		instanceId,
    		countResult +: breakdownResults))
    		
    getResults should equal(emptyResponse(shrineNetworkQueryId))
    
    dao.insertCountResult(idsByResultType(PATIENT_COUNT_XML).head, setSize, obfSetSize)
    
    dao.insertBreakdownResults(idsByResultType, breakdowns, obfscBreakdowns)
    
    val result = getResults.asInstanceOf[R]
    
    val Some((actualNetworkQueryId, Seq(actualQueryResult))) = extractor(result)
    
    actualNetworkQueryId should equal(shrineNetworkQueryId)
    
    actualQueryResult.resultType should equal(Some(PATIENT_COUNT_XML))
    actualQueryResult.setSize should equal(obfSetSize)
    actualQueryResult.startDate should be(None) //TODO: This is probably wrong
    actualQueryResult.endDate should be(None) //TODO: This is probably wrong
    actualQueryResult.description should be(None) //TODO: This is probably wrong
    actualQueryResult.statusType should equal(QueryResult.StatusType.Finished.name)
    actualQueryResult.statusMessage should be(None)
    actualQueryResult.breakdowns should equal(obfscBreakdowns)
  }
}

object AbstractQueryRetrievalTestCase {
  final class BogusRequest extends ShrineRequest("fooProject", 1000L, null) {
    override protected def i2b2MessageBody: NodeSeq = <foo></foo>

    override def handle(handler: ShrineRequestHandler): ShrineResponse = null

    override val requestType: CRCRequestType = CRCRequestType.GetRequestXml

    override def toXml = <x></x>
  }
}