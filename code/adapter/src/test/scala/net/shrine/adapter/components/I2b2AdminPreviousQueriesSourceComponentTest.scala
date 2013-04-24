package net.shrine.adapter.components

import net.shrine.adapter.spring.AbstractShrineJUnitSpringTest
import net.shrine.adapter.AdapterDbTest
import net.shrine.adapter.AdapterTestHelpers
import org.junit.Test
import net.shrine.protocol.ReadI2b2AdminPreviousQueriesRequest
import net.shrine.protocol.ReadPreviousQueriesResponse
import org.scalatest.junit.ShouldMatchersForJUnit
import net.shrine.adapter.service.CanLoadTestData

/**
 * @author clint
 * @date Apr 23, 2013
 */
final class I2b2AdminPreviousQueriesSourceComponentTest extends AbstractShrineJUnitSpringTest with AdapterDbTest with AdapterTestHelpers with ShouldMatchersForJUnit with CanLoadTestData {

  object TestI2b2AdminPreviousQueriesSourceComponent extends I2b2AdminPreviousQueriesSourceComponent {
    override def dao = I2b2AdminPreviousQueriesSourceComponentTest.this.dao
  }

  private def get = TestI2b2AdminPreviousQueriesSourceComponent.I2b2AdminPreviousQueries.get _

  @Test
  def testReadPreviousQueriesNoMatch = afterCreatingTables {
    val request = ReadI2b2AdminPreviousQueriesRequest(projectId, waitTimeMs, authn, "some-query-name-that-doesn't-exist", 10)

    val resp = get(request).asInstanceOf[ReadPreviousQueriesResponse]

    validateUserAndGroupId(resp)

    resp.queryMasters should equal(Nil)
  }

  @Test
  def testReadPreviousQueriesAscending = afterLoadingTestData {
    val request = ReadI2b2AdminPreviousQueriesRequest(projectId, waitTimeMs, authn, "query-name", 10, ReadI2b2AdminPreviousQueriesRequest.SortOrder.Ascending)

    val resp = get(request).asInstanceOf[ReadPreviousQueriesResponse]

    validateUserAndGroupId(resp)

    resp.queryMasters.map(_.name) should equal(Seq(queryName1, queryName2))
  }

  @Test
  def testReadPreviousQueriesDescending = afterLoadingTestData {
    val request = ReadI2b2AdminPreviousQueriesRequest(projectId, waitTimeMs, authn, "query-name", 10, ReadI2b2AdminPreviousQueriesRequest.SortOrder.Descending)

    val resp = get(request).asInstanceOf[ReadPreviousQueriesResponse]

    validateUserAndGroupId(resp)

    resp.queryMasters.map(_.name) should equal(Seq(queryName2, queryName1))
  }

  @Test
  def testReadPreviousQueriesStartsWith = afterLoadingTestData {
    {
      val request = ReadI2b2AdminPreviousQueriesRequest(projectId, waitTimeMs, authn, "query-name", 10, ReadI2b2AdminPreviousQueriesRequest.SortOrder.Ascending, ReadI2b2AdminPreviousQueriesRequest.Strategy.Left)

      val resp = get(request).asInstanceOf[ReadPreviousQueriesResponse]

      validateUserAndGroupId(resp)

      resp.queryMasters.map(_.name) should equal(Seq(queryName1, queryName2))
    }

    {
      val request = ReadI2b2AdminPreviousQueriesRequest(projectId, waitTimeMs, authn, "foo", 10, ReadI2b2AdminPreviousQueriesRequest.SortOrder.Ascending, ReadI2b2AdminPreviousQueriesRequest.Strategy.Left)

      val resp = get(request).asInstanceOf[ReadPreviousQueriesResponse]

      validateUserAndGroupId(resp)

      resp.queryMasters should equal(Nil)
    }
  }
  
  @Test
  def testReadPreviousQueriesEndsWith = afterLoadingTestData {
    {
      val request = ReadI2b2AdminPreviousQueriesRequest(projectId, waitTimeMs, authn, "2", 10, ReadI2b2AdminPreviousQueriesRequest.SortOrder.Ascending, ReadI2b2AdminPreviousQueriesRequest.Strategy.Right)

      val resp = get(request).asInstanceOf[ReadPreviousQueriesResponse]

      validateUserAndGroupId(resp)

      resp.queryMasters.map(_.name) should equal(Seq(queryName2))
    }

    {
      val request = ReadI2b2AdminPreviousQueriesRequest(projectId, waitTimeMs, authn, "foo", 10, ReadI2b2AdminPreviousQueriesRequest.SortOrder.Ascending, ReadI2b2AdminPreviousQueriesRequest.Strategy.Right)

      val resp = get(request).asInstanceOf[ReadPreviousQueriesResponse]

      validateUserAndGroupId(resp)

      resp.queryMasters should equal(Nil)
    }
  }

  @Test
  def testReadPreviousQueriesExact = afterLoadingTestData {
    {
      val request = ReadI2b2AdminPreviousQueriesRequest(projectId, waitTimeMs, authn, queryName1, 10, ReadI2b2AdminPreviousQueriesRequest.SortOrder.Ascending, ReadI2b2AdminPreviousQueriesRequest.Strategy.Exact)

      val resp = get(request).asInstanceOf[ReadPreviousQueriesResponse]

      validateUserAndGroupId(resp)

      resp.queryMasters.map(_.name) should equal(Seq(queryName1))
    }

    {
      val request = ReadI2b2AdminPreviousQueriesRequest(projectId, waitTimeMs, authn, "foo", 10, ReadI2b2AdminPreviousQueriesRequest.SortOrder.Ascending, ReadI2b2AdminPreviousQueriesRequest.Strategy.Right)

      val resp = get(request).asInstanceOf[ReadPreviousQueriesResponse]

      validateUserAndGroupId(resp)

      resp.queryMasters should equal(Nil)
    }
  }

  private def validateUserAndGroupId(resp: ReadPreviousQueriesResponse) {
    resp.userId should be(Some(authn.username))
    resp.groupId should be(Some(authn.domain))
  }
}