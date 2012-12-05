package net.shrine.client

import scala.collection.JavaConverters.asScalaSetConverter
import scala.xml.NodeSeq

import net.shrine.protocol.AggregatedReadInstanceResultsResponse
import net.shrine.protocol.AggregatedReadQueryResultResponse
import net.shrine.protocol.AggregatedRunQueryResponse
import net.shrine.protocol.DeleteQueryResponse
import net.shrine.protocol.ReadApprovedQueryTopicsResponse
import net.shrine.protocol.ReadPdoResponse
import net.shrine.protocol.ReadPreviousQueriesResponse
import net.shrine.protocol.ReadQueryDefinitionResponse
import net.shrine.protocol.ReadQueryInstancesResponse
import net.shrine.protocol.RenameQueryResponse
import net.shrine.protocol.ResultOutputType
import net.shrine.protocol.query.QueryDefinition

/**
 *
 * @author Clint Gilbert
 * @date Sep 14, 2011
 *
 * @link http://cbmi.med.harvard.edu
 *
 */
trait ShrineClient {
  def readApprovedQueryTopics(userId: String): ReadApprovedQueryTopicsResponse

  def readPreviousQueries(userId: String, fetchSize: Int): ReadPreviousQueriesResponse

  def runQuery(topicId: String, outputTypes: Set[ResultOutputType], queryDefinition: QueryDefinition): AggregatedRunQueryResponse
  
  def readQueryInstances(queryId: Long): ReadQueryInstancesResponse
  
  def readInstanceResults(instanceId: Long): AggregatedReadInstanceResultsResponse
  
  def readPdo(patientSetCollId: String, optionsXml: NodeSeq): ReadPdoResponse
  
  def readQueryDefinition(queryId: Long): ReadQueryDefinitionResponse
  
  def deleteQuery(queryId: Long): DeleteQueryResponse
  
  def renameQuery(queryId: Long, queryName: String): RenameQueryResponse
  
  //Overloads for Java interop
  import scala.collection.JavaConverters._

  def runQuery(topicId: String, outputTypes: java.util.Set[ResultOutputType], queryDefinition: QueryDefinition): AggregatedRunQueryResponse = runQuery(topicId, outputTypes.asScala.toSet, queryDefinition)
  
  def readQueryResult(queryId: Long): AggregatedReadQueryResultResponse
}