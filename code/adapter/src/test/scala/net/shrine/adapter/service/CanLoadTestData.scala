package net.shrine.adapter.service

import net.shrine.adapter.AdapterTestHelpers
import net.shrine.adapter.dao.squeryl.AbstractSquerylAdapterTest
import net.shrine.protocol.AuthenticationInfo
import net.shrine.protocol.Credential

/**
 * @author clint
 * @date Apr 23, 2013
 */
trait CanLoadTestData { self: AbstractSquerylAdapterTest with AdapterTestHelpers =>
  lazy val authn2 = AuthenticationInfo(authn.domain, "a-different-user", Credential("jkafhkjdhsfjksdhfkjsdg", false))
  val networkQueryId3 = -42L
  val networkQueryId4 = -99L
  
  protected def loadTestData() {
    dao.insertQuery(masterId1, networkQueryId1, queryName1, authn, queryDef1.expr)
    dao.insertQuery(masterId2, networkQueryId2, queryName2, authn, queryDef2.expr)
    dao.insertQuery(masterId2, networkQueryId3, queryName2, authn2, queryDef2.expr)
    dao.insertQuery(masterId2, networkQueryId4, queryName2, authn2.copy(domain = "some-completely-different-domain"), queryDef2.expr)
  }

  protected def afterLoadingTestData(f: => Any): Unit = afterCreatingTables {
    try {
      loadTestData()
    } finally {
      f
    }
  }
}