package net.shrine.adapter.components

import net.shrine.adapter.spring.AbstractShrineJUnitSpringTest
import net.shrine.adapter.AdapterTestHelpers
import org.junit.Test
import net.shrine.protocol.ReadI2b2AdminPreviousQueriesRequest
import net.shrine.protocol.ReadPreviousQueriesResponse
import org.scalatest.junit.ShouldMatchersForJUnit
import net.shrine.adapter.service.CanLoadTestData
import net.shrine.adapter.HasI2b2AdminPreviousQueriesDao
import net.shrine.adapter.dao.squeryl.AbstractSquerylAdapterTest

/**
 * @author clint
 * @date Apr 23, 2013
 */
final class I2b2AdminPreviousQueriesSourceComponentTest extends AbstractShrineJUnitSpringTest with AbstractSquerylAdapterTest with AdapterTestHelpers with ShouldMatchersForJUnit with CanLoadTestData with HasI2b2AdminPreviousQueriesDao {

  object TestI2b2AdminPreviousQueriesSourceComponent extends I2b2AdminPreviousQueriesSourceComponent {
    override def i2b2AdminDao = I2b2AdminPreviousQueriesSourceComponentTest.this.i2b2AdminDao
  }

  private def get = TestI2b2AdminPreviousQueriesSourceComponent.I2b2AdminPreviousQueries.get _

  @Test
  def testReadPreviousQueriesNoMatch = afterCreatingTables {
    val request = ReadI2b2AdminPreviousQueriesRequest(projectId, waitTimeMs, authn, "@", "some-query-name-that-doesn't-exist", 10)

    val resp = get(request).asInstanceOf[ReadPreviousQueriesResponse]

    validateUserAndGroupId(resp)

    resp.queryMasters should equal(Nil)
  }

  @Test
  def testReadPreviousQueriesAscending = doTestReadPreviousQueries(ReadI2b2AdminPreviousQueriesRequest.SortOrder.Ascending, Seq(queryName1, queryName2))

  @Test
  def testReadPreviousQueriesDescending = doTestReadPreviousQueries(ReadI2b2AdminPreviousQueriesRequest.SortOrder.Descending, Seq(queryName2, queryName1))

  private def doTestReadPreviousQueries(order: ReadI2b2AdminPreviousQueriesRequest.SortOrder, expectedNames: Seq[String], modify: ReadI2b2AdminPreviousQueriesRequest => ReadI2b2AdminPreviousQueriesRequest = r => r) = afterLoadingTestData {
    val baseRequest = modify(ReadI2b2AdminPreviousQueriesRequest(projectId, waitTimeMs, authn, authn.username, "query-name", 10, order))

    {
      val req = baseRequest

      val resp = get(req).asInstanceOf[ReadPreviousQueriesResponse]

      validateUserAndGroupId(resp)

      resp.queryMasters.map(_.name) should equal(expectedNames)
    }

    {
      val req = baseRequest.copy(maxResults = 1)

      val resp = get(req).asInstanceOf[ReadPreviousQueriesResponse]

      validateUserAndGroupId(resp)

      resp.queryMasters.map(_.name) should equal(Seq(expectedNames.head))
    }
  }

  @Test
  def testReadPreviousQueriesStartsWith {
    doTestReadPreviousQueries(ReadI2b2AdminPreviousQueriesRequest.SortOrder.Ascending, Seq(queryName1, queryName2), r => r.copy(searchStrategy = ReadI2b2AdminPreviousQueriesRequest.Strategy.Left))

    afterLoadingTestData {
      val request = ReadI2b2AdminPreviousQueriesRequest(projectId, waitTimeMs, authn, "@", "foo", 10, ReadI2b2AdminPreviousQueriesRequest.SortOrder.Ascending, ReadI2b2AdminPreviousQueriesRequest.Strategy.Left)

      val resp = get(request).asInstanceOf[ReadPreviousQueriesResponse]

      validateUserAndGroupId(resp)

      resp.queryMasters should equal(Nil)
    }
  }

  @Test
  def testReadPreviousQueriesEndsWith {
    {
      val request = ReadI2b2AdminPreviousQueriesRequest(projectId, waitTimeMs, authn, authn.username, "1", 10, ReadI2b2AdminPreviousQueriesRequest.SortOrder.Ascending, ReadI2b2AdminPreviousQueriesRequest.Strategy.Right)

      val resp = get(request).asInstanceOf[ReadPreviousQueriesResponse]

      validateUserAndGroupId(resp)

      resp.queryMasters.map(_.name) should equal(Seq(queryName1))
    }
    
    afterLoadingTestData {
      val request = ReadI2b2AdminPreviousQueriesRequest(projectId, waitTimeMs, authn, "@", "foo", 10, ReadI2b2AdminPreviousQueriesRequest.SortOrder.Ascending, ReadI2b2AdminPreviousQueriesRequest.Strategy.Right)

      val resp = get(request).asInstanceOf[ReadPreviousQueriesResponse]

      validateUserAndGroupId(resp)

      resp.queryMasters should equal(Nil)
    }
  }

  @Test
  def testReadPreviousQueriesExact = afterLoadingTestData {
    {
      val request = ReadI2b2AdminPreviousQueriesRequest(projectId, waitTimeMs, authn,"@", queryName1, 10, ReadI2b2AdminPreviousQueriesRequest.SortOrder.Ascending, ReadI2b2AdminPreviousQueriesRequest.Strategy.Exact)

      val resp = get(request).asInstanceOf[ReadPreviousQueriesResponse]

      validateUserAndGroupId(resp)

      resp.queryMasters.map(_.name) should equal(Seq(queryName1))
    }

    {
      val request = ReadI2b2AdminPreviousQueriesRequest(projectId, waitTimeMs, authn, "@", "foo", 10, ReadI2b2AdminPreviousQueriesRequest.SortOrder.Ascending, ReadI2b2AdminPreviousQueriesRequest.Strategy.Right)

      val resp = get(request).asInstanceOf[ReadPreviousQueriesResponse]

      validateUserAndGroupId(resp)

      resp.queryMasters should equal(Nil)
    }
    
    //Test with specific usernames
    {
      val request = ReadI2b2AdminPreviousQueriesRequest(projectId, waitTimeMs, authn, authn2.username, queryName2, 10, ReadI2b2AdminPreviousQueriesRequest.SortOrder.Ascending, ReadI2b2AdminPreviousQueriesRequest.Strategy.Exact)

      val resp = get(request).asInstanceOf[ReadPreviousQueriesResponse]

      validateUserAndGroupId(resp)

      resp.queryMasters.map(_.name) should equal(Seq(queryName2, queryName2))
    }

    {
      val request = ReadI2b2AdminPreviousQueriesRequest(projectId, waitTimeMs, authn, authn2.username, queryName1, 10, ReadI2b2AdminPreviousQueriesRequest.SortOrder.Ascending, ReadI2b2AdminPreviousQueriesRequest.Strategy.Right)

      val resp = get(request).asInstanceOf[ReadPreviousQueriesResponse]

      validateUserAndGroupId(resp)

      resp.queryMasters should equal(Nil)
    }
  }

  @Test
  def testReadPreviousQueriesContains = afterLoadingTestData {
    {
      val request = ReadI2b2AdminPreviousQueriesRequest(projectId, waitTimeMs, authn, "@", "-name", 10, ReadI2b2AdminPreviousQueriesRequest.SortOrder.Ascending, ReadI2b2AdminPreviousQueriesRequest.Strategy.Contains)

      val resp = get(request).asInstanceOf[ReadPreviousQueriesResponse]

      validateUserAndGroupId(resp)

      resp.queryMasters.map(_.name) should equal(Seq(queryName1, queryName2, queryName2, queryName2))
    }

    {
      val request = ReadI2b2AdminPreviousQueriesRequest(projectId, waitTimeMs, authn, "@", "foo", 10, ReadI2b2AdminPreviousQueriesRequest.SortOrder.Ascending, ReadI2b2AdminPreviousQueriesRequest.Strategy.Contains)

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