package net.shrine.adapter.dao.model

import javax.xml.datatype.XMLGregorianCalendar
import net.shrine.adapter.dao.scalaquery.tables.DateHelpers
import net.shrine.protocol.QueryResult
import net.shrine.protocol.ResultOutputType
import net.shrine.adapter.dao.scalaquery.rows.CountRow
import net.shrine.adapter.dao.scalaquery.rows.QueryResultRow
import net.shrine.protocol.QueryResult.StatusType
import org.spin.tools.NetworkTime
import net.shrine.util.XmlGcEnrichments

/**
 * @author clint
 * @date Oct 16, 2012
 */
final case class Count(
    id: Int,
    resultId: Int,
    localId: Long,
    statusType: StatusType,
    originalValue: Long, 
    obfuscatedValue: Long, 
    creationDate: XMLGregorianCalendar,
    startDate: XMLGregorianCalendar,
    endDate: XMLGregorianCalendar) extends HasResultId {
  
  import ResultOutputType._
  
  private val resultType = Some(PATIENT_COUNT_XML)

  def toQueryResult: QueryResult = {
    QueryResult(localId,
                resultId, //Is this ok?  This field is supposed to be an i2b2 instanceId, but we're passing in an id from the new Shrine adapter DB
                resultType,
                obfuscatedValue,
                Some(startDate),
                Some(endDate),
                //no desc
                None, 
                statusType,
                // no status message
                None)
  }
}

object Count {
  def fromRows(resultRow: QueryResultRow, countRow: CountRow): Count = {
    import XmlGcEnrichments._
    
    val elapsed = resultRow.elapsed.getOrElse(0L)
    
    Count(
      countRow.id, 
      countRow.resultId, 
      resultRow.localId, 
      resultRow.status, 
      countRow.originalValue, 
      countRow.obfuscatedValue, 
      countRow.creationDate,
      countRow.creationDate, //TODO: revisit startDate
      countRow.creationDate + elapsed.milliseconds) //TODO: revisit endDate
  }
}

