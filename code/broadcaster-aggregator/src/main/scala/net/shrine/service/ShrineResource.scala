package net.shrine.service

import java.net.URLDecoder.decode
import java.net.URLEncoder.encode
import scala.Array.canBuildFrom
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component
import ShrineResource.waitTimeMs
import javax.ws.rs.Consumes
import javax.ws.rs.GET
import javax.ws.rs.HeaderParam
import javax.ws.rs.POST
import javax.ws.rs.Path
import javax.ws.rs.PathParam
import javax.ws.rs.Produces
import javax.ws.rs.QueryParam
import javax.ws.rs.core.MediaType
import net.shrine.protocol._
import net.shrine.service.annotation.RequestHandler
import scala.xml.NodeSeq
import java.io.StringReader
import javax.ws.rs.DELETE
import net.shrine.protocol.query.QueryDefinition

/**
 * @author Bill Simons
 * @author Clint Gilbert
 * @date 8/30/11
 * @link http://cbmi.med.harvard.edu
 * @link http://chip.org
 *       <p/>
 *       NOTICE: This software comes with NO guarantees whatsoever and is
 *       licensed as Lgpl Open Source
 * @link http://www.gnu.org/licenses/lgpl.html
 */
@Path("/shrine")
@Produces(Array(MediaType.APPLICATION_XML))
@Component
@Scope("singleton")
//shrineRequestHandler constructor param needs to be annotated with @RequestHandler (@ShrineRequestHandler)
//So Jersey/JAXRS will be able to inject a mock ShrineRequestHandler when running tests of this class in an
//embedded HTTP server. :/
final class ShrineResource @Autowired() (@RequestHandler private val shrineRequestHandler: ShrineRequestHandler) {
  import ShrineResource.waitTimeMs  
  
  @GET
  @Path("{userId}/approved-topics")
  def readApprovedQueryTopics(
      @HeaderParam("projectId") projectId: String,
      //authorization will be constructed by JAXRS using the String value of the 'Authorization' header
      @HeaderParam("Authorization") authorization: AuthenticationInfo,
      @PathParam("userId") userId: String): String = {
    performAndSerialize(_.readApprovedQueryTopics(new ReadApprovedQueryTopicsRequest(projectId, waitTimeMs, authorization, userId)))
  }

  @GET
  @Path("{userId}/queries")
  def readPreviousQueries(
      @HeaderParam("projectId") projectId: String,
      //authorization will be constructed by JAXRS using the String value of the 'Authorization' header
      @HeaderParam("Authorization") authorization: AuthenticationInfo,
      @PathParam("userId") userId: String,
      @QueryParam("fetchSize") fetchSize: Int): String = {
    val fSize = if(fetchSize != 0) fetchSize else 20
    performAndSerialize(_.readPreviousQueries(new ReadPreviousQueriesRequest(projectId, waitTimeMs, authorization, userId, fSize)))
  }

  @POST
  @Path("/queries")
  @Consumes(Array(MediaType.APPLICATION_XML))
  def runQuery(
      @HeaderParam("projectId") projectId: String,
      //authorization will be constructed by JAXRS using the String value of the 'Authorization' header
      @HeaderParam("Authorization") authorization: AuthenticationInfo,
      @HeaderParam("topicId") topicId: String,
      //outputTypes will be constructed by JAXRS using the String value of the 'outputTypes' header
      @HeaderParam("outputTypes") outputTypes: OutputTypeSet,
      queryDefinitionXml: String): String = {
    val queryDef = QueryDefinition.fromXml(queryDefinitionXml).get //TODO: remove fragile .get call
    performAndSerialize(_.runQuery(new RunQueryRequest(projectId, waitTimeMs, authorization, topicId, outputTypes.toSet, queryDef)))
  }
  
  @GET
  @Path("/queries/{queryId}/instances")
  def readQueryInstances(
      @HeaderParam("projectId") projectId: String,
      //authorization will be constructed by JAXRS using the String value of the 'Authorization' header
      @HeaderParam("Authorization") authorization: AuthenticationInfo,
      @PathParam("queryId") queryId: Long): String = {
    performAndSerialize(_.readQueryInstances(new ReadQueryInstancesRequest(projectId, waitTimeMs, authorization, queryId)))
  }
  
  @GET
  @Path("/instances/{instanceId}/results")
  def readInstanceResults(
      @HeaderParam("projectId") projectId: String,
      //authorization will be constructed by JAXRS using the String value of the 'Authorization' header
      @HeaderParam("Authorization") authorization: AuthenticationInfo,
      @PathParam("instanceId") instanceId: Long): String = {
    performAndSerialize(_.readInstanceResults(new ReadInstanceResultsRequest(projectId, waitTimeMs, authorization, instanceId))) 
  }
  
  @POST //This must be POST, since we're sending content in the request body
  @Path("/patient-set/{patientSetCollId}")
  @Consumes(Array(MediaType.APPLICATION_XML))
  def readPdo(
      @HeaderParam("projectId") projectId: String, 
      //authorization will be constructed by JAXRS using the String value of the 'Authorization' header
      @HeaderParam("Authorization") authorization: AuthenticationInfo, 
      @PathParam("patientSetCollId") patientSetCollId: String, 
      optionsXml: String): String = {
    performAndSerialize(_.readPdo(new ReadPdoRequest(projectId, waitTimeMs, authorization, patientSetCollId, slurpXml(optionsXml))))
  }
  
  @GET
  @Path("/queries/{queryId}")
  def readQueryDefinition(
      @HeaderParam("projectId") projectId: String, 
      //authorization will be constructed by JAXRS using the String value of the 'Authorization' header
      @HeaderParam("Authorization") authorization: AuthenticationInfo,
      @PathParam("queryId") queryId: Long): String = {
    performAndSerialize(_.readQueryDefinition(new ReadQueryDefinitionRequest(projectId, waitTimeMs, authorization, queryId)))
  }

  @DELETE
  @Path("/queries/{queryId}")
  def deleteQuery(
      @HeaderParam("projectId") projectId: String, 
      //authorization will be constructed by JAXRS using the String value of the 'Authorization' header
      @HeaderParam("Authorization") authorization: AuthenticationInfo,
      @PathParam("queryId") queryId: Long): String = {
    performAndSerialize(_.deleteQuery(new DeleteQueryRequest(projectId, waitTimeMs, authorization, queryId)))
  }

  @POST
  @Path("/queries/{queryId}/name")
  @Consumes(Array(MediaType.TEXT_PLAIN))
  def renameQuery(
      @HeaderParam("projectId") projectId: String, 
      //authorization will be constructed by JAXRS using the String value of the 'Authorization' header
      @HeaderParam("Authorization") authorization: AuthenticationInfo,
      @PathParam("queryId") queryId: Long,
      queryName: String): String = {
    performAndSerialize(_.renameQuery(new RenameQueryRequest(projectId, waitTimeMs, authorization, queryId, queryName)))
  }
  
  private def slurpXml(xml: String): NodeSeq = scala.xml.XML.load(new StringReader(xml))
  
  private def performAndSerialize[R <: ShrineResponse](op: ShrineRequestHandler => R): String = {
    op(shrineRequestHandler).toXmlString
  }
}

object ShrineResource {
  val waitTimeMs = 10000
}

