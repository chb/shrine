package net.shrine.broadcaster.dao

import net.shrine.broadcaster.dao.model.AuditEntry

import java.util.Date

/**
 * @author ??
 * @author Clint Gilbert
 * @date ??
 * Ported to Scala on Feb 28, 2012
 *
 * DAO that reads and writes audit entries
 */
trait AuditDao {

  def addAuditEntry(
    project: String, 
    domain: String,
    username: String,
    queryText: String, 
    queryTopic: String): Unit = addAuditEntry(new Date, project, domain, username, queryText, queryTopic)
    
  def addAuditEntry(
    time: Date,
    project: String, 
    domain: String,
    username: String,
    queryText: String, 
    queryTopic: String): Unit

  def findRecentEntries(limit: Int): Seq[AuditEntry]
  
  def inTransaction[T](f: => T): T
}
