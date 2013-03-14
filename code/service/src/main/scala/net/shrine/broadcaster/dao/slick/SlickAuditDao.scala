package net.shrine.broadcaster.dao.slick

import net.shrine.broadcaster.dao.AuditDao
import scala.slick.session.Database
import net.shrine.broadcaster.dao.slick.tables.Tables
import net.shrine.broadcaster.dao.model.AuditEntry
import java.util.Date
import scala.slick.session.Session
import scala.slick.lifted.Parameters

/**
 * @author clint
 * @date Jan 25, 2013
 */
final class SlickAuditDao(database: Database, tables: Tables) extends AuditDao {
  import tables._
  import driver.Implicit._

  override def addAuditEntry(time: Date, project: String, username: String, domain: String, queryText: String, queryTopic: String) {
    val timestamp = new java.sql.Timestamp(time.getTime)

    database.withTransaction { implicit session: Session =>
      AuditEntries.withoutId.insert(project, username, domain, timestamp, queryText, queryTopic)
    }
  }

  override def findRecentEntries(limit: Int): Seq[AuditEntry] = {
    database.withTransaction { implicit session: Session =>
      Queries.recentEntries(limit).list
    }
  }
  
  override def inTransaction[T](f: => T): T = {
    database.withTransaction(f)
  }

  object Queries {
    //session.createCriteria(classOf[AuditEntry]).addOrder(Order.desc("time")).setMaxResults(limit).list().asInstanceOf[JList[AuditEntry]].asScala

    lazy val allEntries = (for {
      row <- AuditEntries
    } yield (row.time, row.*)).sortBy { case (time, _) => time.desc }.map { case (_, entry) => entry }

    def recentEntries(limit: Int) = allEntries.take(limit)
  }
}