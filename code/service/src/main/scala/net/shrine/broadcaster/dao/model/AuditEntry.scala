package net.shrine.broadcaster.dao.model

import java.sql.Timestamp
import org.squeryl.KeyedEntity //TODO: Don't depend on Squeryl-specific stuff here

/**
 * @author ???
 * @author clint
 * @date Jan 25, 2013
 */
case class AuditEntry(
    id: Long,
    project: String, 
    domain: String,
    username: String,
    time: Timestamp, 
    queryText: Option[String], 
    queryTopic: Option[String])
