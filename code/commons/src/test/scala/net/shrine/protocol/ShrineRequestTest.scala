package net.shrine.protocol

import junit.framework.TestCase
import org.scalatest.junit.ShouldMatchersForJUnit
import org.junit.Test
import scala.xml.NodeSeq
import net.shrine.protocol.query.QueryDefinition
import net.shrine.protocol.query.Term

/**
 * @author clint
 * @date Mar 22, 2013
 */
final class ShrineRequestTest extends TestCase with ShouldMatchersForJUnit {
  @Test
  def testFromXmlThrowsOnBadInput {
    intercept[Exception] {
      ShrineRequest.fromXml("jksahdjkashdjkashdjkashdjksad")
    }
  }
  
  @Test
  def testFromI2b2ThrowsOnBadInput {
    intercept[Exception] {
      ShrineRequest.fromI2b2("jksahdjkashdjkashdjkashdjksad")
    }
  }
  
  @Test
  def testFromXml {
    val projectId = "salkdjksaljdkla"
    val waitTimeMs = 98374L
    val userId = "foo-user"
    val authn = AuthenticationInfo("blarg-domain", userId, Credential("sajkhdkjsadh", true))
    val queryId = 485794359L
    val patientSetCollId = "ksaldjksal"
    val optionsXml: NodeSeq = <request><foo>x</foo></request>
    val fetchSize = 12345
    val queryName = "saljkd;salda"
    val topicId = "saldjkasljdasdsadsadasdas"
    val outputTypes = ResultOutputType.nonBreakdownTypes.toSet
    val queryDefinition = QueryDefinition(queryName, Term("oiweruoiewkldfhsofi"))
    val localResultId = "aoiduaojsdpaojcmsal"
    
    def doMarshallingRoundTrip(req: ShrineRequest) {
      val xml = req.toXmlString
      
      val unmarshalled = ShrineRequest.fromXml(xml)
      
      req match {
        //NB: Special handling of ReadPdoRequest due to fiddly serialization and equality issues with its NodeSeq field. :( :(
        case readPdoRequest: ReadPdoRequest => {
          val unmarshalledReadPdoRequest = unmarshalled.asInstanceOf[ReadPdoRequest]
          
          readPdoRequest.projectId should equal(unmarshalledReadPdoRequest.projectId)
          readPdoRequest.waitTimeMs should equal(unmarshalledReadPdoRequest.waitTimeMs)
          readPdoRequest.authn should equal(unmarshalledReadPdoRequest.authn)
          readPdoRequest.patientSetCollId should equal(unmarshalledReadPdoRequest.patientSetCollId)
          //NB: Ugh :(
          readPdoRequest.optionsXml.toString should equal(unmarshalledReadPdoRequest.optionsXml.toString)
        }
        case _ => unmarshalled should equal(req)
      }
    }
    
    doMarshallingRoundTrip(ReadQueryResultRequest(projectId, waitTimeMs, authn, queryId))
    doMarshallingRoundTrip(DeleteQueryRequest(projectId, waitTimeMs, authn, queryId))
    doMarshallingRoundTrip(ReadApprovedQueryTopicsRequest(projectId, waitTimeMs, authn, userId))
    doMarshallingRoundTrip(ReadInstanceResultsRequest(projectId, waitTimeMs, authn, queryId))
    doMarshallingRoundTrip(ReadPdoRequest(projectId, waitTimeMs, authn, patientSetCollId, optionsXml))
    doMarshallingRoundTrip(ReadPreviousQueriesRequest(projectId, waitTimeMs, authn, userId, fetchSize))
    doMarshallingRoundTrip(ReadQueryDefinitionRequest(projectId, waitTimeMs, authn, queryId))
    doMarshallingRoundTrip(ReadQueryInstancesRequest(projectId, waitTimeMs, authn, queryId))
    doMarshallingRoundTrip(RenameQueryRequest(projectId, waitTimeMs, authn, queryId, queryName))
    doMarshallingRoundTrip(RunQueryRequest(projectId, waitTimeMs, authn, queryId, topicId, outputTypes, queryDefinition))
    doMarshallingRoundTrip(ReadResultRequest(projectId, waitTimeMs, authn, localResultId))
  }
}