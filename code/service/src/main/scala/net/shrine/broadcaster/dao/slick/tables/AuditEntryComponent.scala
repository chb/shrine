package net.shrine.broadcaster.dao.slick.tables

import net.shrine.broadcaster.dao.model.AuditEntry
import java.sql.Timestamp
import scala.slick.lifted.TypeMapper._
import net.shrine.dao.slick.tables.HasDriver

/**
 * @author clint
 * @date Jan 25, 2013
 */
trait AuditEntryComponent { self: HasDriver =>
  
  import self.driver.simple._

  object AuditEntries extends Table[AuditEntry]("AUDIT_ENTRY") {
    def id = column[Long]("AUDIT_ENTRY_ID", O.PrimaryKey, O.NotNull, O.AutoInc)
    def project = column[String]("PROJECT", O.NotNull)
    def username = column[String]("USERNAME", O.NotNull)
    def domain = column[String]("DOMAIN_NAME", O.NotNull)
    def time = column[Timestamp]("TIME", O.NotNull)// TODO: DEFAULT CURRENT_TIMESTAMP,
    def queryText = column[String]("QUERY_TEXT", O.Nullable)//TODO:  TEXT,
    def queryTopic = column[String]("QUERY_TOPIC", O.Nullable)
    
    def withoutId = project ~ username ~ domain ~ time ~ queryText ~ queryTopic
    
    override def * = id ~: withoutId <> (AuditEntry, AuditEntry.unapply _)
    
    def domainUsernameAndQueryTopicIndex = index("IDX_AUDIT_ENTRY_DOMAIN_USERNAME_QUERY_TOPIC", domain ~ username ~ queryTopic, unique = false)
  }
}