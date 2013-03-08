package net.shrine.utilities.scanner

import net.shrine.client.ShrineClient
import net.shrine.protocol.ResultOutputType
import scala.xml.NodeSeq
import net.shrine.protocol.ReadApprovedQueryTopicsResponse
import net.shrine.protocol.ReadPreviousQueriesResponse
import net.shrine.protocol.AggregatedRunQueryResponse
import net.shrine.protocol.ReadQueryInstancesResponse
import net.shrine.protocol.AggregatedReadInstanceResultsResponse
import net.shrine.protocol.ReadPdoResponse
import net.shrine.protocol.ReadQueryDefinitionResponse
import net.shrine.protocol.DeleteQueryResponse
import net.shrine.protocol.RenameQueryResponse
import net.shrine.protocol.AggregatedReadQueryResultResponse
import net.shrine.protocol.query.QueryDefinition

/**
 * @author clint
 * @date Mar 7, 2013
 */
abstract class ShrineClientAdapter extends ShrineClient {
  override def readApprovedQueryTopics(userId: String): ReadApprovedQueryTopicsResponse = null

  override def readPreviousQueries(userId: String, fetchSize: Int): ReadPreviousQueriesResponse = null

  override def runQuery(topicId: String, outputTypes: Set[ResultOutputType], queryDefinition: QueryDefinition): AggregatedRunQueryResponse = null

  override def readQueryInstances(queryId: Long): ReadQueryInstancesResponse = null

  override def readInstanceResults(instanceId: Long): AggregatedReadInstanceResultsResponse = null

  override def readPdo(patientSetCollId: String, optionsXml: NodeSeq): ReadPdoResponse = null

  override def readQueryDefinition(queryId: Long): ReadQueryDefinitionResponse = null

  override def deleteQuery(queryId: Long): DeleteQueryResponse = null

  override def renameQuery(queryId: Long, queryName: String): RenameQueryResponse = null

  override def readQueryResult(queryId: Long): AggregatedReadQueryResultResponse = null
}