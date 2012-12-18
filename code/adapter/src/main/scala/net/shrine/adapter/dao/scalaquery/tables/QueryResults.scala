package net.shrine.adapter.dao.scalaquery.tables

import org.scalaquery.ql._
import org.scalaquery.ql.TypeMapper._
import org.scalaquery.ql.extended.{ExtendedTable => Table}
import java.sql.Date
import net.shrine.protocol.ResultOutputType
import net.shrine.adapter.dao.scalaquery.rows.QueryResultRow
import net.shrine.protocol.QueryResult.StatusType
import javax.xml.datatype.XMLGregorianCalendar


/**
 * @author clint
 * @date Oct 12, 2012
 */
object QueryResults extends Table[QueryResultRow]("QUERY_RESULT") with HasId with HasLocalId {
  def localId = localIdColumn[Long]
  def queryId = column[Int]("QUERY_ID", O.NotNull)
  def resultType = column[ResultOutputType]("TYPE", O.NotNull)
  def status = column[StatusType]("STATUS", O.NotNull)
  def elapsed = column[Long]("TIME_ELAPSED", O.Nullable) //Will be null for error results
  def lastUpdated = {
    import DateHelpers.Implicit._
    
    column[XMLGregorianCalendar]("LAST_UPDATED", O.NotNull)
  }
  
  def withoutGeneratedColumns = localId ~ queryId ~ resultType ~ status ~ elapsed.?
  
  import ProjectionHelpers._
  
  override def * = id ~~ withoutGeneratedColumns ~ lastUpdated <> (QueryResultRow, QueryResultRow.unapply _)
  
  import ForeignKeyAction.{ NoAction, Cascade }
  
  def queryIdFk = foreignKey("QueryResultQueryId_FK", queryId, ShrineQueries)(targetColumns = _.id, onUpdate = NoAction, onDelete = Cascade)
  
  private implicit val resultOutputType2StringMapper: TypeMapper[ResultOutputType] = 
    MappedTypeMapper.base[ResultOutputType, String](
      _.toString, 
      ResultOutputType.valueOf) //TODO: What about failures? Say if valueOf() returns null?
      
  private implicit val statusType2StringMapper: TypeMapper[StatusType] = 
    MappedTypeMapper.base[StatusType, String](
      _.toString, 
      StatusType.valueOf(_).get) //TODO: What about failures? Say if serialized is null, or valueOf returns None?
}