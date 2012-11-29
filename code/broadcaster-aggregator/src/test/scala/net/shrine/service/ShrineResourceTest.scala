package net.shrine.service

import org.scalatest.junit.AssertionsForJUnit
import org.scalatest.junit.ShouldMatchersForJUnit
import org.scalatest.mock.EasyMockSugar
import junit.framework.TestCase
import org.easymock.EasyMock.{ eq => isEqualTo, expect => invoke, reportMatcher }
import org.spin.tools.NetworkTime
import net.shrine.protocol._
import net.shrine.protocol.query.QueryDefinition
import net.shrine.protocol.query.Term
import org.easymock.IArgumentMatcher
import org.easymock.internal.ArgumentToString

/**
 * @author Clint Gilbert
 * @date 9/13/2011
 * @link http://cbmi.med.harvard.edu
 * @link http://chip.org
 *       <p/>
 *       NOTICE: This software comes with NO guarantees whatsoever and is
 *       licensed as Lgpl Open Source
 * @link http://www.gnu.org/licenses/lgpl.html
 */
final class ShrineResourceTest extends TestCase with AssertionsForJUnit with ShouldMatchersForJUnit with EasyMockSugar {

  private var handler: ShrineRequestHandler = _
  private var resource: ShrineResource = _

  private val projectId = "projectId"
  private val authenticationInfo = new AuthenticationInfo("domain", "username", new Credential("secret", true))
  private val userId = "userId"

  override def setUp() {
    handler = mock[ShrineRequestHandler]
    resource = new ShrineResource(handler)
  }

  import ShrineResource.waitTimeMs
  
  def testReadApprovedQueryTopics {
    val expectedRequest = new ReadApprovedQueryTopicsRequest(projectId, waitTimeMs, authenticationInfo, userId)
    val expectedResponse = new ReadApprovedQueryTopicsResponse(Seq(new ApprovedTopic(123L, "foo")))

    setExpectations(_.readApprovedQueryTopics, expectedRequest, expectedResponse)

    execute {
      resource.readApprovedQueryTopics(projectId, authenticationInfo, userId)
    }
  }

  def testReadPreviousQueries {
    def doTestReadPreviousQueries(fetchSize: Int, expectedFetchSize: Int) {
      //Call setUp again create a new mock and new ShrinResource;
      //each pair of expecting/whenExecuting calls needs a fresh mock.
      this.setUp()

      val expectedRequest = new ReadPreviousQueriesRequest(projectId, waitTimeMs, authenticationInfo, userId, expectedFetchSize)
      val expectedResponse = new ReadPreviousQueriesResponse("userId", "groupId", Seq.empty)

      setExpectations(_.readPreviousQueries, expectedRequest, expectedResponse)

      execute {
        resource.readPreviousQueries(projectId, authenticationInfo, userId, fetchSize)
      }
    }

    doTestReadPreviousQueries(-100, -100)
    doTestReadPreviousQueries(0, 20)
    doTestReadPreviousQueries(1, 1)
    doTestReadPreviousQueries(100, 100)
  }

  def testRunQuery {
    val outputTypes = ResultOutputType.values.toSet
    val queryDef = QueryDefinition("foo", Term("nuh"))
    val topicId = "topicId"

    val expectedRequest = new RunQueryRequest(projectId, waitTimeMs, authenticationInfo, 999L, topicId, outputTypes, queryDef)
    val expectedResponse = new RunQueryResponse(999L, null, "userId", "groupId", queryDef, 0L, Seq.empty)

    def isEqualToExceptForQueryId(expected: RunQueryRequest): RunQueryRequest = {
      reportMatcher(new IArgumentMatcher {
        override def matches(argument: AnyRef): Boolean = {
          argument.isInstanceOf[RunQueryRequest] && {
            val actual = argument.asInstanceOf[RunQueryRequest]
            
            //Everything *but* queryId, which is randomly generated by ShrineResource :\
            actual.authn == expected.authn &&
            actual.outputTypes == expected.outputTypes &&
            actual.projectId == expected.projectId &&
            actual.queryDefinition == expected.queryDefinition &&
            actual.requestType == expected.requestType &&
            actual.topicId == expected.topicId &&
            actual.waitTimeMs == expected.waitTimeMs
          }
        }
        
        override def appendTo(buffer: StringBuffer): Unit = ArgumentToString.appendArgument(expected, buffer)
      })
      
      null
    }
    
    //setExpectations(_.runQuery, expectedRequest, expectedResponse)
    expecting {
      invoke(handler.runQuery(isEqualToExceptForQueryId(expectedRequest))).andReturn(expectedResponse)
    }

    execute {
      resource.runQuery(projectId, authenticationInfo, topicId, new OutputTypeSet(outputTypes), queryDef.toXml.toString)
    }
  }

  def testReadQueryInstances {
    val queryId = 999L

    val expectedRequest = new ReadQueryInstancesRequest(projectId, waitTimeMs, authenticationInfo, queryId)
    val expectedResponse = new ReadQueryInstancesResponse(queryId, "userId", "groupId", Seq.empty)

    setExpectations(_.readQueryInstances, expectedRequest, expectedResponse)
    
    execute {
      resource.readQueryInstances(projectId, authenticationInfo, queryId)
    }
  }
  
  def testReadInstanceResults {
    val instanceId = 123456789L
    
    val expectedRequest = new ReadInstanceResultsRequest(projectId, waitTimeMs, authenticationInfo, instanceId)
    val expectedResponse = new ReadInstanceResultsResponse(instanceId, Seq.empty)
    
    setExpectations(_.readInstanceResults, expectedRequest, expectedResponse)
    
    execute {
      resource.readInstanceResults(projectId, authenticationInfo, instanceId)
    }
  }

  def testReadPdo {
    val patientSetCollId = "123456789L"
    val optionsXml = <foo><bar/></foo>
    
    def paramResponse = new ParamResponse("foo", "bar", "baz")
      
    val expectedRequest = new ReadPdoRequest(projectId, waitTimeMs, authenticationInfo, patientSetCollId, optionsXml)
    val expectedResponse = new ReadPdoResponse(Seq(new EventResponse("event", "patient", None, None, Seq.empty)), Seq(new PatientResponse("patientId", Seq(paramResponse))),
      Seq(new ObservationResponse(None, "eventId", None, "patientId", None, None, None, "observerCode", "startDate", None, "valueTypeCode",None,None,None,None,None,None,None, Seq(paramResponse))))
    
    setExpectations(_.readPdo, expectedRequest, expectedResponse)
    
    execute {
      resource.readPdo(projectId, authenticationInfo, patientSetCollId, optionsXml.toString)
    }
  }

  def testReadQueryDefinition {
    val queryId = 123456789L
    
    def now = (new NetworkTime).getXMLGregorianCalendar
    
    val expectedRequest = new ReadQueryDefinitionRequest(projectId, waitTimeMs, authenticationInfo, queryId)
    val expectedResponse = new ReadQueryDefinitionResponse(queryId, "name", "userId", now, "<foo/>")
    
    setExpectations(_.readQueryDefinition, expectedRequest, expectedResponse)
    
    execute {
      resource.readQueryDefinition(projectId, authenticationInfo, queryId)
    }
  }

  def testDeleteQuery {
    val queryId = 123456789L
    
    val expectedRequest = new DeleteQueryRequest(projectId, waitTimeMs, authenticationInfo, queryId)
    
    val expectedResponse = new DeleteQueryResponse(queryId)
    
    setExpectations(_.deleteQuery, expectedRequest, expectedResponse)
    
    execute {
      resource.deleteQuery(projectId, authenticationInfo, queryId)
    }
  }

  def testRenameQuery {
    val queryId = 123456789L
    val queryName = "asjkdhkahsf"
    
    val expectedRequest = new RenameQueryRequest(projectId, waitTimeMs, authenticationInfo, queryId, queryName)
    
    val expectedResponse = new RenameQueryResponse(queryId, queryName)
    
    setExpectations(_.renameQuery, expectedRequest, expectedResponse)
    
    execute {
      resource.renameQuery(projectId, authenticationInfo, queryId, queryName)
    }
  }
  
  private def execute(f: => Unit) = whenExecuting(handler)(f)

  private def setExpectations[Req <: ShrineRequest, Resp <: ShrineResponse](handlerMethod: ShrineRequestHandler => Req => ShrineResponse, expectedRequest: Req, expectedResponse: Resp) {
    expecting {
      invoke(handlerMethod(handler)(isEqualTo(expectedRequest))).andReturn(expectedResponse)
    }
  }
}