package net.shrine.client

import net.shrine.protocol.ReadPreviousQueriesResponse
import net.shrine.protocol.ReadApprovedQueryTopicsResponse
import net.shrine.protocol.RunQueryResponse
import net.shrine.protocol.ResultOutputType
import net.shrine.protocol.ReadQueryInstancesResponse
import net.shrine.protocol.ReadInstanceResultsResponse
import net.shrine.protocol.ReadPdoResponse
import scala.xml.NodeSeq
import net.shrine.protocol.ReadQueryDefinitionResponse
import net.shrine.protocol.DeleteQueryResponse
import net.shrine.protocol.RenameQueryResponse
import net.shrine.protocol.query.QueryDefinition
import scala.collection.JavaConversions._


/**
 *
 * @author Clint Gilbert
 * @date Sep 14, 2011
 *
 * @link http://cbmi.med.harvard.edu
 *
 * This software is licensed under the LGPL
 * @link http://www.gnu.org/licenses/lgpl.html
 *
 */
trait ShrineClient {
  def readApprovedQueryTopics(userId: String): ReadApprovedQueryTopicsResponse

  def readPreviousQueries(userId: String, fetchSize: Int): ReadPreviousQueriesResponse

  def runQuery(topicId: String, outputTypes: Set[ResultOutputType], queryDefinition: QueryDefinition): RunQueryResponse
  
  def readQueryInstances(queryId: Long): ReadQueryInstancesResponse
  
  def readInstanceResults(instanceId: Long): ReadInstanceResultsResponse
  
  def readPdo(patientSetCollId: String, optionsXml: NodeSeq): ReadPdoResponse
  
  def readQueryDefinition(queryId: Long): ReadQueryDefinitionResponse
  
  def deleteQuery(queryId: Long): DeleteQueryResponse
  
  def renameQuery(queryId: Long, queryName: String): RenameQueryResponse
  
  //Overloads for Java interop


 def runQuery(topicId: String, outputTypes: java.util.Set[ResultOutputType], queryDefinition: QueryDefinition): RunQueryResponse = runQuery(topicId, outputTypes.toSet, queryDefinition)
}