package net.shrine.adapter

import scala.io.Source
import javax.annotation.Resource
import org.scalaquery.session.Database
import org.scalaquery.ql.extended.ExtendedProfile
import net.shrine.adapter.dao.AdapterDao
import org.scalaquery.session.Session
import org.springframework.test.AbstractDependencyInjectionSpringContextTests

/**
 * @author clint
 * @date Nov 7, 2012
 */
trait AdapterDbTest { self: AbstractDependencyInjectionSpringContextTests =>
  @Resource
  var database: Database = _
  
  @Resource
  var driver: ExtendedProfile = _

  @Resource
  var dao: AdapterDao = _
  
  override protected final def getConfigPath = "/testApplicationContext.xml"
  
  //TODO: There /must/ be a better way
  protected def afterCreatingTables(body: => Any) {
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