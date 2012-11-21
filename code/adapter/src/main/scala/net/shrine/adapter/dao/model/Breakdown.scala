package net.shrine.adapter.dao.model

import net.shrine.adapter.dao.scalaquery.rows.BreakdownResultRow
import net.shrine.protocol.ResultOutputType

/**
 * @author clint
 * @date Oct 16, 2012
 */
final case class Breakdown(resultId: Int, resultType: ResultOutputType, data: Map[String, ObfuscatedPair]) extends HasResultId

object Breakdown {
  def fromRows(resultType: ResultOutputType, rows: Seq[BreakdownResultRow]): Option[Breakdown] = {
    require(resultType.isBreakdown)
    
    if(rows.isEmpty) {
      None
    } else {
      val entries = rows.map(r => (r.dataKey, ObfuscatedPair(r.originalValue, r.obfuscatedValue)))
      
      Some(Breakdown(rows.head.resultId, resultType, entries.toMap))
    }
  }
}