package net.shrine.adapter.service

import net.shrine.adapter.AdapterTestHelpers
import org.scalatest.junit.ShouldMatchersForJUnit
import net.shrine.adapter.spring.AbstractShrineJUnitSpringTest
import org.junit.Before
import org.junit.After
import net.shrine.util.JerseyHttpClient
import net.shrine.util.HttpClient
import net.shrine.protocol.ErrorResponse
import net.shrine.util.XmlUtil
import net.shrine.protocol.ReadQueryDefinitionRequest
import net.shrine.protocol.query.QueryDefinition
import net.shrine.protocol.ReadQueryDefinitionResponse
import scala.xml.XML
import net.shrine.protocol.ReadI2b2AdminPreviousQueriesRequest
import net.shrine.protocol.ReadPreviousQueriesResponse
import net.shrine.protocol.QueryMaster
import net.shrine.adapter.dao.squeryl.AbstractSquerylAdapterTest

/**
 * @author clint
 * @date Apr 24, 2013
 */
abstract class AbstractI2b2AdminResourceJaxrsTest extends AbstractShrineJUnitSpringTest with JerseyTestComponent[I2b2AdminService] with AbstractSquerylAdapterTest with ShouldMatchersForJUnit with CanLoadTestData with AdapterTestHelpers {

  protected def adminClient = I2b2AdminClient(resourceUrl, new JerseyHttpClient)

  @Before
  def setUp() = this.JerseyTest.setUp()

  @After
  def tearDown() = this.JerseyTest.tearDown()
  
  protected object NeverAuthenticatesMockPmHttpClient extends HttpClient {
    override def post(input: String, url: String): String = ErrorResponse("blarg").toI2b2String
  }
  
  protected object AlwaysAuthenticatesMockPmHttpClient extends HttpClient {
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
                <project id={ projectId }>
                  <role>MANAGER</role>
                </project>
              </user>
            </configure>
          </message_body>
        </response>
      }.toString
    }
  }
  
  protected def doTestReadQueryDefinition(networkQueryId: Long, expectedQueryNameAndQueryDef: Option[(String, QueryDefinition)]) {
    val request = ReadQueryDefinitionRequest(projectId, waitTimeMs, authn, networkQueryId)

    val currentHandler = handler
    
    val resp = adminClient.readQueryDefinition(request)

    def stripNamespaces(s: String) = XmlUtil.stripNamespaces(XML.loadString(s))

    expectedQueryNameAndQueryDef match {
      case Some((expectedQueryName, expectedQueryDef)) => {
        val response @ ReadQueryDefinitionResponse(masterId, name, userId, createDate, queryDefinition) = resp
        
        masterId should be(networkQueryId)
        name should be(expectedQueryName)
        userId should be(authn.username)
        createDate should not be (null)
        //NB: I'm not sure why whacky namespaces were coming back from the resource;
        //this checks that the gist of the queryDef XML makes it back.
        //TODO: revisit this
        stripNamespaces(queryDefinition) should equal(stripNamespaces(expectedQueryDef.toI2b2String))
      } 
      case None => resp.isInstanceOf[ErrorResponse] should be(true)
    }
  }
  
  protected def doTestReadI2b2AdminPreviousQueries(request: ReadI2b2AdminPreviousQueriesRequest, expectedQueryMaster: Option[QueryMaster]) {
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
      case None => {
        userIdOption should be(None)
        groupIdOption should be(None)
        queryMasters.isEmpty should be(true)
      }
    }
  }
}