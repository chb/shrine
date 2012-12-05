package net.shrine.client

import net.shrine.protocol._
import com.sun.jersey.api.client.WebResource
import javax.ws.rs.core.MediaType
import scala.xml.NodeSeq
import scala.xml.XML
import com.sun.jersey.api.client.RequestBuilder
import com.sun.jersey.api.client.UniformInterface
import net.shrine.protocol.query.QueryDefinition
import net.shrine.util.JerseyHttpClient.createJerseyClient
import java.io.StringReader
import java.net.{MalformedURLException, URL}
import net.shrine.util.Util


/**
 *
 * @author Clint Gilbert
 * @date Sep 16, 2011
 *
 * @link http://cbmi.med.harvard.edu
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

  private[client] lazy val webResource = createJerseyClient(acceptAllSslCerts).resource(shrineUrl)

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
    post[AggregatedRunQueryResponse] {
      webResource.path("/shrine/queries").header("outputTypes", OutputTypeSet(outputTypes).serialized).header("topicId", topicId).entity(queryDefinition.toXmlString, MediaType.APPLICATION_XML)
    }
  }

  override def readQueryInstances(queryId: Long) = {
    get[ReadQueryInstancesResponse] {
      webResource.path("/shrine/queries/" + queryId + "/instances")
    }
  }

  override def readInstanceResults(instanceId: Long) = {
    get[AggregatedReadInstanceResultsResponse] {
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
  
  override def readQueryResult(queryId: Long) = {
    get[AggregatedReadQueryResultResponse] {
      webResource.path("/shrine/queries/" + queryId + "/results")
    }
  } 

  private type WebResourceLike = RequestBuilder[WebResource#Builder] with UniformInterface

  //TODO: it would be nice to be able to test post(), get(), and delete()
  private def post[T: Deserializer](webResource: => WebResourceLike): T = perform[T](webResource, _.post(classOf[String]))

  private def get[T: Deserializer](webResource: => WebResourceLike): T = perform[T](webResource, _.get(classOf[String]))

  private def delete[T: Deserializer](webResource: => WebResourceLike): T = perform[T](webResource, _.delete(classOf[String]))

  private[client] def perform[T: Deserializer](webResource: WebResourceLike, httpVerb: UniformInterface => String): T = {

    val withNeededHeaders = webResource.header("Authorization", authorization.toHeader).header("projectId", projectId)

    val xml = XML.loadString(httpVerb(withNeededHeaders))

    val deserialize = implicitly[Deserializer[T]]

    deserialize(xml)
  }
}

object JerseyShrineClient {
  //package-private for testing
  def isValidUrl(url: String): Boolean = {
    try {
      new URL(url)

      true
    } catch {
      case e: MalformedURLException => false
    }
  }

  private[client] trait Deserializer[T] extends (NodeSeq => T)

  private[client] object Deserializer {
    private def toDeserializer[T](f: NodeSeq => T) = new Deserializer[T] {
      override def apply(xml: NodeSeq): T = f(xml) 
    }
    
    private[client] implicit val aggregatedReadQueryResultResponseDeserializer: Deserializer[AggregatedReadQueryResultResponse] = toDeserializer(AggregatedReadQueryResultResponse.fromXml)
    
    private[client] implicit val aggregatedRunQueryResponseDeserializer: Deserializer[AggregatedRunQueryResponse] = toDeserializer(AggregatedRunQueryResponse.fromXml)

    private[client] implicit val readPreviousQueriesResponseDeserializer: Deserializer[ReadPreviousQueriesResponse] = toDeserializer(ReadPreviousQueriesResponse.fromXml)

    private[client] implicit val readApprovedQueryTopicsResponseDeserializer: Deserializer[ReadApprovedQueryTopicsResponse] = toDeserializer(ReadApprovedQueryTopicsResponse.fromXml)

    private[client] implicit val readQueryInstancesResponseDeserializer: Deserializer[ReadQueryInstancesResponse] = toDeserializer(ReadQueryInstancesResponse.fromXml)

    private[client] implicit val aggregatedReadInstanceResultsResponseDeserializer: Deserializer[AggregatedReadInstanceResultsResponse] = toDeserializer(AggregatedReadInstanceResultsResponse.fromXml)

    private[client] implicit val readPdoResponseDeserializer: Deserializer[ReadPdoResponse] = toDeserializer(ReadPdoResponse.fromXml)

    private[client] implicit val readQueryDefinitionResponseDeserializer: Deserializer[ReadQueryDefinitionResponse] = toDeserializer(ReadQueryDefinitionResponse.fromXml)

    private[client] implicit val deleteQueryResponseDeserializer: Deserializer[DeleteQueryResponse] = toDeserializer(DeleteQueryResponse.fromXml)

    private[client] implicit val renameQueryResponseDeserializer: Deserializer[RenameQueryResponse] = toDeserializer(RenameQueryResponse.fromXml)
  }
}
