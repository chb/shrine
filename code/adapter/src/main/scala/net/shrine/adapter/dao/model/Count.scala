package net.shrine.adapter.dao.model

import javax.xml.datatype.XMLGregorianCalendar
import net.shrine.adapter.dao.scalaquery.tables.DateHelpers
import net.shrine.protocol.QueryResult
import net.shrine.protocol.ResultOutputType
import net.shrine.adapter.dao.scalaquery.rows.CountRow

/**
 * @author clint
 * @date Oct 16, 2012
 */
final case class Count(
    id: Int,
    resultId: Int,
    localId: Long,
    originalValue: Long, 
    obfuscatedValue: Long, 
    creationDate: XMLGregorianCalendar) extends HasResultId {
  
  import ResultOutputType._
  
  private val resultType = Some(PATIENT_COUNT_XML)

  def toQueryResult: QueryResult = {
    QueryResult(localId, //Is this ok?  This field is supposed to be an i2b2 resultId, but we're passing in an id from the new Shrine adapter DB
                resultId, //Is this ok?  This field is supposed to be an i2b2 instanceId, but we're passing in an id from the new Shrine adapter DB
                resultType,
                obfuscatedValue,
                None, //TODO: Have a real start date here?
                None, //TODO: Have a real end date here?
                None, //no desc
                QueryResult.StatusType.Finished,
                // no status message
                None)
  }
}

object Count {
  //TODO: TEST!!!
  def fromCountRow(localResultId: Long, row: CountRow): Count = {
    Count(row.id, row.resultId, localResultId, row.originalValue, row.obfuscatedValue, row.creationDate)
  }
}

