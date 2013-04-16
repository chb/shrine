package net.shrine.adapter.service

import junit.framework.TestCase
import com.sun.jersey.test.framework.JerseyTest
import org.scalatest.junit.ShouldMatchersForJUnit
import net.shrine.util.JerseyAppDescriptor
import com.sun.jersey.test.framework.AppDescriptor
import net.shrine.protocol.I2b2AdminRequestHandler
import net.shrine.protocol.ReadQueryDefinitionRequest
import net.shrine.protocol.ReadI2b2AdminPreviousQueriesRequest
import net.shrine.util.Util
import net.shrine.protocol.ShrineResponse
import net.shrine.protocol.ReadQueryDefinitionResponse
import net.shrine.protocol.ReadPreviousQueriesResponse
import org.junit.Test
import net.shrine.util.JerseyHttpClient
import net.shrine.protocol.AuthenticationInfo
import net.shrine.protocol.Credential
import net.shrine.protocol.query.QueryDefinition
import net.shrine.protocol.query.Term
import net.shrine.protocol.ReadI2b2AdminPreviousQueriesRequest
import net.shrine.protocol.QueryMaster
import net.shrine.util.XmlUtil
import scala.xml.XML
import I2b2AdminResourceJaxrsTest._

/**
 * @author clint
 * @date Apr 10, 2013
 */
final class I2b2AdminResourceJaxrsTest extends JerseyTest with ShouldMatchersForJUnit {
  
  var handler: MockShrineRequestHandler = _
  
  def resourceUrl = resource.getURI.toString + "i2b2/admin/request"
  
  override def configure: AppDescriptor = {
    JerseyAppDescriptor.forResource[I2b2AdminResource].using { 
      handler = new MockShrineRequestHandler
      
      handler
    }
  }
  
  @Test
  def testReadQueryDefinition {
    val client = I2b2AdminClient(resourceUrl, new JerseyHttpClient)
    
    val queryId = 987654321L
    
    val request = ReadQueryDefinitionRequest(projectId, waitTimeMs, authn, queryId)
    
    val currentHandler = handler
    
    val response = client.readQueryDefinition(request)
    
    response should not be(null)
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
    currentHandler.readQueryDefinitionParam should equal(request)
  }
  
  @Test
  def testReadI2b2AdminPreviousQueries {
    val client = I2b2AdminClient(resourceUrl, new JerseyHttpClient)
    
    val searchString = "asdk;laskd;lask;gdjsg"
    val maxResults = 123
    val sortOrder = ReadI2b2AdminPreviousQueriesRequest.SortOrder.Ascending
    val categoryToSearchWithin = ReadI2b2AdminPreviousQueriesRequest.Category.All
    val searchStrategy = ReadI2b2AdminPreviousQueriesRequest.Strategy.Exact
    
    val request = ReadI2b2AdminPreviousQueriesRequest(projectId, waitTimeMs, authn, searchString, maxResults, sortOrder, categoryToSearchWithin, searchStrategy)
    
    val currentHandler = handler
    
    val response = client.readI2b2AdminPreviousQueries(request)
    
    response should not be(null)
    response.userId should equal(request.authn.username)
    response.groupId should equal(request.authn.domain)
    response.queryMasters should equal(Seq(queryMaster))
    
    currentHandler.shouldBroadcastParam should be(false)
    currentHandler.readI2b2AdminPreviousQueriesParam should be(request)
    currentHandler.readQueryDefinitionParam should be(null)
  }
}

object I2b2AdminResourceJaxrsTest {
  private val queryDef = QueryDefinition("foo", Term("x"))
  
  private val userId = "some-user-id"

  private val domain = "some-domain" 
    
  private lazy val authn = new AuthenticationInfo(domain, userId, new Credential("some-val", false))
  
  private val projectId = "some-project-id"
    
  private val waitTimeMs = 12345L
  
  private lazy val queryMaster = QueryMaster("queryMasterId", "name", userId, domain, Util.now)
  
  /**
   * Mock ShrineRequestHandler; stores passed parameters for later inspection.
   * Private, since this is (basically) the enclosing test class's state
   */
  final class MockShrineRequestHandler extends I2b2AdminRequestHandler {
    private val lock = new AnyRef
    
    def shouldBroadcastParam = lock.synchronized(_shouldBroadcastParam)
    def readQueryDefinitionParam = lock.synchronized(_readQueryDefinitionParam)
    def readI2b2AdminPreviousQueriesParam = lock.synchronized(_readI2b2AdminPreviousQueriesParam)
    
    private var _shouldBroadcastParam = false
    private var _readQueryDefinitionParam: ReadQueryDefinitionRequest = _
    private var _readI2b2AdminPreviousQueriesParam: ReadI2b2AdminPreviousQueriesRequest = _

    override def readI2b2AdminPreviousQueries(request: ReadI2b2AdminPreviousQueriesRequest, shouldBroadcast: Boolean): ShrineResponse = setShouldBroadcastAndThen(shouldBroadcast) {
      lock.synchronized { _readI2b2AdminPreviousQueriesParam = request }
      
      ReadPreviousQueriesResponse(Option(request.authn.username), Option(request.authn.domain), Seq(queryMaster))
    }
    
    override def readQueryDefinition(request: ReadQueryDefinitionRequest, shouldBroadcast: Boolean): ShrineResponse = setShouldBroadcastAndThen(shouldBroadcast) {
      lock.synchronized { _readQueryDefinitionParam = request }

      ReadQueryDefinitionResponse(request.queryId, "some-query-name", request.authn.username, Util.now, queryDef.toI2b2String)
    }
    
    private def setShouldBroadcastAndThen(shouldBroadcast: Boolean)(f: => ShrineResponse): ShrineResponse = {
      try { f } finally {
        lock.synchronized { _shouldBroadcastParam = shouldBroadcast }
      }
    }
  }
}