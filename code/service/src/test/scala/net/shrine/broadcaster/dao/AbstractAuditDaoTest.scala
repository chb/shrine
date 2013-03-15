package net.shrine.broadcaster.dao

import org.springframework.test.AbstractDependencyInjectionSpringContextTests
import org.scalatest.junit.ShouldMatchersForJUnit
import org.springframework.beans.factory.annotation.Autowired
import scala.slick.session.Database
import net.shrine.broadcaster.dao.slick.tables.Tables
import scala.slick.session.Session

/**
 * @author clint
 * @date Mar 14, 2013
 */
abstract class AbstractAuditDaoTest extends AbstractDependencyInjectionSpringContextTests with ShouldMatchersForJUnit {
  @Autowired
  var auditDao: AuditDao = _

  @Autowired
  var tables: Tables = _

  @Autowired
  var database: Database = _

  override protected def getConfigLocations: Array[String] = Array("classpath:testApplicationContext.xml")
  
  //NB: Ugh, there must be a better way
  protected def afterMakingTables(f: => Any) {
    val t = tables
    import t.driver.Implicit._

    database.withTransaction { implicit session: Session =>
      try {
        tables.AuditEntries.ddl.create

        f
      } finally {
        tables.AuditEntries.ddl.drop
      }
    }
  }
}