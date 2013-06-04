package net.shrine.adapter.dao.model.squeryl

import java.sql.Timestamp
import net.shrine.protocol.query.Term
import net.shrine.util.Util
import net.shrine.adapter.dao.model.ShrineQuery
import net.shrine.protocol.query.Expression
import net.shrine.dao.DateHelpers
import javax.xml.datatype.XMLGregorianCalendar
import org.squeryl.KeyedEntity
import org.squeryl.annotations.Column

/**
 * @author clint
 * @date May 28, 2013
 */
case class SquerylShrineQuery(
    @Column(name = "ID")
    id: Int,
    @Column(name = "LOCAL_ID")
    localId: String,
    @Column(name = "NETWORK_ID")
    networkId: Long,
    @Column(name = "QUERY_NAME")
    name: String,
    @Column(name = "USERNAME")
    username: String,
    @Column(name = "DOMAIN")
    domain: String,
    @Column(name = "QUERY_EXPRESSION")
    queryExpr: String,
    @Column(name = "DATE_CREATED")
    dateCreated: Timestamp) extends KeyedEntity[Int] {
  
  def this(
      id: Int,
      localId: String,
      networkId: Long,
      name: String,
      username: String,
      domain: String,
      queryExpr: Expression,
      dateCreated: XMLGregorianCalendar) = this(id, localId, networkId, name, username, domain, queryExpr.toXmlString, DateHelpers.toTimestamp(dateCreated))

  //NB: For Squeryl, ugh :(
  def this() = this(0, "", 0L, "", "", "", Term("foo"), Util.now)
      
  def toShrineQuery = ShrineQuery(id, localId, networkId, name, username, domain, Expression.fromXml(queryExpr).get, DateHelpers.toXmlGc(dateCreated))
}
