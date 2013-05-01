package net.shrine.broadcaster.dao.slick

import java.sql.Timestamp
import java.util.Date

import scala.slick.lifted.Query
import scala.slick.session.Database
import scala.slick.session.Session

import net.shrine.broadcaster.dao.AuditDao
import net.shrine.broadcaster.dao.model.AuditEntry
import net.shrine.broadcaster.dao.slick.tables.Tables

/**
 * @author clint
 * @date Jan 25, 2013
 */
final class SlickAuditDao(database: Database, tables: Tables) extends AuditDao {
  import tables._
  import driver.Implicit._

  override def addAuditEntry(time: Date, project: String, domain: String, username: String, queryText: String, queryTopic: String) {
    val timestamp = new Timestamp(time.getTime)

    database.withTransaction { implicit session: Session =>
      AuditEntries.withoutId.insert(project, domain, username, timestamp, queryText, queryTopic)
    }
  }

  override def findRecentEntries(limit: Int): Seq[AuditEntry] = {
    database.withTransaction { implicit session: Session =>
      Queries.recentEntries(limit).list
    }
  }
  
  override def inTransaction[T](f: => T): T = database.withTransaction(f)

  object Queries {
    lazy val allEntries = {
      val allRowsWithTimes = Query(AuditEntries).map(row => (row.time, row.*))
      
      allRowsWithTimes.sortBy { case (time, _) => time.desc }.map { case (_, auditEntryRow) => auditEntryRow }
    }

    def recentEntries(limit: Int) = allEntries.take(limit)
  }
}