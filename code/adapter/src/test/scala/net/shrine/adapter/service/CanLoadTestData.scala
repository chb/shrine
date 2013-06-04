package net.shrine.adapter.service

import net.shrine.adapter.AdapterTestHelpers
import net.shrine.adapter.dao.squeryl.AbstractSquerylAdapterTest

/**
 * @author clint
 * @date Apr 23, 2013
 */
trait CanLoadTestData { self: AbstractSquerylAdapterTest with AdapterTestHelpers =>
  protected def loadTestData() {
    dao.insertQuery(masterId1, networkQueryId1, queryName1, authn, queryDef1.expr)
    dao.insertQuery(masterId2, networkQueryId2, queryName2, authn, queryDef2.expr)
  }

  protected def afterLoadingTestData(f: => Any): Unit = afterCreatingTables {
    try {
      loadTestData()
    } finally {
      f
    }
  }
}