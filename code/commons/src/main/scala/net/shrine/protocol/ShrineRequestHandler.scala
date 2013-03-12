package net.shrine.protocol

/**
 * @author Bill Simons
 * @date 3/9/11
 * @link http://cbmi.med.harvard.edu
 * @link http://chip.org
 *       <p/>
 *       NOTICE: This software comes with NO guarantees whatsoever and is
 *       licensed as Lgpl Open Source
 * @link http://www.gnu.org/licenses/lgpl.html
 */
trait ShrineRequestHandler {
  def readApprovedQueryTopics(request: ReadApprovedQueryTopicsRequest, shouldBroadcast: Boolean = true): ShrineResponse

  def readPreviousQueries(request: ReadPreviousQueriesRequest, shouldBroadcast: Boolean = true): ShrineResponse

  def readQueryInstances(request: ReadQueryInstancesRequest, shouldBroadcast: Boolean = true): ShrineResponse

  def readInstanceResults(request: ReadInstanceResultsRequest, shouldBroadcast: Boolean = true): ShrineResponse

  def readPdo(request: ReadPdoRequest, shouldBroadcast: Boolean = true): ShrineResponse

  def readQueryDefinition(request: ReadQueryDefinitionRequest, shouldBroadcast: Boolean = true): ShrineResponse

  def runQuery(request: RunQueryRequest, shouldBroadcast: Boolean = true): ShrineResponse

  def deleteQuery(request: DeleteQueryRequest, shouldBroadcast: Boolean = true): ShrineResponse

  def renameQuery(request: RenameQueryRequest, shouldBroadcast: Boolean = true): ShrineResponse
  
  def readQueryResult(request: ReadQueryResultRequest, shouldBroadcast: Boolean = true): ShrineResponse
}