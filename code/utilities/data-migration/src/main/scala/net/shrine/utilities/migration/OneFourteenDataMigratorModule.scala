package net.shrine.utilities.migration

import org.squeryl.Schema
import org.squeryl.adapters.{ MSSQLServer, OracleAdapter, MySQLAdapter }

import net.shrine.adapter.dao.squeryl.SquerylEntryPoint
import net.shrine.adapter.dao.squeryl.tables.BreakdownResultsComponent
import net.shrine.adapter.dao.squeryl.tables.CountResultsComponent
import net.shrine.adapter.dao.squeryl.tables.ErrorResultsComponent
import net.shrine.adapter.dao.squeryl.tables.QueryResultsComponent
import net.shrine.adapter.dao.squeryl.tables.ShrineQueriesComponent
import net.shrine.dao.squeryl.JdbcUrlSquerylInitializer
import net.shrine.dao.squeryl.SquerylInitializer
import net.shrine.protocol.query.QueryDefinition
import com.typesafe.config.{ Config, ConfigFactory }
import org.squeryl.internals.DatabaseAdapter

/**
 * @author clint
 * @date May 3, 2013
 */
object OneFourteenDataMigratorModule extends App {

  def loadConfig: Config = {
    //  The convenience method ConfigFactory.load() loads the following (first-listed are higher priority):
    //
    //  system properties
    //  application.conf (all resources on classpath with this name)
    //  application.json (all resources on classpath with this name)
    //  application.properties (all resources on classpath with this name)
    //  reference.conf (all resources on classpath with this name)
    ConfigFactory.load()
  }

  def identifyAdapter(adapterName: String): DatabaseAdapter = adapterName match {
    case "mysql" => new MySQLAdapter
    case "oracle" => new OracleAdapter
    case "sqlserver" => new MSSQLServer
    case _ => throw new IllegalArgumentException("db-adapter must be one of msyql, oracle, sqlserver")
  }

  val config = loadConfig

  val initializer: SquerylInitializer = new JdbcUrlSquerylInitializer(
    identifyAdapter(config.getString("db-adapter")),
    config.getString("driver-name"),
    config.getString("jdbc-url"),
    config.getString("jdbc-user"),
    config.getString("jdbc-password"))

  val module = new OneFourteenDataMigratorModule(initializer)

  module.migrate()
}

import SquerylEntryPoint._

/**
 * @author clint
 * @date May 3, 2013
 */
final class OneFourteenDataMigratorModule(initializer: SquerylInitializer) extends 
	Schema with 
	CountResultsComponent with 
	BreakdownResultsComponent with 
	ErrorResultsComponent with 
	QueryResultsComponent with 
	ShrineQueriesComponent {

  //
  //Get all the ShrineQueries from the (presumed-to-be-i2b2-formatted) SHRINE_QUERY table,
  //then for each row, convert the QUERY_EXPRESSION column to Shrine format and update the 
  //row in the SHRINE_QUERY table.
  def migrate() {
    initializer.init

    inTransaction {
      val i2b2FormatQueries = from(shrineQueries)(select(_))

      i2b2FormatQueries.iterator.foreach { i2b2FormatQuery =>
        val shrineFormatExpr = QueryDefinition.fromI2b2(i2b2FormatQuery.queryExpr).get.expr.toXmlString

        update(shrineQueries) { shrineQueryForUpdate =>
          where(i2b2FormatQuery.id === shrineQueryForUpdate.id).
          set(shrineQueryForUpdate.queryExpr := shrineFormatExpr)
        }
      }
    }
  }
}
