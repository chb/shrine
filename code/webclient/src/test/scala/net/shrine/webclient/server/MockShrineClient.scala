package net.shrine.webclient.server

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
import net.shrine.webclient.shared.domain.SingleInstitutionQueryResult
import net.shrine.client.ShrineClient
import net.shrine.protocol.AggregatedRunQueryResponse
import net.shrine.protocol.AggregatedReadInstanceResultsResponse
import net.shrine.protocol.AggregatedReadQueryResultResponse

/**
 * @author clint
 * @date May 22, 2012
 */
final case class MockShrineClient(toReturn: Map[String, SingleInstitutionQueryResult]) extends ShrineClient {
  var queryDefinition: QueryDefinition = _

  override def readApprovedQueryTopics(userId: String): ReadApprovedQueryTopicsResponse = null

  override def readPreviousQueries(userId: String, fetchSize: Int): ReadPreviousQueriesResponse = null

  override def runQuery(topicId: String, outputTypes: Set[ResultOutputType], queryDef: QueryDefinition): AggregatedRunQueryResponse = {
    this.queryDefinition = queryDef

    val now = NetworkTime.makeXMLGregorianCalendar(new Date)

    val queryResults = toReturn.map { 
      case (instName, instResult) => {
        val statusType = if(instResult.isError) QueryResult.StatusType.Error else QueryResult.StatusType.Finished
        
        QueryResult(123L, 456L, Option(ResultOutputType.PATIENT_COUNT_XML), instResult.getCount, Some(now), Some(now), Some(instName), statusType, None)
      }
    }

    AggregatedRunQueryResponse(987L, now, "some-user-id", "some-group-id", queryDefinition, 42L, queryResults.toSeq)
  }

  override def readQueryInstances(queryId: Long): ReadQueryInstancesResponse = null

  override def readInstanceResults(instanceId: Long): AggregatedReadInstanceResultsResponse = null

  override def readPdo(patientSetCollId: String, optionsXml: NodeSeq): ReadPdoResponse = null

  override def readQueryDefinition(queryId: Long): ReadQueryDefinitionResponse = null

  override def deleteQuery(queryId: Long): DeleteQueryResponse = null

  override def renameQuery(queryId: Long, queryName: String): RenameQueryResponse = null
  
  override def readQueryResult(queryId: Long): AggregatedReadQueryResultResponse = null
}