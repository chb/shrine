package net.shrine.adapter.service

import com.sun.jersey.test.framework.JerseyTest
import org.scalatest.junit.ShouldMatchersForJUnit
import net.shrine.adapter.AdapterDbTest
import junit.framework.TestCase
import com.sun.jersey.test.framework.AppDescriptor
import net.shrine.util.JerseyAppDescriptor
import net.shrine.util.JerseyHttpClient
import org.junit.Test
import net.shrine.protocol.ReadQueryDefinitionRequest
import net.shrine.protocol.ReadI2b2AdminPreviousQueriesRequest
import net.shrine.protocol.query.QueryDefinition
import net.shrine.protocol.query.Term
import net.shrine.protocol.AuthenticationInfo
import net.shrine.protocol.Credential
import net.shrine.protocol.QueryMaster
import net.shrine.util.Util
import net.shrine.adapter.spring.AbstractShrineJUnitSpringTest
import net.shrine.util.HttpClient
import net.shrine.util.XmlUtil
import net.shrine.protocol.ErrorResponse
import org.junit.Before
import org.junit.After
import net.shrine.protocol.ReadQueryDefinitionResponse
import scala.xml.XML
import net.shrine.protocol.ReadPreviousQueriesResponse

/**
 * @author clint
 * @date Apr 12, 2013
 *
 * NB: Ideally we would extend JerseyTest here, but since we have to extend AbstractDependencyInjectionSpringContextTests,
 * we get into a diamond-problem when extending JerseyTest as well, even when both of them are extended by shim traits.
 *
 * We work around this issue by mising in JerseyTestCOmponent, which brings in a JerseyTest by composition, and ensures
 * that it is set up and torn down properly.
 */
final class I2b2AdminResourceEndToEndJaxrsTest extends AbstractShrineJUnitSpringTest with JerseyTestComponent[I2b2AdminService] with AdapterDbTest with ShouldMatchersForJUnit {

  import I2b2AdminResourceEndToEndJaxrsTest._

  override def makeHandler = new I2b2AdminService(dao, AlwaysAuthenticatesMockPmHttpClient, "")

  private def adminClient = I2b2AdminClient(resourceUrl, new JerseyHttpClient)
  
  @Before
  def setUp() = this.JerseyTest.setUp()

  @After
  def tearDown() = this.JerseyTest.tearDown()

  @Test
  def testReadQueryDefinition = afterLoadingTestData {
    doTestReadQueryDefinition(networkQueryId1, Some((queryName1, queryDef1)))
  }
  
  @Test
  def testReadQueryDefinitionUnknownQueryId = afterLoadingTestData {
    doTestReadQueryDefinition(87134682364L, None)
  }
  
  private def doTestReadQueryDefinition(networkQueryId: Long, expectedQueryNameAndQueryDef: Option[(String, QueryDefinition)]) {
    val request = ReadQueryDefinitionRequest(projectId, waitTimeMs, authn, networkQueryId)

    val currentHandler = handler
    
    val response @ ReadQueryDefinitionResponse(masterId, name, userId, createDate, queryDefinition) = adminClient.readQueryDefinition(request)

    def stripNamespaces(s: String) = XmlUtil.stripNamespaces(XML.loadString(s))

    expectedQueryNameAndQueryDef match {
      case Some((expectedQueryName, expectedQueryDef)) => {
        masterId.get should be(networkQueryId)
        name.get should be(expectedQueryName)
        userId.get should be(authn.username)
        createDate.get should not be (null)
        //NB: I'm not sure why whacky namespaces were coming back from the resource;
        //this checks that the gist of the queryDef XML makes it back.
        //TODO: revisit this
        stripNamespaces(queryDefinition.get) should equal(stripNamespaces(expectedQueryDef.toI2b2String))
      } 
      case None => response should equal(ReadQueryDefinitionResponse.Empty)
    }
  }

  @Test
  def testReadI2b2AdminPreviousQueries = afterLoadingTestData {
    val searchString = queryName1
    val maxResults = 123
    val sortOrder = ReadI2b2AdminPreviousQueriesRequest.SortOrder.Ascending
    val categoryToSearchWithin = ReadI2b2AdminPreviousQueriesRequest.Category.All
    val searchStrategy = ReadI2b2AdminPreviousQueriesRequest.Strategy.Exact

    val request = ReadI2b2AdminPreviousQueriesRequest(projectId, waitTimeMs, authn, searchString, maxResults, sortOrder, categoryToSearchWithin, searchStrategy)

    doTestReadI2b2AdminPreviousQueries(request, Some(queryMaster1.copy(queryMasterId = networkQueryId1.toString)))
  }
  
  @Test
  def testReadI2b2AdminPreviousQueriesNoResultsExpected = afterLoadingTestData {
    //A request that won't return anything
    val request = ReadI2b2AdminPreviousQueriesRequest(projectId, waitTimeMs, authn, "askjdhakfgkafgkasf", 123)
    
    doTestReadI2b2AdminPreviousQueries(request, None)
  }
  
  private def doTestReadI2b2AdminPreviousQueries(request: ReadI2b2AdminPreviousQueriesRequest, expectedQueryMaster: Option[QueryMaster]) {
    val currentHandler = handler
    
    val ReadPreviousQueriesResponse(userIdOption, groupIdOption, queryMasters) = adminClient.readI2b2AdminPreviousQueries(request)

    expectedQueryMaster match {
      case Some(expected) => {
        userIdOption.get should equal(request.authn.username)
        groupIdOption.get should equal(request.authn.domain)
        
        queryMasters.isEmpty should be(false)
        queryMasters.head.createDate should not be(null)
        queryMasters.head.groupId should equal(expected.groupId)
        queryMasters.head.name should equal(expected.name)
        queryMasters.head.queryMasterId should equal(expected.queryMasterId)
        queryMasters.head.userId should equal(expected.userId)
      }
      case None => queryMasters.isEmpty should be(true)
    }
  }

  private def loadTestData() {
    dao.insertQuery(masterId1, networkQueryId1, queryName1, authn, queryDef1.expr)
  }

  private def afterLoadingTestData(f: => Any): Unit = afterCreatingTables {
    try {
      loadTestData()
    } finally {
      f
    }
  }
}

object I2b2AdminResourceEndToEndJaxrsTest {
  private val masterId1 = "1"

  private val networkQueryId1 = 999L

  private val queryName1 = "query-name1"

  private val queryDef1 = QueryDefinition(queryName1, Term("x"))

  private[this] val userId = "some-user-id"

  private[this] val domain = "some-domain"

  private[this] val password = "some-val"

  private lazy val authn = AuthenticationInfo(domain, userId, Credential(password, false))

  private val projectId = "some-project-id"

  private val waitTimeMs = 12345L

  private lazy val queryMaster1 = QueryMaster(masterId1, queryName1, userId, domain, Util.now)

  private object AlwaysAuthenticatesMockPmHttpClient extends HttpClient {
    override def post(input: String, url: String): String = {
      XmlUtil.stripWhitespace {
        <response>
          <message_body>
            <configure>
              <user>
                <full_name>Some user</full_name>
                <user_name>{ userId }</user_name>
                <domain>{ domain }</domain>
                <password>{ password }</password>
              </user>
            </configure>
          </message_body>
        </response>
      }.toString
    }
  }

  private object NeverAuthenticatesMockPmHttpClient extends HttpClient {
    override def post(input: String, url: String): String = ErrorResponse("blarg").toI2b2String
  }
}