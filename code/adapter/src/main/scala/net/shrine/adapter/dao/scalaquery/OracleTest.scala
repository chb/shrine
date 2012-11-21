/*package net.shrine.adapter.dao.scalaquery

import org.scalaquery.ql.extended.OracleDriver
import org.scalaquery.session.Database
import net.shrine.protocol.AuthenticationInfo
import net.shrine.protocol.Credential
import net.shrine.protocol.query.QueryDefinition
import net.shrine.protocol.query.Term
import net.shrine.adapter.dao.model.ShrineQuery

import oracle.jdbc.{OracleDriver => OracleJdbcDriver} 

//TODO: DELETE

object OracleTest extends App {
  val db = Database.forURL("jdbc:oracle:thin:@localhost:1521:xe", "ADAPTER", "bendah", driver = classOf[OracleJdbcDriver].getName)
  
  val dao = new ScalaQueryAdapterDao(db, OracleDriver)
  
  TestDriver.doIt(dao)
}*/