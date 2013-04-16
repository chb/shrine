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
abstract class I2b2AdminResourceEndToEndJaxrsTest extends AbstractShrineJUnitSpringTest with JerseyTestComponent[I2b2AdminService] with AdapterDbTest with ShouldMatchersForJUnit {

  import I2b2AdminResourceEndToEndJaxrsTest._

  override def makeHandler = new I2b2AdminService(dao, AlwaysAuthenticatesMockPmHttpClient, "")

  @Before
  def setUp() = this.JerseyTest.setUp()

  @After
  def tearDown() = this.JerseyTest.tearDown()

  @Test
  def testReadQueryDefinition = afterCreatingTables {
    val client = I2b2AdminClient(resourceUrl, new JerseyHttpClient)

    val queryId = 987654321L

    val request = ReadQueryDefinitionRequest(projectId, waitTimeMs, authn, queryId)

    val currentHandler = handler

    val response = client.readQueryDefinition(request)

    /*response should not be(null)
    response.masterId should equal(queryId)
    response.name should equal("some-query-name")
    response.createDate should not be(null)
    
    def stripNamespaces(s: String) = XmlUtil.stripNamespaces(XML.loadString(s))
    
    //NB: I'm not sure why whacky namespaces were coming back from the resource;
    //this checks that the gist of the queryDef XML makes it back.
    //TODO: revisit this
    stripNamespaces(response.queryDefinition) should equal(stripNamespaces(queryDef.toI2b2String))
    
    currentHandler.shouldBroadcastParam should be(false)
    currentHandler.readI2b2AdminPreviousQueriesParam should be(null)
    currentHandler.readQueryDefinitionParam should equal(request)*/
  }

  @Test
  def testReadI2b2AdminPreviousQueries = afterCreatingTables {
    val client = I2b2AdminClient(resourceUrl, new JerseyHttpClient)

    val searchString = "asdk;laskd;lask;gdjsg"
    val maxResults = 123
    val sortOrder = ReadI2b2AdminPreviousQueriesRequest.SortOrder.Ascending
    val categoryToSearchWithin = ReadI2b2AdminPreviousQueriesRequest.Category.All
    val searchStrategy = ReadI2b2AdminPreviousQueriesRequest.Strategy.Exact

    val request = ReadI2b2AdminPreviousQueriesRequest(projectId, waitTimeMs, authn, searchString, maxResults, sortOrder, categoryToSearchWithin, searchStrategy)

    val currentHandler = handler

    val response = client.readI2b2AdminPreviousQueries(request)

    /*response should not be(null)
    response.userId should equal(request.authn.username)
    response.groupId should equal(request.authn.domain)
    response.queryMasters should equal(Seq(queryMaster))
    
    currentHandler.shouldBroadcastParam should be(false)
    currentHandler.readI2b2AdminPreviousQueriesParam should be(request)
    currentHandler.readQueryDefinitionParam should be(null)*/
  }
}

object I2b2AdminResourceEndToEndJaxrsTest {
  private val queryDef = QueryDefinition("foo", Term("x"))

  private val userId = "some-user-id"

  private val domain = "some-domain"

  private val password = "some-val"

  private lazy val authn = new AuthenticationInfo(domain, userId, new Credential(password, false))

  private val projectId = "some-project-id"

  private val waitTimeMs = 12345L

  private lazy val queryMaster = QueryMaster("queryMasterId", "name", userId, domain, Util.now)

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