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
    val shrineQuery = ShrineQuery(123, 456L, "some-query-name", "foo", "bar", Term("nuh"), Util.now)
    
    val queryMaster = shrineQuery.toQueryMaster
    
    queryMaster.queryMasterId.toLong should equal(shrineQuery.networkId)
    queryMaster.name should equal(shrineQuery.name)
    queryMaster.userId should equal(shrineQuery.username)
    queryMaster.groupId should equal(shrineQuery.domain)
    queryMaster.createDate should equal(shrineQuery.dateCreated)
  }
}