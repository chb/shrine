package net.shrine.adapter.dao.scalaquery.rows

import net.shrine.protocol.ResultOutputType
import net.shrine.protocol.QueryResult.StatusType
import javax.xml.datatype.XMLGregorianCalendar

/**
 * @author clint
 * @date Oct 16, 2012
 */
final case class QueryResultRow(
    id: Int,
    queryId: Int,
    resultType: ResultOutputType,
    status: StatusType,
    elapsed: Option[Long], //Will be None for error results
    lastUpdated: XMLGregorianCalendar)