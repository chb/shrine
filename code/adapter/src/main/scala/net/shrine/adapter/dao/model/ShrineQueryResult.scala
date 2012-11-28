package net.shrine.adapter.dao.model

import scala.Option.option2Iterable
import net.shrine.adapter.dao.scalaquery.rows.BreakdownResultRow
import net.shrine.adapter.dao.scalaquery.rows.QueryResultRow
import net.shrine.protocol.ResultOutputType
import net.shrine.protocol.QueryResult
import net.shrine.protocol.I2b2ResultEnvelope

/**
 * @author clint
 * @date Oct 16, 2012
 *
 * NB: Named ShrineQueryResult to avoid clashes with net.shrine.protocol.QueryResult
 */
final case class ShrineQueryResult(
  count: Option[Count],
  breakdowns: Seq[Breakdown],
  errors: Seq[ShrineError]) {
  
  def toQueryResults(doObfuscation: Boolean): Option[QueryResult] = {
    val countResult = count.map(_.toQueryResult).map { countQueryResult =>
      //add breakdowns
      
      val byType = Map.empty ++ breakdowns.map(b => (b.resultType, b.data))
      
      val getRealOrObfuscated: ObfuscatedPair => Long = { 
        if(doObfuscation) _.obfuscated
        else _.original
      }
      
      val typesToData = byType.mapValues(_.mapValues(getRealOrObfuscated))
      
      countQueryResult.withBreakdowns(typesToData.map { 
        case (resultType, data) => 
          (resultType, I2b2ResultEnvelope(resultType, data)) 
      })
    }
    
    def firstError = errors.headOption.map(_.toQueryResult)
    
    countResult orElse firstError
  }
}

object ShrineQueryResult {
  def fromRows(resultRows: Seq[QueryResultRow], countRows: Seq[Count], breakdownRows: Map[ResultOutputType, Seq[BreakdownResultRow]], errorRows: Seq[ShrineError]): Option[ShrineQueryResult] = {
    if(resultRows.isEmpty) {
      None
    } else {
      val count = countRows.headOption
    
      val breakdowns = breakdownRows.flatMap { case (resultType, rows) => Breakdown.fromRows(resultType, rows) }.toSeq
    
      Some(ShrineQueryResult(count, breakdowns, errorRows))
    }
  }
}