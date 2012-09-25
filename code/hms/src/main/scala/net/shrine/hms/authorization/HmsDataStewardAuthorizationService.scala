package net.shrine.hms.authorization

import org.apache.commons.httpclient.auth.AuthScope
import org.apache.commons.httpclient.methods.{StringRequestEntity, PostMethod, GetMethod}
import org.apache.commons.httpclient.{HttpMethod, HttpStatus, UsernamePasswordCredentials, HttpClient => CommonsHttpClient}
import net.liftweb.json._
import net.shrine.protocol.{AuthenticationInfo, ReadApprovedQueryTopicsResponse, ApprovedTopic, RunQueryRequest, ReadApprovedQueryTopicsRequest}
import xml.{XML, Utility}
import net.shrine.util.{XmlUtil, Loggable}
import net.shrine.util.HttpClient 
import net.shrine.i2b2.protocol.pm.{User, GetUserConfigurationRequest}
import net.shrine.authorization.{AuthorizationException, QueryAuthorizationService}

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
class HmsDataStewardAuthorizationService(serviceUrl: String, username: String, password: String, pmEndpoint: String, httpClient: HttpClient) extends QueryAuthorizationService with Loggable {

  import net.shrine.hms.authorization.HmsDataStewardAuthorizationService._

  def identifyEcommonsUsername(authn: AuthenticationInfo): String = {
    val pmRequest = new GetUserConfigurationRequest(authn.domain, authn.username, authn.credential.value)
    val responseXml: String = httpClient.post(pmRequest.toI2b2String, pmEndpoint)
    User.fromI2b2(responseXml).params("ecommons_username")
  }


  def readApprovedEntries(request: ReadApprovedQueryTopicsRequest) = {
    val ecommonsUsername: String = identifyEcommonsUsername(request.authn)

    val responseString = getApprovedEntries(ecommonsUsername)
    val topics = parseApprovedTopics(responseString)
    new ReadApprovedQueryTopicsResponse(topics)
  }

  def authorizeRunQueryRequest(request: RunQueryRequest) {
    val ecommonsUsername = identifyEcommonsUsername(request.authn)
    val responseString = postAuthorizationRequest(ecommonsUsername, request.topicId, request.queryDefinition.toI2b2.toString)
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
    val client = new CommonsHttpClient()
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

object HmsDataStewardAuthorizationService extends Loggable {

  implicit val formats = DefaultFormats

  def parseAuthorizationResponse(responseString: String): Boolean = {
    val json = parseJson(responseString)
    json match {
      case Some(x) => (x \ "approved").extract[Boolean]
      case None => false
    }
  }

  def parseApprovedTopics(responseString: String): Seq[ApprovedTopic] = {
    val json = parseJson(responseString)
    json match {
      case Some(x) => {
        val transformedJson = x transform {
          case JField("id", x) => JField("queryTopicId", x)
          case JField("name", x) => JField("queryTopicName", x)
        }

        return transformedJson.children map {
          _.extract[ApprovedTopic]
        }
      }
      case None => Seq.empty
    }
  }

  def parseJson(jsonString: String): Option[JValue] = {
    try {
      val json = parse(jsonString)
      Some[JValue](json)
    } catch {
      case e: Exception => {
        error(String.format("Exception %s parsing JSON response with stack trace:\r\n%s\r\n with response %s", e.toString, e.getStackTraceString, jsonString))
        None
      }
    }
  }

  def escapeQueryText(queryText: String): String = {
    val queryXml = XML.loadString(queryText)
    val trimmedXml = XmlUtil.stripWhitespace(queryXml)
    val escapedText = Utility.escape(trimmedXml.toString())
    escapedText.replace("\\", "\\\\")
  }
}