package net.shrine.utilities.scanner

import net.shrine.protocol.QueryResult

/**
 * @author clint
 * @date Mar 6, 2013
 */
final case class TermResult(networkQueryId: Long, term: String, status: QueryResult.StatusType, count: Long)