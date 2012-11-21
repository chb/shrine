package net.shrine.adapter.dao.scalaquery

import net.shrine.adapter.dao.AdapterDao
import net.shrine.protocol.AuthenticationInfo
import net.shrine.protocol.Credential
import net.shrine.protocol.query.QueryDefinition
import net.shrine.protocol.query.Term

///TODO: DELETE

object TestDriver {

  def doIt(dao: AdapterDao) {
    val id = dao.insertQuery(12345L, "foo", AuthenticationInfo("some-domain", "some-user", Credential("blah", false)), Term("""\\SHRINE\SHRINE\nuh"""))
    
    println(id)

    println(dao.findQueryByNetworkId(12345L))

    //dao.findQueryByNetworkId(12345).foreach(q => println(dao.findResultsFor(q.networkId)))
  }
}
