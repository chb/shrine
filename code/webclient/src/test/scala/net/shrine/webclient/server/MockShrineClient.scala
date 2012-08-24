package net.shrine.webclient.server

import net.shrine.service.ShrineClient
import net.shrine.protocol.query.QueryDefinition
import net.shrine.protocol.ReadApprovedQueryTopicsResponse
import net.shrine.protocol.ReadQueryDefinitionResponse
import net.shrine.protocol.ResultOutputType
import org.spin.tools.NetworkTime
import net.shrine.protocol.ReadPdoResponse
import net.shrine.protocol.ReadInstanceResultsResponse
import net.shrine.protocol.RenameQueryResponse
import net.shrine.protocol.ReadQueryInstancesResponse
import net.shrine.protocol.DeleteQueryResponse
import net.shrine.protocol.RunQueryResponse
import net.shrine.protocol.ReadPreviousQueriesResponse
import scala.xml.NodeSeq
import net.shrine.protocol.QueryResult
import java.util.Date

/**
 * @author clint
 * @date May 22, 2012
 */
final case class MockShrineClient(toReturn: Map[String, Int]) extends ShrineClient {
  var queryDefinition: QueryDefinition = _

  def readApprovedQueryTopics(userId: String): ReadApprovedQueryTopicsResponse = null

  def readPreviousQueries(userId: String, fetchSize: Int): ReadPreviousQueriesResponse = null

  def runQuery(topicId: String, outputTypes: Set[ResultOutputType], queryDef: QueryDefinition): RunQueryResponse = {
    this.queryDefinition = queryDef

    val now = NetworkTime.makeXMLGregorianCalendar(new Date)

    val queryResults = toReturn.map { case (instName, count) =>
      QueryResult(123L, 456L, Option(ResultOutputType.PATIENT_COUNT_XML), count.toLong, Some(now), Some(now), Some(instName), "some-status-type", None)
    }

    RunQueryResponse(987L, now, "some-user-id", "some-group-id", queryDefinition, 42L, queryResults.toSeq)
  }

  def readQueryInstances(queryId: Long): ReadQueryInstancesResponse = null

  def readInstanceResults(instanceId: Long): ReadInstanceResultsResponse = null

  def readPdo(patientSetCollId: String, optionsXml: NodeSeq): ReadPdoResponse = null

  def readQueryDefinition(queryId: Long): ReadQueryDefinitionResponse = null

  def deleteQuery(queryId: Long): DeleteQueryResponse = null

  def renameQuery(queryId: Long, queryName: String): RenameQueryResponse = null
}