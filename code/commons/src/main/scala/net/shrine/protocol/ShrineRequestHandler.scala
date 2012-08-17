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
  def readApprovedQueryTopics(request: ReadApprovedQueryTopicsRequest): ShrineResponse

  def readPreviousQueries(request: ReadPreviousQueriesRequest): ShrineResponse

  def readQueryInstances(request: ReadQueryInstancesRequest): ShrineResponse

  def readInstanceResults(request: ReadInstanceResultsRequest): ShrineResponse

  def readPdo(request: ReadPdoRequest): ShrineResponse

  def readQueryDefinition(request: ReadQueryDefinitionRequest): ShrineResponse

  def runQuery(request: RunQueryRequest): ShrineResponse

  def deleteQuery(request: DeleteQueryRequest): ShrineResponse

  def renameQuery(request: RenameQueryRequest): ShrineResponse
  
  def readResult(request: ReadResultRequest): ShrineResponse
}