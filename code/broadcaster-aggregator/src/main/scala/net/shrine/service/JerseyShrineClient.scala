package net.shrine.service

import java.net.MalformedURLException
import java.net.URL
import net.shrine.protocol.AuthenticationInfo
import net.shrine.protocol.ReadApprovedQueryTopicsResponse
import net.shrine.protocol.ReadPreviousQueriesResponse
import net.shrine.protocol.ResultOutputType
import net.shrine.protocol.RunQueryResponse
import com.sun.jersey.api.client.Client
import com.sun.jersey.api.client.WebResource
import javax.ws.rs.core.MediaType
import scala.xml.NodeSeq
import scala.xml.XML
import java.io.StringReader
import net.shrine.protocol.ReadQueryInstancesResponse
import net.shrine.protocol.ReadInstanceResultsRequest
import net.shrine.protocol.ReadInstanceResultsResponse
import net.shrine.protocol.ReadPdoResponse
import net.shrine.protocol.ReadQueryDefinitionResponse
import net.shrine.protocol.DeleteQueryResponse
import net.shrine.protocol.RenameQueryResponse
import com.sun.jersey.api.client.config.ClientConfig
import com.sun.jersey.api.client.config.DefaultClientConfig
import com.sun.jersey.client.urlconnection.HTTPSProperties
import javax.net.ssl.SSLSession
import javax.net.ssl.HostnameVerifier
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager
import java.security.cert.X509Certificate
import javax.net.ssl.SSLContext
import java.security.SecureRandom
import com.sun.jersey.api.client.RequestBuilder
import com.sun.jersey.api.client.UniformInterface
import net.shrine.protocol.query.QueryDefinition
import net.shrine.util.HTTPClient.createJerseyClient

/**
 *
 * @author Clint Gilbert
 * @date Sep 16, 2011
 *
 * @link http://cbmi.med.harvard.edu
 *
 * This software is licensed under the LGPL
 * @link http://www.gnu.org/licenses/lgpl.html
 *
 * A client for remote ShrineResources, implemented using Jersey
 *
 * @param shrineUrl: The base URL that the remote ShrineResource is exposed at
 */
final class JerseyShrineClient(val shrineUrl: String, val projectId: String, val authorization: AuthenticationInfo, val acceptAllSslCerts: Boolean = false) extends ShrineClient {
  import JerseyShrineClient._

  require(shrineUrl != null)
  require(isValidUrl(shrineUrl))
  require(projectId != null)
  require(authorization != null)

  private[service] lazy val webResource = createJerseyClient(acceptAllSslCerts).resource(shrineUrl)

  override def readApprovedQueryTopics(userId: String) = {
    get[ReadApprovedQueryTopicsResponse] {
      webResource.path("/shrine/" + userId + "/approved-topics")
    }
  }

  override def readPreviousQueries(userId: String, fetchSize: Int) = {
    get[ReadPreviousQueriesResponse] {
      webResource.path("/shrine/" + userId + "/queries").queryParam("fetchSize", fetchSize.toString)
    }
  }

  override def runQuery(topicId: String, outputTypes: Set[ResultOutputType], queryDefinition: QueryDefinition) = {
    post[RunQueryResponse] { 
      webResource.path("/shrine/queries").header("outputTypes", OutputTypeSet(outputTypes).serialized).header("topicId", topicId).entity(queryDefinition.toXmlString, MediaType.APPLICATION_XML)
    }
  }

  override def readQueryInstances(queryId: Long) = {
    get[ReadQueryInstancesResponse] {
      webResource.path("/shrine/queries/" + queryId + "/instances")
    }
  }

  override def readInstanceResults(instanceId: Long) = {
    get[ReadInstanceResultsResponse] {
      webResource.path("/shrine/instances/" + instanceId + "/results")
    }
  }

  override def readPdo(patientSetCollId: String, optionsXml: NodeSeq) = {
    post[ReadPdoResponse] {
      webResource.path("/shrine/patient-set/" + patientSetCollId).entity(optionsXml.toString, MediaType.APPLICATION_XML)
    }
  }

  override def readQueryDefinition(queryId: Long) = {
    get[ReadQueryDefinitionResponse] {
      webResource.path("/shrine/queries/" + queryId)
    }
  }

  override def deleteQuery(queryId: Long) = {
    delete[DeleteQueryResponse] {
      webResource.path("/shrine/queries/" + queryId)
    }
  }

  override def renameQuery(queryId: Long, queryName: String) = {
    post[RenameQueryResponse] {
      webResource.path("/shrine/queries/" + queryId + "/name").entity(queryName, MediaType.TEXT_PLAIN)
    }
  }

  private type WebResourceLike = RequestBuilder[WebResource#Builder] with UniformInterface

  //TODO: it would be nice to be able to test post(), get(), and delete()
  private def post[T: Deserializer](webResource: => WebResourceLike): T = perform[T](webResource, _.post(classOf[String]))

  private def get[T: Deserializer](webResource: => WebResourceLike): T = perform[T](webResource, _.get(classOf[String]))

  private def delete[T: Deserializer](webResource: => WebResourceLike): T = perform[T](webResource, _.delete(classOf[String]))

  private[service] def perform[T: Deserializer](webResource: WebResourceLike, httpVerb: UniformInterface => String): T = {
    
    val withNeededHeaders = webResource.header("Authorization", authorization.toHeader).header("projectId", projectId)

    val xml = XML.load(new StringReader(httpVerb(withNeededHeaders)))

    val deserialize = implicitly[Deserializer[T]]

    deserialize(xml)
  }
}

object JerseyShrineClient {
  //package-private for testing
  private[service] def isValidUrl(url: String): Boolean = {
    try {
      new URL(url)

      true
    } catch {
      case e: MalformedURLException => false
    }
  }

  private type Deserializer[T] = NodeSeq => T

  private[service] implicit val runQueryResponseDeserializer: Deserializer[RunQueryResponse] = RunQueryResponse.fromXml

  private[service] implicit val readPreviousQueriesResponseDeserializer: Deserializer[ReadPreviousQueriesResponse] = ReadPreviousQueriesResponse.fromXml

  private[service] implicit val readApprovedQueryTopicsResponseDeserializer: Deserializer[ReadApprovedQueryTopicsResponse] = ReadApprovedQueryTopicsResponse.fromXml

  private[service] implicit val readQueryInstancesResponseDeserializer: Deserializer[ReadQueryInstancesResponse] = ReadQueryInstancesResponse.fromXml

  private[service] implicit val readInstanceResultsResponseDeserializer: Deserializer[ReadInstanceResultsResponse] = ReadInstanceResultsResponse.fromXml

  private[service] implicit val readPdoResponseDeserializer: Deserializer[ReadPdoResponse] = ReadPdoResponse.fromXml

  private[service] implicit val readQueryDefinitionResponseDeserializer: Deserializer[ReadQueryDefinitionResponse] = ReadQueryDefinitionResponse.fromXml

  private[service] implicit val deleteQueryResponseDeserializer: Deserializer[DeleteQueryResponse] = DeleteQueryResponse.fromXml

  private[service] implicit val renameQueryResponseDeserializer: Deserializer[RenameQueryResponse] = RenameQueryResponse.fromXml
}
