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
import org.springframework.test.AbstractDependencyInjectionSpringContextTests
import net.shrine.adapter.dao.AdapterDao
import org.scalaquery.session.Session
import net.shrine.adapter.dao.scalaquery.tables.ShrineQueries
import net.shrine.adapter.dao.scalaquery.tables.QueryResults
import net.shrine.adapter.dao.scalaquery.tables.CountResults
import net.shrine.adapter.dao.scalaquery.tables.ErrorResults
import net.shrine.adapter.dao.scalaquery.tables.BreakdownResults
import net.shrine.util.Loggable

/**
 * @author Bill Simons
 * @author Clint Gilbert
 * @date 4/19/11
 * @link http://cbmi.med.harvard.edu
 */
final class RunQueryAdapterTest extends AbstractDependencyInjectionSpringContextTests with AdapterDbTest with ShouldMatchersForJUnit with Loggable {
  private val queryDef = QueryDefinition("foo", Term("foo"))

  private val broadcastMessageId = 1234563789L
  private val queryId = 123L
  private val expectedNetworkQueryId = 999L
  private val expectedLocalMasterId = queryId.toString
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

  private val countQueryResult = QueryResult(resultId, instanceId, Some(ResultOutputType.PATIENT_COUNT_XML), setSize, Some(now), Some(now), None, QueryResult.StatusType.Finished, None)

  private val dummyBreakdownData = Map("x" -> 1L, "y" -> 2L, "z" -> 3L)

  private val hiveCredentials = HiveCredentials("some-hive-domain", "hive-username", "hive-password", "hive-project")

  private val authn = AuthenticationInfo("some-domain", "username", Credential("jksafhkjaf", false))

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

    firstResult should equal(countQueryResult)
    
    resp.results.size should equal(1)
  }

  @Test
  def testGetBreakdownsWithRegularCountQuery {
    val breakdowns = ResultOutputType.breakdownTypes.map(breakdownFor)

    val resp = doTestGetBreakdowns(breakdowns)

    import ResultOutputType._

    val firstResult = resp.results.head

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

    val resultTypesExpectedToSucceed = Seq(PATIENT_AGE_COUNT_XML, PATIENT_GENDER_COUNT_XML)

    val breakdowns = resultTypesExpectedToSucceed.map(breakdownFor)

    val resp = doTestGetBreakdowns(breakdowns)

    val firstResult = resp.results.head

    firstResult.resultType should equal(Some(PATIENT_COUNT_XML))
    firstResult.setSize should equal(setSize)
    firstResult.description should equal(None)
    firstResult.breakdowns.keySet should equal(resultTypesExpectedToSucceed.toSet)
    firstResult.breakdowns.values.map(_.data).foreach(_ should equal(dummyBreakdownData))

    resp.results.size should equal(1)
  }

  private def breakdownFor(resultType: ResultOutputType) = I2b2ResultEnvelope(resultType, dummyBreakdownData)

  private def doTestGetBreakdowns(successfulBreakdowns: Seq[I2b2ResultEnvelope]): RunQueryResponse = {
    val outputTypes = justCounts ++ ResultOutputType.breakdownTypes

    val resp = doQueryThatReturnsSpecifiedBreakdowns(outputTypes, successfulBreakdowns)

    dobasicRunQueryResponseTest(resp)

    resp
  }

  private def dobasicRunQueryResponseTest(resp: RunQueryResponse) {
    resp.createDate should equal(now)
    resp.groupId should equal(groupId)
    resp.queryId should equal(queryId)
    resp.queryInstanceId should equal(instanceId)
    resp.queryName should equal(queryDef.name)
    resp.requestXml should equal(queryDef)
  }

  private def doQueryThatReturnsSpecifiedBreakdowns(outputTypes: Set[ResultOutputType], successfulBreakdowns: Seq[I2b2ResultEnvelope]): RunQueryResponse = afterCreatingTablesReturn {
    val breakdownQueryResults = ResultOutputType.breakdownTypes.zipWithIndex.map {
      case (rt, i) =>
        countQueryResult.withId(resultId + i + 1).withResultType(rt)
    }

    //Need this rigamarole to ensure that resultIds line up such that the type of breakdown the adapter asks for
    //(PATIENT_AGE_COUNT_XML, etc) is what the mock HttpClient actually returns.  Here, we build up maps of QueryResults
    //and I2b2ResultEnvelopes, keyed on resultIds generated in the previous expression, to use to look up values to use
    //to build ReadResultResponses
    val successfulBreakdownsByType = successfulBreakdowns.map(e => e.resultType -> e).toMap
    
    val successfulBreakdownTypes = successfulBreakdownsByType.keySet
    
    val breakdownQueryResultsByResultId = breakdownQueryResults.collect { case qr if successfulBreakdownTypes(qr.resultType.get) => qr.resultId -> qr }.toMap
    
    val breakdownsToBeReturnedByResultId = breakdownQueryResultsByResultId.map {
      case (resultId, queryResult) => (resultId, successfulBreakdownsByType(queryResult.resultType.get))
    }
    
    val expectedLocalTerm = Term("bar")

    val httpClient = new HttpClient {
      override def post(input: String, url: String): String = {
        val resp = ShrineRequest.fromI2b2(input) match {
          case req: RunQueryRequest => {
            //NB: Terms should be translated
            req.queryDefinition.expr should equal(expectedLocalTerm)

            //Credentials should be "translated"
            req.authn.username should equal(hiveCredentials.username)
            req.authn.domain should equal(hiveCredentials.domain)

            //I2b2 Project ID should be translated 
            req.projectId should equal(hiveCredentials.project)

            val queryResultMap = RawCrcRunQueryResponse.toQueryResultMap(countQueryResult +: breakdownQueryResults)

            RawCrcRunQueryResponse(queryId, now, "userId", "groupId", queryDef, instanceId, queryResultMap)
          }
          //NB: return a ReadResultResponse with new breakdown data each time, but will throw if the asked-for breakdown 
          //is not one of the ones passed to the enclosing method, simulating an error calling the CRC 
          case req: ReadResultRequest => {
            val resultId = req.localResultId.toLong
            
            ReadResultResponse(xmlResultId, breakdownQueryResultsByResultId(resultId), breakdownsToBeReturnedByResultId(resultId))
          }
        }

        resp.toI2b2String
      }
    }

    val result = doQuery(outputTypes, dao, httpClient)

    validateDb(successfulBreakdowns, breakdownQueryResultsByResultId)
    
    result
  }
  
  private def validateDb(breakdownsReturned: Seq[I2b2ResultEnvelope], breakdownQueryResultsByResultId: Map[Long, QueryResult]) {
    val expectedNetworkTerm = Term("foo")
    
    import ObfuscatorTest.within3
    
    //We should have one row in the shrine_query table, for the query just performed
    val queryRow = first(queryRows)
     
    {
      queryRow.dateCreated should not be (null)
      queryRow.domain should equal(authn.domain)
      queryRow.name should equal(queryDef.name)
      queryRow.localId should equal(expectedLocalMasterId)
      queryRow.networkId should equal(expectedNetworkQueryId)
      queryRow.username should equal(authn.username)
      queryRow.queryExpr should equal(expectedNetworkTerm)
    }
    
    list(queryRows).size should equal(1)

    //We should have one row in the count_result table, with the right obfuscated value, which is within the expected amount from the original count
    val countRow = first(countResultRows)

    {
      countRow.creationDate should not be (null)
      countRow.obfuscatedValue should equal(countQueryResult.setSize)
      within3(countRow.obfuscatedValue, countRow.originalValue) should be(true)
    }
    
    list(countResultRows).size should equal(1)

    //We should have 5 rows in the query_result table, one for the count result and one for each of the 4 requested breakdown types
    
    val queryResults = list(queryResultRows)

    {
      import ResultOutputType._

      val countQueryResultRow = queryResults.find(_.resultType == PATIENT_COUNT_XML).get
      
      countQueryResultRow.localId should equal(countQueryResult.resultId)
      countQueryResultRow.queryId should equal(queryRow.id)
      
      val resultIdsByResultType = breakdownQueryResultsByResultId.map { case (resultId, queryResult) => queryResult.resultType.get -> resultId }.toMap

      for (breakdownType <- ResultOutputType.breakdownTypes) {
        val breakdownQueryResultRow = queryResults.find(_.resultType == breakdownType).get
        
        breakdownQueryResultRow.queryId should equal(queryRow.id)
        
        //We'll have a result id if this breakdown type didn't fail
        if(resultIdsByResultType.contains(breakdownQueryResultRow.resultType)) {
          breakdownQueryResultRow.localId should equal(resultIdsByResultType(breakdownQueryResultRow.resultType))
        }
      }
    }
    
    queryResults.size should equal(5)
    
    val returnedBreakdownTypes = breakdownsReturned.map(_.resultType).toSet

    val notReturnedBreakdownTypes = ResultOutputType.breakdownTypes.toSet -- returnedBreakdownTypes

    val errorResults = list(errorResultRows)

    //We should have a row in the error_result table for each breakdown that COULD NOT be retrieved
    
    {
      for {
        queryResult <- queryResults
        if notReturnedBreakdownTypes.contains(queryResult.resultType)
        resultType = queryResult.resultType
        resultId = queryResult.id
      } {
        errorResults.find(_.resultId == resultId).isDefined should be(true)
      }
    }
    
    errorResults.size should equal(notReturnedBreakdownTypes.size)
    
    //We should have properly-obfuscated rows in the breakdown_result table for each of the breakdown types that COULD be retrieved  
    val breakdownResults = list(breakdownResultRows)
    
    {
      for {
        queryResult <- queryResults
        if returnedBreakdownTypes.contains(queryResult.resultType)
        resultType = queryResult.resultType
        resultId = queryResult.id
      } {
        //Find all the rows for a particular breakdown type
        val rowsWithType = breakdownResults.filter(_.resultId == resultId)
        
        //Combining the rows should give the expected dummy data
        rowsWithType.map(row => row.dataKey -> row.originalValue).toMap should equal(dummyBreakdownData)
        
        for(breakdownRow <- rowsWithType) {
          breakdownRow.obfuscatedValue should equal(-1) //all breakdown values are < 10, so we expect -1 always
        }
      }
    }
  }

  private def doQuery(outputTypes: Set[ResultOutputType])(i2b2XmlToReturn: => String): RunQueryResponse = {
    doQuery(outputTypes, MockAdapterDao, MockHttpClient(i2b2XmlToReturn))
  }

  private def doQuery(outputTypes: Set[ResultOutputType], adapterDao: AdapterDao, httpClient: HttpClient): RunQueryResponse = {
    val identity = new Identity("some-domain", "username")

    val translator = new QueryDefinitionTranslator(new ExpressionTranslator(Map("foo" -> Set("bar"))))

    //NB: Don't obfuscate, for simpler testing
    val adapter = new RunQueryAdapter("crc-url", httpClient, adapterDao, hiveCredentials, translator, new ShrineConfig, false)

    val req = new RunQueryRequest(projectId, 1000L, authn, expectedNetworkQueryId, "topicId", outputTypes, queryDef)

    //val broadcastMessage = new BroadcastMessage(queryId, masterId, instanceId, Seq(resultId), req)
    val broadcastMessage = BroadcastMessage(queryId, req)

    adapter.processRequest(identity, broadcastMessage)
  }
}