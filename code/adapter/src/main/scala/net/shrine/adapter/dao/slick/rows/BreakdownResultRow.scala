package net.shrine.adapter.dao.slick.rows

/**
 * @author clint
 * @date Oct 16, 2012
 */
final case class BreakdownResultRow(
  id: Int,
  resultId: Int,
  dataKey: String,
  originalValue: Long,
  obfuscatedValue: Long) extends ResultRow(id, resultId)