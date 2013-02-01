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
    username: String, 
    domain: String, 
    queryText: String, 
    queryTopic: String): Unit = addAuditEntry(new Date, project, username, domain, queryText, queryTopic)
    
  def addAuditEntry(
    time: Date,
    project: String, 
    username: String, 
    domain: String, 
    queryText: String, 
    queryTopic: String): Unit

  def findRecentEntries(limit: Int): Seq[AuditEntry]
  
  def inTransaction[T](f: => T): T
}
