package net.shrine.adapter

import java.util.GregorianCalendar
import scala.Array.canBuildFrom
import org.junit.Test
import org.scalatest.junit.AssertionsForJUnit
import org.scalatest.junit.ShouldMatchersForJUnit
import org.spin.tools.NetworkTime
import org.spin.tools.crypto.signature.Identity
import junit.framework.TestCase
import net.shrine.adapter.dao.MockAdapterDao
import net.shrine.adapter.translators.ExpressionTranslator
import net.shrine.adapter.translators.QueryDefinitionTranslator
import net.shrine.config.HiveCredentials
import net.shrine.config.ShrineConfig
import net.shrine.protocol.AuthenticationInfo
import net.shrine.protocol.BroadcastMessage
import net.shrine.protocol.Credential
import net.shrine.protocol.I2b2ResultEnvelope
import net.shrine.protocol.QueryResult
import net.shrine.protocol.ReadResultRequest
import net.shrine.protocol.ReadResultResponse
import net.shrine.protocol.ResultOutputType
import net.shrine.protocol.ResultOutputType.PATIENT_AGE_COUNT_XML
import net.shrine.protocol.ResultOutputType.PATIENT_COUNT_XML
import net.shrine.protocol.ResultOutputType.PATIENT_GENDER_COUNT_XML
import net.shrine.protocol.ResultOutputType.PATIENT_RACE_COUNT_XML
import net.shrine.protocol.RunQueryRequest
import net.shrine.protocol.RunQueryResponse
import net.shrine.protocol.ShrineRequest
import net.shrine.protocol.query.OccuranceLimited
import net.shrine.protocol.query.Or
import net.shrine.protocol.query.QueryDefinition
import net.shrine.protocol.query.Term
import net.shrine.util.HttpClient
import net.shrine.protocol.RunQueryResponse
import net.shrine.protocol.RawCrcRunQueryResponse

/**
 * @author Bill Simons
 * @date 4/19/11
 * @link http://cbmi.med.harvard.edu
 * @link http://chip.org
 *       <p/>
 *       NOTICE: This software comes with NO guarantees whatsoever and is
 *       licensed as Lgpl Open Source
 * @link http://www.gnu.org/licenses/lgpl.html
 */
final class RunQueryAdapterTest extends TestCase with ShouldMatchersForJUnit {
  private val queryDef = QueryDefinition("foo", Term("foo"))

  private val broadcastMessageId = 1234563789L
  private val queryId = 123L
  private val masterId = 99L
  private val instanceId = 456L
  private val resultId = 42L
  private val projectId = "projectId"
  private val setSize = 17L
  private val xmlResultId = 98765L
  private val userId = "userId"
  private val groupId = "groupId"

  private val justCounts = Set(ResultOutputType.PATIENT_COUNT_XML)

  private val now = NetworkTime.makeXMLGregorianCalendar(new GregorianCalendar)
  
  private val countQueryResult = QueryResult(resultId, instanceId, Some(ResultOutputType.PATIENT_COUNT_XML), setSize, Some(now), Some(now), None, QueryResult.StatusType.Finished.name, None)
  
  private val dummyBreakdownData = Map("x" -> 1L, "y" -> 2L, "z" -> 3L)
  
  private val hiveCredentials = HiveCredentials("some-hive-domain", "hive-username", "hive-password", "hive-project")

  @Test
  def testObfuscateBreakdowns {
    import ResultOutputType._
    
    val breakdown1 = I2b2ResultEnvelope(PATIENT_AGE_COUNT_XML, Map.empty)
    val breakdown2 = I2b2ResultEnvelope(PATIENT_GENDER_COUNT_XML, Map("foo" -> 123, "bar" -> 345))
    val breakdown3 = I2b2ResultEnvelope(PATIENT_RACE_COUNT_XML, Map("x" -> 999, "y" -> 888))
    
    val original = Map.empty ++ Seq(breakdown1, breakdown2, breakdown3).map(env => (env.resultType, env))
    
    val obfuscated = RunQueryAdapter.obfuscateBreakdowns(original)
    
    original.keySet should equal(obfuscated.keySet)
    
    original.keySet.forall(resultType => original(resultType).data.keySet == obfuscated(resultType).data.keySet) should be(true)
    
    for {
      (resultType, origBreakdown) <- original
      obfscBreakdown <- obfuscated.get(resultType)
      key <- origBreakdown.data.keySet
    } {
      (origBreakdown eq obfscBreakdown) should be(false)
      
      ObfuscatorTest.within3(origBreakdown.data(key), obfscBreakdown.data(key)) should be(true)
    }
  }
  
  @Test
  def testTranslateQueryDefinitionXml {
    val localTerms = Set("local1a", "local1b")

    val mappings = Map("network" -> localTerms)

    val translator = new QueryDefinitionTranslator(new ExpressionTranslator(mappings))

    val adapter = new RunQueryAdapter("crc-url", MockHttpClient, null, null, translator, null, true)

    val queryDefinition = QueryDefinition("10-17 years old@14:39:20", OccuranceLimited(1, Term("network")))

    val newDef = adapter.conceptTranslator.translate(queryDefinition)

    val expected = QueryDefinition("10-17 years old@14:39:20", OccuranceLimited(1, Or(Term("local1a"), Term("local1b"))))

    newDef should equal(expected)
  }

  @Test
  def testRegularCountQuery {
    val outputTypes = justCounts

    val resp = doQuery(outputTypes) {
      import RawCrcRunQueryResponse.toQueryResultMap
      
      RawCrcRunQueryResponse(queryId, now, userId, groupId, queryDef, instanceId, toQueryResultMap(Seq(countQueryResult))).toI2b2String
    }

    dobasicRunQueryResponseTest(resp)

    val firstResult = resp.results.head

    //TODO, NB: hard to compare QueryResults directly due to ID translation; use simpler test here once translation is no longer performed
    firstResult.setSize should equal(countQueryResult.setSize)
    firstResult.breakdowns.isEmpty should equal(true)
    firstResult.resultType should equal(Some(ResultOutputType.PATIENT_COUNT_XML))
    firstResult.description should equal(None)

    resp.results.size should equal(1)
  }

  @Test
  def testGetBreakdownsWithRegularCountQuery {
    val breakdowns = ResultOutputType.breakdownTypes.map(breakdownFor).iterator
    
    val resp = doTestGetBreakdowns(breakdowns)

    import ResultOutputType._

    val firstResult = resp.results.head

    //TODO, NB: hard to compare QueryResults directly due to ID translation; use simpler test here once translation is no longer performed
    firstResult.resultType should equal(Some(PATIENT_COUNT_XML))
    firstResult.setSize should equal(setSize)
    firstResult.description should equal(None)
    firstResult.breakdowns.keySet should equal(ResultOutputType.breakdownTypes.toSet)
    firstResult.breakdowns.values.map(_.data).foreach(_ should equal(dummyBreakdownData))

    resp.results.size should equal(1)
  }

  @Test
  def testGetBreakdownsSomeFailures {
    import ResultOutputType._
    
    val resultTypesExpectedToSucceed = Set(PATIENT_AGE_COUNT_XML, PATIENT_GENDER_COUNT_XML)
    val resultTypesExpectedTofail = ResultOutputType.breakdownTypes.toSet -- resultTypesExpectedToSucceed
    
    val breakdowns = resultTypesExpectedToSucceed.toSeq.map(breakdownFor).iterator
    
    val resp = doTestGetBreakdowns(breakdowns)

    val firstResult = resp.results.head

    //TODO, NB: hard to compare QueryResults directly due to ID translation; use simpler test here once translation is no longer performed
    firstResult.resultType should equal(Some(PATIENT_COUNT_XML))
    firstResult.setSize should equal(setSize)
    firstResult.description should equal(None)
    firstResult.breakdowns.keySet should equal(resultTypesExpectedToSucceed)
    firstResult.breakdowns.values.map(_.data).foreach(_ should equal(dummyBreakdownData)) 

    resp.results.size should equal(1)    


  }
  
  private def breakdownFor(resultType: ResultOutputType) = I2b2ResultEnvelope(resultType, dummyBreakdownData)
  
  private def doTestGetBreakdowns(successfulBreakdowns: Iterator[I2b2ResultEnvelope]): RunQueryResponse = {
    val outputTypes = justCounts ++ ResultOutputType.breakdownTypes

    val resp = doQueryThatReturnsSpecifiedBreakdowns(countQueryResult, outputTypes, successfulBreakdowns)

    dobasicRunQueryResponseTest(resp)
    
    resp
  }

  private def dobasicRunQueryResponseTest(resp: RunQueryResponse) {
    resp.createDate should equal(now)
    resp.groupId should equal(groupId)
    //TODO: re-enable these once ID translation is no longer performed
    //resp.queryId should equal(queryId)
    //resp.queryInstanceId should equal(instanceId)
    resp.queryName should equal(queryDef.name)
    resp.requestXml should equal(queryDef)
  }

  private def doQueryThatReturnsSpecifiedBreakdowns(countQueryResult: QueryResult, outputTypes: Set[ResultOutputType], successfulBreakdowns: Iterator[I2b2ResultEnvelope]): RunQueryResponse = {
    val breakdownQueryResults = ResultOutputType.breakdownTypes.zipWithIndex.map {
      case (rt, i) =>
        countQueryResult.withId(resultId + i + 1).withResultType(rt)
    }

    doQuery(outputTypes, new HttpClient {
      override def post(input: String, url: String): String = {
        val resp = ShrineRequest.fromI2b2(input) match {
          case req: RunQueryRequest => {
            //NB: Terms should be translated
            req.queryDefinition.expr should equal(Term("bar"))
            
            //Credentials should be "translated"
            req.authn.username should equal(hiveCredentials.username)
            req.authn.domain should equal(hiveCredentials.domain)
            
            //I2b2 Project ID should be translated 
            req.projectId should equal(hiveCredentials.project)
            
            val queryResultMap = RawCrcRunQueryResponse.toQueryResultMap(countQueryResult +: breakdownQueryResults)
            
            val result = RawCrcRunQueryResponse(queryId, now, "userId", "groupId", queryDef, instanceId, queryResultMap)
            
            result
          }
          //NB: return a ReadResultResponse with new breakdown data each time, but will throw if the successfulBreakdowns
          //iterator is exhausted, simulating an error calling the CRC 
          case req: ReadResultRequest => ReadResultResponse(xmlResultId, countQueryResult, successfulBreakdowns.next())
        }

        resp.toI2b2String
      }
    })
  }

  private def doQuery(outputTypes: Set[ResultOutputType])(i2b2XmlToReturn: => String): RunQueryResponse = {
    doQuery(outputTypes, MockHttpClient(i2b2XmlToReturn))
  }

  private def doQuery(outputTypes: Set[ResultOutputType], httpClient: HttpClient): RunQueryResponse = {
    val identity = new Identity("some-domain", "username")

    val authn = AuthenticationInfo("some-domain", "username", Credential("jksafhkjaf", false))

    val translator = new QueryDefinitionTranslator(new ExpressionTranslator(Map("foo" -> Set("bar"))))

    //NB: Don't obfuscate, for simpler testing
    val adapter = new RunQueryAdapter("crc-url", httpClient, MockAdapterDao, hiveCredentials, translator, new ShrineConfig, false)

    val req = new RunQueryRequest(projectId, 1000L, authn, 999L, "topicId", outputTypes, queryDef)

    //val broadcastMessage = new BroadcastMessage(queryId, masterId, instanceId, Seq(resultId), req)
    val broadcastMessage = BroadcastMessage(queryId, req)

    adapter.processRequest(identity, broadcastMessage)
  }
}