package net.shrine.aggregation

import scala.Option.option2Iterable

import org.spin.message.Result

import net.shrine.aggregation.BasicAggregator.Valid
import net.shrine.protocol.QueryResult
import net.shrine.protocol.ResultOutputType
import net.shrine.protocol.ResultOutputType.PATIENTSET
import net.shrine.protocol.ShrineResponse

/**
 * @author clint
 * @date Nov 9, 2012
 */
abstract class StoredResultsAggregator[R <: ShrineResponse : Manifest : HasResults](
    shrineNetworkQueryId: Long,
    showAggregation: Boolean,
    errorMessage: Option[String] = None, 
    invalidMessage: Option[String] = None) extends PackagesErrorsAggregator[R](errorMessage, invalidMessage) {
  
  /**
   * Default implementation only replaces the description with the spinResult description; Subclasses can override
   * and do something more interesting.
   */
  //protected def transformResult(queryResult: QueryResult, spinResult: Result): QueryResult = queryResult.withDescription(spinResult.getDescription)

  protected def consolidateQueryResults(queryResultsFromAllValidResponses: Seq[(SpinResultEntry, Seq[QueryResult])]): Seq[QueryResult]
  
  protected def makeAggregatedResult(queryResults: Seq[QueryResult]): Option[QueryResult]
  
  import ResultOutputType._
  
  private val setType = Some(PATIENTSET)
  private val finishedStatusType = QueryResult.StatusType.Finished.name
  private val allowedSetTypes = ResultOutputType.values.filterNot(_.isError).toSet

  private val resultsFrom = implicitly[HasResults[R]].resultsFrom _
  private val makeResponse = implicitly[HasResults[R]].makeResponse _
  
  private[aggregation] final override def makeResponse(validResponses: Seq[Valid[R]], errorResponses: Seq[QueryResult], invalidResponses: Seq[QueryResult]): ShrineResponse = {

    def isAllowedSetType(result: QueryResult) = result.resultType.map(allowedSetTypes).getOrElse(false)
    
    val allQueryResults = for{
      Valid(spinResult, response) <- validResponses 
    } yield (spinResult, resultsFrom(response))
    
    val queryResults = consolidateQueryResults(allQueryResults)

    //Append the aggregated response, if any
    val finalQueryResults =
      if(showAggregation) queryResults ++ makeAggregatedResult(queryResults)
      else queryResults

    makeResponse(shrineNetworkQueryId, finalQueryResults ++ errorResponses ++ invalidResponses)
  }
}