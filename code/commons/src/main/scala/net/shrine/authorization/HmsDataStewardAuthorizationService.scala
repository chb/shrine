package net.shrine.authorization

import org.apache.commons.httpclient.auth.AuthScope
import org.apache.commons.httpclient.methods.{StringRequestEntity, PostMethod, GetMethod}
import org.apache.commons.httpclient.{HttpMethod, HttpStatus, UsernamePasswordCredentials, HttpClient}
import net.liftweb.json._
import edu.harvard.i2b2.crc.datavo.pm.UserType
import net.shrine.serializers.pm.{PMHttpClient, PMSerializer}
import net.shrine.protocol.{AuthenticationInfo, ReadApprovedQueryTopicsResponse, ApprovedTopic, RunQueryRequest, ReadApprovedQueryTopicsRequest}
import xml.{XML, Utility}
import java.util.regex.Matcher
import net.shrine.util.{XmlUtil, Loggable}

/**
 * @author Bill Simons
 * @date 1/30/12
 * @link http://cbmi.med.harvard.edu
 * @link http://chip.org
 *       <p/>
 *       NOTICE: This software comes with NO guarantees whatsoever and is
 *       licensed as Lgpl Open Source
 * @link http://www.gnu.org/licenses/lgpl.html
 */
class HmsDataStewardAuthorizationService(serviceUrl: String, username: String, password: String, pmHttpClient: PMHttpClient) extends QueryAuthorizationService with Loggable {

  import net.shrine.authorization.HmsDataStewardAuthorizationService._

  def identifyEcommonsUsername(authn: AuthenticationInfo): String = {
    val user: UserType = pmHttpClient.getUserConfiguration(authn.domain, authn.username, authn.credential.value)
    val ecommonsUsername: String = PMSerializer.extractEcommonsUsername(user)
    ecommonsUsername
  }

  def readApprovedEntries(request: ReadApprovedQueryTopicsRequest) = {
    val ecommonsUsername: String = identifyEcommonsUsername(request.authn)

    val responseString = getApprovedEntries(ecommonsUsername)
    val topics = parseApprovedTopics(responseString)
    new ReadApprovedQueryTopicsResponse(topics)
  }

  def authorizeRunQueryRequest(request: RunQueryRequest) {
    val ecommonsUsername = identifyEcommonsUsername(request.authn)
    val responseString = postAuthorizationRequest(ecommonsUsername, request.topicId, request.queryDefinitionXml)
    val isAuthorized = parseAuthorizationResponse(responseString)
    if(!isAuthorized) {
      throw new AuthorizationException("Requested topic is not approved")
    }
  }

  def getApprovedEntries(resource: String): String = {
    val method = new GetMethod(serviceUrl + resource)
    sendRequest(method)
  }

  def postAuthorizationRequest(username: String, topicId: String, queryText: String): String = {
    val escapedText: String = escapeQueryText(queryText)
    val method = new PostMethod(serviceUrl + "authorize/" + username + "/" + topicId)
    val requestEntity = new StringRequestEntity("{\"queryText\" : \"" + escapedText + "\"}", "text/json", null)
    method.setRequestEntity(requestEntity)
    sendRequest(method)
  }

  def sendRequest(method: HttpMethod): String = {
    val client = new HttpClient()
    client.getState().setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(username, password))
    method.setDoAuthentication(true)
    val responseString = try {
      val statusCode = client.executeMethod(method)
      if(statusCode != HttpStatus.SC_OK) {
        //TODO throw exception System.err.println("Method failed: " + method.getStatusLine());
      }

      method.getResponseBodyAsString()
    } finally {
      method.releaseConnection()
    }
    responseString

  }

}

object HmsDataStewardAuthorizationService {
  implicit val formats = DefaultFormats

  def parseAuthorizationResponse(responseString: String): Boolean = {
    val json = parse(responseString)
    (json \ "approved").extract[Boolean]
  }

  def parseApprovedTopics(responseString: String): Seq[ApprovedTopic] = {
    val json = parse(responseString)
    val transformedJson = json transform {
      case JField("id", x) => JField("queryTopicId", x)
      case JField("name", x) => JField("queryTopicName", x)
    }

    transformedJson.children map {
      _.extract[ApprovedTopic]
    }
  }

  def escapeQueryText(queryText: String): String = {
    val queryXml = XML.loadString(queryText)
    val trimmedXml = XmlUtil.stripWhitespace(queryXml)
    val escapedText = Utility.escape(trimmedXml.toString())
    escapedText.replace("\\","\\\\")
  }
}