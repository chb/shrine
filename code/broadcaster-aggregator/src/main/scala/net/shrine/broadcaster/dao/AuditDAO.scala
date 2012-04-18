package net.shrine.broadcaster.dao

import net.shrine.broadcaster.dao.hibernate.AuditEntry

import java.util.Date

/**
 * @author ??
 * @author Clint Gilbert
 * @date ??
 * Ported to Scala on Feb 28, 2012
 * 
 * DAO that reads and writes audit entries
 */
trait AuditDAO {

    def addAuditEntry(auditEntry: AuditEntry): Unit

    def findRecentEntries(limit: Int): Seq[AuditEntry]
}
