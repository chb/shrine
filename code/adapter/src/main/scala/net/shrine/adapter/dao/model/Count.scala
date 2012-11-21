package net.shrine.adapter.dao.model

import javax.xml.datatype.XMLGregorianCalendar
import net.shrine.adapter.dao.scalaquery.tables.DateHelpers
import net.shrine.protocol.QueryResult
import net.shrine.protocol.ResultOutputType

/**
 * @author clint
 * @date Oct 16, 2012
 */
final case class Count(
    id: Int,
    resultId: Int, 
    originalValue: Long, 
    obfuscatedValue: Long, 
    creationDate: XMLGregorianCalendar) extends HasResultId {
  
  import ResultOutputType._
  
  private val resultType = Some(PATIENT_COUNT_XML)

  def toQueryResult: QueryResult = {
    QueryResult(resultId, //Is this ok?  This field is supposed to be an i2b2 resultId, but we're passing in an id from the new Shrine adapter DB
                resultId, //Is this ok?  This field is supposed to be an i2b2 instanceId, but we're passing in an id from the new Shrine adapter DB
                resultType,
                obfuscatedValue,
                None, //TODO: Have a real start date here?
                None, //TODO: Have a real end date here?
                None, //no desc
                QueryResult.StatusType.Finished.name,
                // no status message
                None)
  }
}
