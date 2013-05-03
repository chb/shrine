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
  
  def withName(newName: String) = this.copy(name = newName)
  
  def withQueryExpr(newQueryExpr: Expression) = this.copy(queryExpr = newQueryExpr)
  
  //NB: Due to the new i2b2 admin previous queries API, we need to be able to transform
  //ourselves into a QueryMaster using either the network or local id .
  def toQueryMaster(idField: ShrineQuery => String = _.networkId.toString): QueryMaster = {
    QueryMaster(idField(this), name, username, domain, dateCreated)
  }
}
