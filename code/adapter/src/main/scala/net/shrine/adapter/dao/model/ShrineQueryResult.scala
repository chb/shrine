package net.shrine.adapter.dao.model

import net.shrine.adapter.dao.slick.rows.BreakdownResultRow
import net.shrine.adapter.dao.slick.rows.QueryResultRow
import net.shrine.protocol.ResultOutputType
import net.shrine.protocol.QueryResult
import net.shrine.protocol.I2b2ResultEnvelope
import net.shrine.adapter.dao.slick.rows.CountRow

/**
 * @author clint
 * @date Oct 16, 2012
 *
 * NB: Named ShrineQueryResult to avoid clashes with net.shrine.protocol.QueryResult
 */
final case class ShrineQueryResult(
  networkQueryId: Long,
  localId: String,
  count: Option[Count],
  breakdowns: Seq[Breakdown],
  errors: Seq[ShrineError]) {
  
  //TODO: include breakdowns as well?  What if they're PROCESSING while the count is FINISHED?  Can this even happen?
  val isDone = count.map(_.statusType.isDone).getOrElse(false)
  
  def toQueryResults(doObfuscation: Boolean): Option[QueryResult] = {
    val countResult = count.map(_.toQueryResult).map { countQueryResult =>
      //add breakdowns
      
      val byType = Map.empty ++ breakdowns.map(b => (b.resultType, b.data))
      
      val getRealOrObfuscated: ObfuscatedPair => Long = { 
        if(doObfuscation) { _.obfuscated }
        else { _.original }
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
  def fromRows(queryRow: ShrineQuery, resultRows: Seq[QueryResultRow], countRowOption: Option[CountRow], breakdownRows: Map[ResultOutputType, Seq[BreakdownResultRow]], errorRows: Seq[ShrineError]): Option[ShrineQueryResult] = {
    if(resultRows.isEmpty) {
      None
    } else {
      val resultRowsByType = resultRows.map(r => r.resultType -> r).toMap
      
      val count = for {
        countRow <- countRowOption
        resultRow <- resultRowsByType.get(ResultOutputType.PATIENT_COUNT_XML)
      } yield Count.fromRows(resultRow, countRow)
      
      val breakdowns = (for {
        (resultType, resultRow) <- resultRowsByType
        rows <- breakdownRows.get(resultType)
        breakdown <- Breakdown.fromRows(resultType, resultRow.localId, rows)
      } yield breakdown).toSeq
      
      Some(ShrineQueryResult(queryRow.networkId, queryRow.localId, count, breakdowns, errorRows))
    }
  }
}