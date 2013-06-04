package net.shrine.broadcaster.dao

import org.springframework.test.AbstractDependencyInjectionSpringContextTests
import org.scalatest.junit.ShouldMatchersForJUnit
import org.springframework.beans.factory.annotation.Autowired
import net.shrine.broadcaster.dao.squeryl.tables.Tables
import net.shrine.broadcaster.dao.squeryl.SquerylEntryPoint

/**
 * @author clint
 * @date Mar 14, 2013
 */
abstract class AbstractAuditDaoTest extends AbstractDependencyInjectionSpringContextTests with ShouldMatchersForJUnit {
  @Autowired
  var auditDao: AuditDao = _

  @Autowired
  var tables: Tables = _

  override protected def getConfigLocations: Array[String] = Array("classpath:testApplicationContext.xml")

  protected def afterMakingTables(f: => Any) {
    import SquerylEntryPoint._

    inTransaction {
      try {
        tables.auditEntries.schema.create

        f
      } finally {
        tables.auditEntries.schema.drop
      }
    }
  }
}