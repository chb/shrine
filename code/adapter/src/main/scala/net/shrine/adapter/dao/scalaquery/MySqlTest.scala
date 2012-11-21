package net.shrine.adapter.dao.scalaquery

import org.scalaquery.ql.extended.MySQLDriver
import org.scalaquery.session.Database
import com.mysql.jdbc.Driver
import net.shrine.protocol.AuthenticationInfo
import net.shrine.protocol.Credential
import net.shrine.protocol.query.QueryDefinition
import net.shrine.protocol.query.Term
import net.shrine.adapter.dao.model.ShrineQuery

//TODO: DELETE

object MySqlTest extends App {
  val db = Database.forURL("jdbc:mysql://localhost:3306/slicktest", "root", "slide&dense-plum", driver = classOf[Driver].getName)
  
  val dao = new ScalaQueryAdapterDao(db, MySQLDriver, SequenceHelper.mySql(db))
  
  TestDriver.doIt(dao)
}