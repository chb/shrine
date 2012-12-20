package net.shrine.adapter.dao.scalaquery.rows

import javax.xml.datatype.XMLGregorianCalendar
import net.shrine.adapter.dao.model.HasResultId
import net.shrine.protocol.ResultOutputType

/**
 * @author clint
 * @date Oct 16, 2012
 */
final case class CountRow(
    id: Int,
    resultId: Int,
    originalValue: Long, 
    obfuscatedValue: Long, 
    creationDate: XMLGregorianCalendar) extends HasResultId