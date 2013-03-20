package net.shrine.utilities.scanner

import net.shrine.protocol.query.Expression
import scala.concurrent.Future
import net.shrine.protocol.query.QueryDefinition
import net.shrine.protocol.query.Term
import net.shrine.protocol.QueryResult

/**
 * @author clint
 * @date Mar 12, 2013
 */
trait ScannerClient { 
  def query(term: String): Future[TermResult]
  
  def retrieveResults(termResult: TermResult): Future[TermResult]
}

object ScannerClient {
  def toQueryDef(term: String) = QueryDefinition("scanner query", Term(term))
  
  def errorTermResult(networkQueryId: Long, term: String): TermResult = TermResult(networkQueryId, term, QueryResult.StatusType.Error, -1L)
}