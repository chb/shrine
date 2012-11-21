package net.shrine.adapter.dao.scalaquery.workarounds

import org.scalaquery.ql.extended.H2Driver
import org.scalaquery.ql.extended.ExtendedProfile
import org.scalaquery.ql.extended.MySQLDriver
import org.scalaquery.ql.extended.OracleDriver
import net.shrine.adapter.dao.scalaquery.SequenceHelper
import org.scalaquery.session.Database
import net.shrine.adapter.dao.scalaquery.SequenceHelper

/**
 * @author clint
 * @date Oct 25, 2012
 * 
 * Ugh.  I resorted to this after having trouble getting Spring to work well with Scala objects.
 */
final class SpringWorkarounds(dbType: String) {
  private def unknownDbError() = sys.error("Unknown DB type: '" + dbType + "'")
  
  def driver: ExtendedProfile = dbType match {
    case "mysql" => MySQLDriver
    case "h2" => H2Driver
    case "oracle" => OracleDriver
    case _ => unknownDbError()
  }

  def sequenceHelper(database: Database): SequenceHelper = dbType match {
    case "mysql" => SequenceHelper.mySql(database)
    case "h2" => SequenceHelper.h2(database)
    case "oracle" => SequenceHelper.oracle(database)
    case _ => unknownDbError()
  }
}