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

  @Before
  def setUp() = this.JerseyTest.setUp()

  @After
  def tearDown() = this.JerseyTest.tearDown()

  @Test
  def testReadQueryDefinition = afterLoadingTestData {
    val client = I2b2AdminClient(resourceUrl, new JerseyHttpClient)

    val request = ReadQueryDefinitionRequest(projectId, waitTimeMs, authn, networkQueryId1)

    val currentHandler = handler

    val ReadQueryDefinitionResponse(masterId, name, userId, createDate, queryDefinition) = client.readQueryDefinition(request)

    def stripNamespaces(s: String) = XmlUtil.stripNamespaces(XML.loadString(s))
    
    masterId should be(networkQueryId1)
    name should be(queryName1)
    userId should be(authn.username)
    createDate should not be (null)

    //NB: I'm not sure why whacky namespaces were coming back from the resource;
    //this checks that the gist of the queryDef XML makes it back.
    //TODO: revisit this
    stripNamespaces(queryDefinition) should equal(stripNamespaces(queryDef1.toI2b2String))
  }

  @Test
  def testReadI2b2AdminPreviousQueries = afterLoadingTestData {
    val client = I2b2AdminClient(resourceUrl, new JerseyHttpClient)

    val searchString = queryName1
    val maxResults = 123
    val sortOrder = ReadI2b2AdminPreviousQueriesRequest.SortOrder.Ascending
    val categoryToSearchWithin = ReadI2b2AdminPreviousQueriesRequest.Category.All
    val searchStrategy = ReadI2b2AdminPreviousQueriesRequest.Strategy.Exact

    val request = ReadI2b2AdminPreviousQueriesRequest(projectId, waitTimeMs, authn, searchString, maxResults, sortOrder, categoryToSearchWithin, searchStrategy)

    val currentHandler = handler

    val ReadPreviousQueriesResponse(Some(userId), Some(groupId), Seq(queryMaster)) = client.readI2b2AdminPreviousQueries(request)

    userId should equal(request.authn.username)
    groupId should equal(request.authn.domain)
    queryMaster.createDate should not be(null)
    queryMaster.groupId should equal(queryMaster1.groupId)
    queryMaster.name should equal(queryMaster1.name)
    queryMaster.queryMasterId should equal(networkQueryId1.toString)
    queryMaster.userId should equal(queryMaster1.userId)
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

  private lazy val authn = new AuthenticationInfo(domain, userId, new Credential(password, false))

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