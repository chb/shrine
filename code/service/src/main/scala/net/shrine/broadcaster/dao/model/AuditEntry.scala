package net.shrine.broadcaster.dao.model

import java.sql.Timestamp

/**
 * @author ???
 * @author clint
 * @date Jan 25, 2013
 */
final case class AuditEntry(
    auditEntryId: Long, 
    project: String, 
    username: String, 
    domain: String, 
    time: Timestamp, 
    queryText: String, 
    queryTopic: String)
