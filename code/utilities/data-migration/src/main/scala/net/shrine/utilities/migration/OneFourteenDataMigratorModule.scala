package net.shrine.utilities.migration

import java.util.Properties

import scala.slick.driver.ExtendedProfile
import scala.slick.driver.MySQLDriver
import scala.slick.session.Database
import scala.slick.session.Session

import net.shrine.adapter.dao.slick.tables.ShrineQueriesComponent
import net.shrine.dao.slick.tables.HasDriver
import net.shrine.utilities.migration.components.I2b2QueryDefShrineQueriesComponent

/**
 * @author clint
 * @date May 3, 2013
 * 
 * Super-simple tool to take a partially-migrated SHRINE_QUERY table, with i2b2-formatted XML
 * query definitions in the QUERY_EXPRESSION column, and update the table, turning the values
 * into the QUERY_EXPRESSION column into Shrine-XML-formatted Expressions.  
 */
object OneFourteenDataMigratorModule {
  def main(args: Array[String]) {
    val database = Database.forURL("""jdbc-url""", "db-user", """db-password""", new Properties)

    val module = new OneFourteenDataMigratorModule(MySQLDriver, database) //or SqlServerDriver, etc

    module.migrate()
  }
}

final class OneFourteenDataMigratorModule(override val driver: ExtendedProfile, database: Database) extends 
	HasDriver with 
	ShrineQueriesComponent with 
	I2b2QueryDefShrineQueriesComponent {

  import driver.simple._

  //
  //Get all the ShrineQueries via the i2b2-formatted view of the world (ShrineQueriesWithI2b2QueryDefs),
  //which uses a custom TypeMapper to interpret the QUERY_EXPRESSION column as an XML-serialized, i2b2-formatted
  //QueryDefinition.
  //
  //Then save these back via the Shrine-formatted view of the world (ShrineQueries), which will serialize the
  //queryExpr fields as Shrine-formatted XML.
  def migrate() {
    database.withTransaction {
      database.withSession { implicit session: Session =>
        Query(ShrineQueriesWithI2b2QueryDefs).foreach { shrineQuery =>
          Query(ShrineQueries).filter(_.id === shrineQuery.id).update(shrineQuery)
        }
      }
    }
  }
}
