package net.shrine.adapter

import scala.io.Source
import javax.annotation.Resource
import net.shrine.adapter.dao.AdapterDao
import org.springframework.test.AbstractDependencyInjectionSpringContextTests
import scala.slick.session.Database
import scala.slick.driver.ExtendedProfile
import scala.slick.driver.BasicDriver.Table
import scala.slick.lifted.Query
import scala.slick.session.Session
import net.shrine.adapter.dao.slick.tables.Tables
import scala.slick.driver.BasicProfile
import scala.slick.driver.BasicDriver

/**
 * @author clint
 * @date Nov 7, 2012
 */
trait AdapterDbTest { self: AbstractDependencyInjectionSpringContextTests =>
  @Resource
  var database: Database = _
  
  @Resource
  var driver: ExtendedProfile = _

  @Resource(name = "adapterDao")
  var dao: AdapterDao = _
  
  @Resource
  var tables: Tables = _
  
  override protected final def getConfigPath = "/testApplicationContext.xml"
  
  protected lazy val queryRows = for(row <- Query(tables.ShrineQueries)) yield row.*

  protected lazy val queryResultRows = for(row <- Query(tables.QueryResults)) yield row.*

  protected lazy val countResultRows = for(row <- Query(tables.CountResults)) yield row.*

  protected lazy val breakdownResultRows = for(row <- Query(tables.BreakdownResults)) yield row.*

  protected lazy val errorResultRows = for(row <- Query(tables.ErrorResults)) yield row.*

  protected def list[A, B](q: Query[A, B]): Seq[B] = {
    val d = driver
    import d.Implicit._

    database.withSession { implicit session: Session => q.list }
  }
  
  protected def first[A, B](q: Query[A, B]): B = {
    val d = driver
    import d.Implicit._

    database.withSession { implicit session: Session => q.first }
  }
  
  protected def afterCreatingTables(body: => Any): Unit = afterCreatingTablesReturn(body)
  
  //TODO: There /must/ be a better way
  protected def afterCreatingTablesReturn[T](body: => T): T = {
    def sqlLines(filename: String): Iterator[String] = {
      Source.fromInputStream(this.getClass.getClassLoader.getResourceAsStream(filename)).getLines.map(_.trim).filter(!_.isEmpty).filter(!_.startsWith("--"))
    }

    def ignore[T <: Exception : Manifest](g: => Any) {
      val classOfT = manifest[T].erasure

      try { g } catch { case e: T if classOfT.isAssignableFrom(e.getClass) => }
    }

    val ddlStatements = sqlLines("adapter-h2.sql").toSeq

    val dropStatements = sqlLines("adapter-h2-drop.sql").toSeq

    val d = driver
    import d.Implicit._

    database.withTransaction {
      database.withSession { implicit session: Session =>
        val conn = session.conn

        dropStatements.foreach { sql =>
          ignore[Exception](conn.createStatement.execute(sql))
        }
      }
    }

    database.withTransaction {
      database.withSession { implicit session: Session =>
        val conn = session.conn

        ddlStatements.foreach { sql =>
          conn.createStatement.execute(sql)
        }
      }
    }

    database.withTransaction {
      body
    }
  }
}