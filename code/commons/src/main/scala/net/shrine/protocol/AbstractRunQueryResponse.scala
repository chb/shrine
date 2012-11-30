package net.shrine.protocol

import javax.xml.datatype.XMLGregorianCalendar
import net.shrine.protocol.query.QueryDefinition


/**
 * @author clint
 * @date Nov 29, 2012
 */
abstract class AbstractRunQueryResponse(
    val queryId: Long,
    val createDate: XMLGregorianCalendar,
    val userId: String,
    val groupId: String,
    val requestXml: QueryDefinition,
    val queryInstanceId: Long,
    val results: Seq[QueryResult]) extends ShrineResponse