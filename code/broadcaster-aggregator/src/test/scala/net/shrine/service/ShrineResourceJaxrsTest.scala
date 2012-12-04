package net.shrine.service

import com.sun.jersey.test.framework.JerseyTest
import com.sun.jersey.test.framework.LowLevelAppDescriptor.Builder
import org.junit.Test
import com.sun.jersey.test.framework.AppDescriptor
import com.sun.jersey.api.core.ResourceConfig
import com.sun.jersey.api.core.ClassNamesResourceConfig
import com.sun.jersey.spi.inject.InjectableProvider
import java.lang.reflect.Type
import com.sun.jersey.core.spi.component.ComponentScope
import com.sun.jersey.spi.inject.Injectable
import com.sun.jersey.core.spi.component.ComponentContext
import org.scalatest.junit.AssertionsForJUnit
import org.scalatest.matchers.ShouldMatchers
import net.shrine.protocol._
import net.shrine.protocol.query.QueryDefinition
import net.shrine.protocol.query.Term
import org.spin.tools.{RandomTool, NetworkTime}
import net.shrine.client.JerseyShrineClient

/**
 *
 * @author Clint Gilbert
 * @date Sep 14, 2011
 *
 * @link http://cbmi.med.harvard.edu
 *
 * This software is licensed under the LGPL
 * @link http://www.gnu.org/licenses/lgpl.html
 *
 * Starts a ShrineResource in an embedded HTTP server, sends requests to it, then verifies that the requests don't fail,
 * and that the parameters made it from the client to the ShrineResource successfully.  Uses a mock ShrineRequestHandler, so
 * it doesn't test that correct values are returned by the ShrineResource.
 */
final class ShrineResourceJaxrsTest extends JerseyTest with AssertionsForJUnit with ShouldMatchers {
  private val projectId = "some-project-id"

  private val topicId = "some-topic-id"

  private val userId = "some-user-id"

  private val authenticationInfo = new AuthenticationInfo("some-domain", userId, new Credential("some-val", false))

  private val shrineClient = new JerseyShrineClient(resource.getURI.toString, projectId, authenticationInfo)

  @Test
  def testReadApprovedQueryTopics {
    val response = shrineClient.readApprovedQueryTopics(userId)

    response should not(be(null))

    MockShrineRequestHandler.readPreviousQueriesParam should be(null)
    MockShrineRequestHandler.runQueryParam should be(null)
    MockShrineRequestHandler.readQueryInstancesParam should be(null)
    MockShrineRequestHandler.readInstanceResultsParam should be(null)
    MockShrineRequestHandler.readPdoParam should be(null)
    MockShrineRequestHandler.readQueryDefinitionParam should be(null)
    MockShrineRequestHandler.deleteQueryParam should be(null)
    MockShrineRequestHandler.renameQueryParam should be(null)
    MockShrineRequestHandler.readResultParam should be(null)

    val param = MockShrineRequestHandler.readApprovedQueryTopicsParam

    validateCachedParam(param, CRCRequestType.SheriffRequestType)

    param.userId should equal(userId)
  }

  @Test
  def testReadPreviousQueries = resetMockThen {
    val fetchSize = 123

    val response = shrineClient.readPreviousQueries(userId, fetchSize)

    response should not(be(null))

    MockShrineRequestHandler.readApprovedQueryTopicsParam should be(null)
    MockShrineRequestHandler.runQueryParam should be(null)
    MockShrineRequestHandler.readQueryInstancesParam should be(null)
    MockShrineRequestHandler.readInstanceResultsParam should be(null)
    MockShrineRequestHandler.readPdoParam should be(null)
    MockShrineRequestHandler.readQueryDefinitionParam should be(null)
    MockShrineRequestHandler.deleteQueryParam should be(null)
    MockShrineRequestHandler.renameQueryParam should be(null)
    MockShrineRequestHandler.readResultParam should be(null)

    val param = MockShrineRequestHandler.readPreviousQueriesParam

    validateCachedParam(param, CRCRequestType.UserRequestType)

    param.fetchSize should equal(fetchSize)
    param.userId should equal(userId)
  }

  @Test
  def testRunQuery = resetMockThen {

    val queryDef = QueryDefinition("foo", Term("nuh"))

    def doTestRunQueryResponse(response: AggregatedRunQueryResponse, expectedOutputTypes: Set[ResultOutputType]) {

      response should not(be(null))

      MockShrineRequestHandler.readApprovedQueryTopicsParam should be(null)
      MockShrineRequestHandler.readPreviousQueriesParam should be(null)
      MockShrineRequestHandler.readQueryInstancesParam should be(null)
      MockShrineRequestHandler.readInstanceResultsParam should be(null)
      MockShrineRequestHandler.readPdoParam should be(null)
      MockShrineRequestHandler.readQueryDefinitionParam should be(null)
      MockShrineRequestHandler.deleteQueryParam should be(null)
      MockShrineRequestHandler.renameQueryParam should be(null)
      MockShrineRequestHandler.readResultParam should be(null)

      val param = MockShrineRequestHandler.runQueryParam

      validateCachedParam(param, CRCRequestType.QueryDefinitionRequestType)

      param.outputTypes should equal(expectedOutputTypes)
      param.queryDefinition should equal(queryDef)
      param.topicId should equal(topicId)
    }

    def doTestRunQuery(outputTypes: Set[ResultOutputType]) {
      val responseScalaSet = shrineClient.runQuery(topicId, outputTypes, queryDef)

      doTestRunQueryResponse(responseScalaSet, outputTypes)

      val responseJavaSet = shrineClient.runQuery(topicId, outputTypes, queryDef)

      doTestRunQueryResponse(responseJavaSet, outputTypes)
    }

    Seq(ResultOutputType.values.toSet,
      Set(ResultOutputType.PATIENT_COUNT_XML),
      Set(ResultOutputType.PATIENTSET),
      Set.empty[ResultOutputType]).foreach(doTestRunQuery)
  }

  @Test
  def testReadQueryInstances = resetMockThen {
    val queryId = 123L
    
    val response = shrineClient.readQueryInstances(queryId)

    response should not(be(null))

    MockShrineRequestHandler.readApprovedQueryTopicsParam should be(null)
    MockShrineRequestHandler.readPreviousQueriesParam should be(null)
    MockShrineRequestHandler.runQueryParam should be(null)
    MockShrineRequestHandler.readInstanceResultsParam should be(null)
    MockShrineRequestHandler.readPdoParam should be(null)
    MockShrineRequestHandler.readQueryDefinitionParam should be(null)
    MockShrineRequestHandler.deleteQueryParam should be(null)
    MockShrineRequestHandler.renameQueryParam should be(null)
    MockShrineRequestHandler.readResultParam should be(null)

    val param = MockShrineRequestHandler.readQueryInstancesParam

    validateCachedParam(param, CRCRequestType.MasterRequestType)

    param.queryId should equal(queryId)
  }
  
  @Test
  def testReadInstanceResults = resetMockThen {
    val shrineNetworkQueryId = 98765L
    
    val response = shrineClient.readInstanceResults(shrineNetworkQueryId)

    response should not(be(null))

    MockShrineRequestHandler.readApprovedQueryTopicsParam should be(null)
    MockShrineRequestHandler.readPreviousQueriesParam should be(null)
    MockShrineRequestHandler.runQueryParam should be(null)
    MockShrineRequestHandler.readQueryInstancesParam should be(null)
    MockShrineRequestHandler.readPdoParam should be(null)
    MockShrineRequestHandler.readQueryDefinitionParam should be(null)
    MockShrineRequestHandler.deleteQueryParam should be(null)
    MockShrineRequestHandler.renameQueryParam should be(null)
    MockShrineRequestHandler.readResultParam should be(null)

    val param = MockShrineRequestHandler.readInstanceResultsParam

    validateCachedParam(param, CRCRequestType.InstanceRequestType)

    param.shrineNetworkQueryId should equal(shrineNetworkQueryId)
  }
  
  @Test
  def testReadPdo = resetMockThen {
    val patientSetId = "patientSetId"
    val optionsXml = <foo><bar/></foo>
    
    val response = shrineClient.readPdo(patientSetId, optionsXml)

    response should not(be(null))

    MockShrineRequestHandler.readApprovedQueryTopicsParam should be(null)
    MockShrineRequestHandler.readPreviousQueriesParam should be(null)
    MockShrineRequestHandler.runQueryParam should be(null)
    MockShrineRequestHandler.readQueryInstancesParam should be(null)
    MockShrineRequestHandler.readInstanceResultsParam should be(null)
    MockShrineRequestHandler.readQueryDefinitionParam should be(null)
    MockShrineRequestHandler.deleteQueryParam should be(null)
    MockShrineRequestHandler.renameQueryParam should be(null)
    MockShrineRequestHandler.readResultParam should be(null)

    val param = MockShrineRequestHandler.readPdoParam

    validateCachedParam(param, CRCRequestType.GetPDOFromInputListRequestType)

    param.patientSetCollId should equal(patientSetId)
    //Turn NodeSeqs to Strings for reliable comparisons
    param.optionsXml.toString should equal(optionsXml.toString)
  }
  
  @Test
  def testReadQueryDefinition = resetMockThen {
    val queryId = 3789894L
    
    val response = shrineClient.readQueryDefinition(queryId)

    response should not(be(null))

    MockShrineRequestHandler.readApprovedQueryTopicsParam should be(null)
    MockShrineRequestHandler.readPreviousQueriesParam should be(null)
    MockShrineRequestHandler.runQueryParam should be(null)
    MockShrineRequestHandler.readQueryInstancesParam should be(null)
    MockShrineRequestHandler.readPdoParam should be(null)
    MockShrineRequestHandler.readInstanceResultsParam should be(null)
    MockShrineRequestHandler.deleteQueryParam should be(null)
    MockShrineRequestHandler.renameQueryParam should be(null)
    MockShrineRequestHandler.readResultParam should be(null)

    val param = MockShrineRequestHandler.readQueryDefinitionParam

    validateCachedParam(param, CRCRequestType.GetRequestXml)

    param.queryId should equal(queryId)
  }
  
  @Test
  def testDeleteQuery = resetMockThen {
    val queryId = 3789894L
    
    val response = shrineClient.deleteQuery(queryId)

    response should not(be(null))

    MockShrineRequestHandler.readApprovedQueryTopicsParam should be(null)
    MockShrineRequestHandler.readPreviousQueriesParam should be(null)
    MockShrineRequestHandler.runQueryParam should be(null)
    MockShrineRequestHandler.readQueryInstancesParam should be(null)
    MockShrineRequestHandler.readPdoParam should be(null)
    MockShrineRequestHandler.readInstanceResultsParam should be(null)
    MockShrineRequestHandler.renameQueryParam should be(null)
    MockShrineRequestHandler.readResultParam should be(null)
    
    val param = MockShrineRequestHandler.deleteQueryParam

    validateCachedParam(param, CRCRequestType.MasterDeleteRequestType)

    param.queryId should equal(queryId)
  }
  
  @Test
  def testRenameQuery = resetMockThen {
    val queryId = 3789894L
    val queryName = "aslkfhkasfh"
    
    val response = shrineClient.renameQuery(queryId, queryName)

    response should not(be(null))

    MockShrineRequestHandler.readApprovedQueryTopicsParam should be(null)
    MockShrineRequestHandler.readPreviousQueriesParam should be(null)
    MockShrineRequestHandler.runQueryParam should be(null)
    MockShrineRequestHandler.readQueryInstancesParam should be(null)
    MockShrineRequestHandler.readPdoParam should be(null)
    MockShrineRequestHandler.readInstanceResultsParam should be(null)
    MockShrineRequestHandler.deleteQueryParam should be(null)
    MockShrineRequestHandler.readResultParam should be(null)
    
    val param = MockShrineRequestHandler.renameQueryParam

    validateCachedParam(param, CRCRequestType.MasterRenameRequestType)

    param.queryId should equal(queryId)
    param.queryName should equal(queryName)
  }
  
  private def validateCachedParam(param: ShrineRequest, expectedRequestType: CRCRequestType) {
    param should not(be(null))
    param.projectId should equal(projectId)
    param.authn should equal(authenticationInfo)
    param.requestType should equal(expectedRequestType)
    param.waitTimeMs should equal(ShrineResource.waitTimeMs)
  }
  
  /**
   * We invoked the no-arg superclass constructor, so we must override configure() to provide an AppDescriptor
   * That tells Jersey to instantiate and expose ShrineResource
   */
  override def configure: AppDescriptor = {
    //Make a ResourceConfig that describes the one class - ShrineResource - we want Jersey to instantiate and expose 
    val resourceConfig: ResourceConfig = new ClassNamesResourceConfig(classOf[ShrineResource])

    //Register an InjectableProvider that produces a mock ShrineRequestHandler that will be provided to the ShrineResource
    //instantiated by Jersey.  For this to work, the ShrineRequestHandler constructor param on ShrineResource must be 
    //annotated with @net.shrine.service.annotation.ShrineRequestHandler.  Here, we map that annotation to an 
    //InjectableProvider that produces mock ShrineRequestHandlers and register this with the ResourceConfig. 
    resourceConfig.getSingletons.add(new InjectableProvider[annotation.RequestHandler, Type] {
      override def getScope: ComponentScope = ComponentScope.Singleton

      override def getInjectable(context: ComponentContext, a: annotation.RequestHandler, t: Type) = new Injectable[AnyRef] {
        def getValue: AnyRef = MockShrineRequestHandler
      }
    })

    //Make an AppDescriptor from the ResourceConfig
    new Builder(resourceConfig).build
  }

  private def resetMockThen(body: => Any) {
    MockShrineRequestHandler.reset()

    //(Healthy?) paranoia
    MockShrineRequestHandler.readApprovedQueryTopicsParam should be(null)
    MockShrineRequestHandler.readPreviousQueriesParam should be(null)
    MockShrineRequestHandler.runQueryParam should be(null)
    MockShrineRequestHandler.readQueryInstancesParam should be(null)
    MockShrineRequestHandler.readInstanceResultsParam should be(null)
    MockShrineRequestHandler.readPdoParam should be(null)
    MockShrineRequestHandler.readQueryDefinitionParam should be(null)
    MockShrineRequestHandler.deleteQueryParam should be(null)
    MockShrineRequestHandler.renameQueryParam should be(null)
    MockShrineRequestHandler.readResultParam should be(null)
    
    body
  }

  /**
   * Mock ShrineRequestHandler; stores passed parameters for later inspection.
   * Private, since this is (basically) the enclosing test class's state
   */
  private object MockShrineRequestHandler extends ShrineRequestHandler {
    var readApprovedQueryTopicsParam: ReadApprovedQueryTopicsRequest = _
    var readPreviousQueriesParam: ReadPreviousQueriesRequest = _
    var runQueryParam: RunQueryRequest = _
    var readQueryInstancesParam: ReadQueryInstancesRequest =_
    var readInstanceResultsParam: ReadInstanceResultsRequest = _
    var readPdoParam: ReadPdoRequest = _
    var readQueryDefinitionParam: ReadQueryDefinitionRequest = _
    var deleteQueryParam: DeleteQueryRequest = _
    var renameQueryParam: RenameQueryRequest = _
    var readResultParam: ReadResultRequest = _

    def reset() {
      readApprovedQueryTopicsParam = null
      readPreviousQueriesParam = null
      runQueryParam = null
      readQueryInstancesParam = null
      readInstanceResultsParam = null
      readPdoParam = null
      readQueryDefinitionParam = null
      deleteQueryParam = null
      renameQueryParam = null
      readResultParam = null
    }

    override def readApprovedQueryTopics(request: ReadApprovedQueryTopicsRequest): ShrineResponse = {
      readApprovedQueryTopicsParam = request

      ReadApprovedQueryTopicsResponse(Seq(new ApprovedTopic(123L, "some topic")))
    }

    override def readPreviousQueries(request: ReadPreviousQueriesRequest): ShrineResponse = {
      readPreviousQueriesParam = request

      ReadPreviousQueriesResponse("userId", "groupId", Seq.empty)
    }

    override def readQueryInstances(request: ReadQueryInstancesRequest): ShrineResponse = {
      readQueryInstancesParam = request

      ReadQueryInstancesResponse(999L, "userId", "groupId", Seq.empty)
    }

    override def readInstanceResults(request: ReadInstanceResultsRequest): ShrineResponse = {
      readInstanceResultsParam = request

      ReadInstanceResultsResponse(1337L, Seq(new QueryResult(123L, 1337L, Some(ResultOutputType.PATIENT_COUNT_XML), 789L, None, None, Some("description"), "statusType", Some("statusMessage"))))
    }

    override def readPdo(request: ReadPdoRequest): ShrineResponse = {
      readPdoParam = request

      import RandomTool.randomString
  
      def paramResponse = new ParamResponse(randomString, randomString, randomString)
      
      ReadPdoResponse(Seq(new EventResponse("event", "patient", None, None, Seq.empty)), Seq(new PatientResponse("patientId", Seq(paramResponse))), Seq(new ObservationResponse(None, "eventId", None, "patientId", None, None, None, "observerCode", "startDate", None, "valueTypeCode",None,None,None,None,None,None,None, Seq(paramResponse))))
    }

    override def readQueryDefinition(request: ReadQueryDefinitionRequest): ShrineResponse = {
      readQueryDefinitionParam = request

      ReadQueryDefinitionResponse(87456L, "name", "userId", now, "<foo/>")
    }

    override def runQuery(request: RunQueryRequest): ShrineResponse = {
      runQueryParam = request

      AggregatedRunQueryResponse(123L, now, "userId", "groupId", request.queryDefinition, 456L, Seq.empty)
    }

    override def deleteQuery(request: DeleteQueryRequest): ShrineResponse = {
      deleteQueryParam = request

      DeleteQueryResponse(56834756L)
    }

    override def renameQuery(request: RenameQueryRequest): ShrineResponse = {
      renameQueryParam = request

      RenameQueryResponse(873468L, "some-name")
    }
    
    override def readResult(request: ReadResultRequest): ShrineResponse = {
      readResultParam = request
      
      sys.error("TODO")
    }
    
    private def now = (new NetworkTime).getXMLGregorianCalendar
  }
}