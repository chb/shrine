package net.shrine.adapter.dao.model

import org.junit.Test
import org.scalatest.junit.ShouldMatchersForJUnit

import junit.framework.TestCase
import net.shrine.protocol.query.Term
import net.shrine.util.Util

/**
 * @author clint
 * @date Oct 31, 2012
 */
final class ShrineQueryTest extends TestCase with ShouldMatchersForJUnit {
  @Test
  def testToQueryMaster {
    val shrineQuery = ShrineQuery(123, "master-id", 456L, "some-query-name", "foo", "bar", Term("nuh"), Util.now)

    def doTestToQueryMaster(idField: ShrineQuery => String) {
      val queryMaster = shrineQuery.toQueryMaster(idField)

      //TODO: Should this be the real i2b2 master id now?  That would break previous queries, though
      queryMaster.queryMasterId should equal(idField(shrineQuery))
      queryMaster.name should equal(shrineQuery.name)
      queryMaster.userId should equal(shrineQuery.username)
      queryMaster.groupId should equal(shrineQuery.domain)
      queryMaster.createDate should equal(shrineQuery.dateCreated)
    }

    doTestToQueryMaster(_.networkId.toString)
    
    doTestToQueryMaster(_.localId)
  }
}