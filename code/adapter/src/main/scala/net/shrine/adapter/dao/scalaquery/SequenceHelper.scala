package net.shrine.adapter.dao.scalaquery

import org.scalaquery.ql.Query
import org.scalaquery.ql.SimpleFunction
import org.scalaquery.ql.extended.ExtendedProfile
import org.scalaquery.ql.extended.MySQLDriver
import org.scalaquery.ql.extended.OracleDriver
import org.scalaquery.ql.extended.H2Driver
import org.scalaquery.session.Database
import org.scalaquery.session.Session
import net.shrine.util.Util.???

/**
 * @author clint
 * @date Oct 19, 2012
 * 
 * Helper objects to get the ID of the last inserted thing, in DB-specific ways.  Will no longer be needed if we 
 * switch to Slick 0.11.1+, which allows one to do things like
 * 
 * def save(item: Item): Item /* return a copy with the new id */ = database.withSession {
 *   Item.copy(id = (name ~ description) returning id insert (item.name, item.description)
 * }
 */
@Deprecated
trait SequenceHelper {
  def lastInsertedQueryId(implicit session: Session): Int
  
  def lastInsertedQueryResultId(implicit session: Session): Int
}

@Deprecated
object SequenceHelper {
  @Deprecated
  def oracle(database: Database): SequenceHelper = new SelectsMagicValuesSequenceHelper(database, OracleDriver, "seq_shrine_query_id.CURRVAL", "seq_query_result_id.CURRVAL")

  @Deprecated
  def mySql(database: Database): SequenceHelper = new CallsBuiltInFunctionSequenceHelper(database, MySQLDriver, "LAST_INSERT_ID")
  
  @Deprecated
  def h2(database: Database): SequenceHelper = new CallsBuiltInFunctionSequenceHelper(database, H2Driver, "scope_identity")
  
  final class SelectsMagicValuesSequenceHelper private[SequenceHelper] (database: Database, driver: ExtendedProfile, magicQueryIdSequenceColumnName: String, magicQueryResultIdSequenceColumnName: String) extends SequenceHelper {
    
    import driver.Implicit._
    
    //TODO
    override def lastInsertedQueryId(implicit session: Session): Int = ???
    
    //TODO
    override def lastInsertedQueryResultId(implicit session: Session): Int = ???
  }
  
  final class CallsBuiltInFunctionSequenceHelper private[SequenceHelper] (database: Database, driver: ExtendedProfile, sequenceFuncSql: String) extends SequenceHelper {
    private val lastInsertedQueryIdFunc = SimpleFunction.nullary[Int](sequenceFuncSql)
    
    private def lastInsertedId(implicit session: Session): Int = {
      import driver.Implicit._
      
      Query(lastInsertedQueryIdFunc).first
    }
    
    override def lastInsertedQueryId(implicit session: Session): Int = lastInsertedId
    
    override def lastInsertedQueryResultId(implicit session: Session): Int = lastInsertedId
  } 
}