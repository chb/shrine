package net.shrine.dao.slick.workarounds

import scala.slick.driver.ExtendedProfile
import scala.slick.driver.MySQLDriver
import scala.slick.driver.H2Driver

/**
 * @author clint
 * @date Oct 25, 2012
 * 
 * Ugh.  I resorted to this after having trouble getting Spring to work well with Scala objects.
 */
final class SpringWorkarounds(dbType: String) {
  def driver: ExtendedProfile = dbType match {
    case "mysql" => MySQLDriver
    case "h2" => H2Driver
    case _ => sys.error("Unknown DB type: '" + dbType + "'")
  }
}