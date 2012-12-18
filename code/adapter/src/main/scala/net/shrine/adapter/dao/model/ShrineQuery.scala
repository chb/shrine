package net.shrine.adapter.dao.model

import javax.xml.datatype.XMLGregorianCalendar
import net.shrine.protocol.query.QueryDefinition
import net.shrine.protocol.QueryMaster
import net.shrine.protocol.query.Expression

/**
 * @author clint
 * @date Oct 16, 2012
 */
final case class ShrineQuery(
  id: Int,
  localId: String,
  networkId: Long,
  name: String,
  username: String,
  domain: String,
  queryExpr: Expression,
  dateCreated: XMLGregorianCalendar) {
  
  def toQueryMaster: QueryMaster = {
    QueryMaster(networkId.toString, name, username, domain, dateCreated)
  }
}
