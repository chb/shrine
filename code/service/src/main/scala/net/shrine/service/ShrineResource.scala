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
import scala.xml.XML
import javax.ws.rs.core.Response

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
@Scope("singleton") //shrineRequestHandler constructor param needs to be annotated with @RequestHandler (@ShrineRequestHandler)
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
    @PathParam("userId") userId: String,
    @HeaderParam("shouldBroadcast") shouldBroadcast: Boolean): String = {

    performAndSerialize(_.readApprovedQueryTopics(new ReadApprovedQueryTopicsRequest(projectId, waitTimeMs, authorization, userId), shouldBroadcast))
  }

  @GET
  @Path("{userId}/queries")
  def readPreviousQueries(
    @HeaderParam("projectId") projectId: String,
    //authorization will be constructed by JAXRS using the String value of the 'Authorization' header
    @HeaderParam("Authorization") authorization: AuthenticationInfo,
    @PathParam("userId") userId: String,
    @QueryParam("fetchSize") fetchSize: Int,
    @HeaderParam("shouldBroadcast") shouldBroadcast: Boolean): Response = {

    if (userId != authorization.username) {
      Response.status(403).build
    } else {
      val fSize = if (fetchSize != 0) fetchSize else 20

      Response.ok.entity {
        performAndSerialize(_.readPreviousQueries(new ReadPreviousQueriesRequest(projectId, waitTimeMs, authorization, userId, fSize), shouldBroadcast))
      }.build
    }
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
    queryDefinitionXml: String,
    @HeaderParam("shouldBroadcast") shouldBroadcast: Boolean): String = {

    val queryDef = QueryDefinition.fromXml(queryDefinitionXml).get

    //NB: Create the RunQueryRequest with a dummy networkQueryId of '-1'; 
    //this will be filled in with an appropriately-generated value by the ShrineRequestHandler
    performAndSerialize(_.runQuery(new RunQueryRequest(projectId, waitTimeMs, authorization, -1, topicId, outputTypes.toSet, queryDef), shouldBroadcast))
  }

  @GET
  @Path("/queries/{queryId}/instances")
  def readQueryInstances(
    @HeaderParam("projectId") projectId: String,
    //authorization will be constructed by JAXRS using the String value of the 'Authorization' header
    @HeaderParam("Authorization") authorization: AuthenticationInfo,
    @PathParam("queryId") queryId: Long,
    @HeaderParam("shouldBroadcast") shouldBroadcast: Boolean): String = {

    performAndSerialize(_.readQueryInstances(new ReadQueryInstancesRequest(projectId, waitTimeMs, authorization, queryId), shouldBroadcast))
  }

  @GET
  @Path("/instances/{instanceId}/results")
  def readInstanceResults(
    @HeaderParam("projectId") projectId: String,
    //authorization will be constructed by JAXRS using the String value of the 'Authorization' header
    @HeaderParam("Authorization") authorization: AuthenticationInfo,
    @PathParam("instanceId") instanceId: Long,
    @HeaderParam("shouldBroadcast") shouldBroadcast: Boolean): String = {

    performAndSerialize(_.readInstanceResults(new ReadInstanceResultsRequest(projectId, waitTimeMs, authorization, instanceId), shouldBroadcast))
  }

  @POST //This must be POST, since we're sending content in the request body
  @Path("/patient-set/{patientSetCollId}")
  @Consumes(Array(MediaType.APPLICATION_XML))
  def readPdo(
    @HeaderParam("projectId") projectId: String,
    //authorization will be constructed by JAXRS using the String value of the 'Authorization' header
    @HeaderParam("Authorization") authorization: AuthenticationInfo,
    @PathParam("patientSetCollId") patientSetCollId: String,
    optionsXml: String,
    @HeaderParam("shouldBroadcast") shouldBroadcast: Boolean): String = {

    import XML.loadString

    performAndSerialize(_.readPdo(new ReadPdoRequest(projectId, waitTimeMs, authorization, patientSetCollId, loadString(optionsXml)), shouldBroadcast))
  }

  @GET
  @Path("/queries/{queryId}")
  def readQueryDefinition(
    @HeaderParam("projectId") projectId: String,
    //authorization will be constructed by JAXRS using the String value of the 'Authorization' header
    @HeaderParam("Authorization") authorization: AuthenticationInfo,
    @PathParam("queryId") queryId: Long,
    @HeaderParam("shouldBroadcast") shouldBroadcast: Boolean): String = {

    performAndSerialize(_.readQueryDefinition(new ReadQueryDefinitionRequest(projectId, waitTimeMs, authorization, queryId), shouldBroadcast))
  }

  @DELETE
  @Path("/queries/{queryId}")
  def deleteQuery(
    @HeaderParam("projectId") projectId: String,
    //authorization will be constructed by JAXRS using the String value of the 'Authorization' header
    @HeaderParam("Authorization") authorization: AuthenticationInfo,
    @PathParam("queryId") queryId: Long,
    @HeaderParam("shouldBroadcast") shouldBroadcast: Boolean): String = {

    performAndSerialize(_.deleteQuery(new DeleteQueryRequest(projectId, waitTimeMs, authorization, queryId), shouldBroadcast))
  }

  @POST
  @Path("/queries/{queryId}/name")
  @Consumes(Array(MediaType.TEXT_PLAIN))
  def renameQuery(
    @HeaderParam("projectId") projectId: String,
    //authorization will be constructed by JAXRS using the String value of the 'Authorization' header
    @HeaderParam("Authorization") authorization: AuthenticationInfo,
    @PathParam("queryId") queryId: Long,
    queryName: String,
    @HeaderParam("shouldBroadcast") shouldBroadcast: Boolean): String = {

    performAndSerialize(_.renameQuery(new RenameQueryRequest(projectId, waitTimeMs, authorization, queryId, queryName), shouldBroadcast))
  }

  @GET
  @Path("/queries/{queryId}/results")
  @Consumes(Array(MediaType.TEXT_PLAIN))
  def readQueryResults(
    @HeaderParam("projectId") projectId: String,
    //authorization will be constructed by JAXRS using the String value of the 'Authorization' header
    @HeaderParam("Authorization") authorization: AuthenticationInfo,
    @PathParam("queryId") queryId: Long,
    @HeaderParam("shouldBroadcast") shouldBroadcast: Boolean): String = {

    performAndSerialize(_.readQueryResult(new ReadQueryResultRequest(projectId, waitTimeMs, authorization, queryId), shouldBroadcast))
  }

  private def performAndSerialize[R <: ShrineResponse](op: ShrineRequestHandler => R): String = {
    op(shrineRequestHandler).toXmlString
  }
}

object ShrineResource {
  val waitTimeMs = 10000
}

