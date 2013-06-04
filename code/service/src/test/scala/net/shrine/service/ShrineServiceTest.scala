package net.shrine.service

import scala.collection.JavaConverters._
import org.easymock.EasyMock.{ expect => invoke }
import org.easymock.EasyMock.isA
import org.easymock.EasyMock.same
import org.junit.Test
import org.scalatest.junit.AssertionsForJUnit
import org.scalatest.junit.ShouldMatchersForJUnit
import org.scalatest.mock.EasyMockSugar
import org.spin.client.AgentException
import org.spin.tools.crypto.signature.CertID
import org.spin.tools.crypto.signature.Identity
import org.spin.tools.crypto.Envelope
import org.spin.tools.Interval
import net.shrine.aggregation.Aggregator
import net.shrine.aggregation.SpinResultEntry
import net.shrine.protocol.AuthenticationInfo
import net.shrine.protocol.BroadcastMessage
import net.shrine.protocol.Credential
import net.shrine.protocol.DeleteQueryRequest
import net.shrine.protocol.ErrorResponse
import net.shrine.protocol.ShrineResponse
import org.spin.message.QueryInfo
import org.spin.message.QueryInput
import org.spin.message.AckNack
import org.spin.message.serializer.BasicSerializer
import org.spin.message.ResultSet
import org.spin.message.Failure
import org.spin.message.Result
import org.spin.message.StatusCode
import net.shrine.protocol.ReadQueryInstancesRequest
import net.shrine.protocol.ReadQueryInstancesResponse
import org.spin.client.SpinClient
import scala.concurrent.Future
import org.spin.message.serializer.Stringable
import org.spin.client.Credentials
import org.spin.tools.config.DefaultPeerGroups
import net.shrine.protocol.QueryInstance
import net.shrine.protocol.RunQueryRequest
import net.shrine.protocol.query.QueryDefinition
import net.shrine.protocol.query.Term
import net.shrine.broadcaster.dao.AbstractAuditDaoTest
import net.shrine.authorization.QueryAuthorizationService
import net.shrine.protocol.ReadApprovedQueryTopicsRequest
import net.shrine.protocol.ReadApprovedQueryTopicsResponse

/**
 * @author Bill Simons
 * @author Clint Gilbert
 * @date 3/30/11
 * @link http://cbmi.med.harvard.edu
 * @link http://chip.org
 *       <p/>
 *       NOTICE: This software comes with NO guarantees whatsoever and is
 *       licensed as Lgpl Open Source
 * @link http://www.gnu.org/licenses/lgpl.html
 */
final class ShrineServiceTest extends AbstractAuditDaoTest with AssertionsForJUnit with EasyMockSugar {

  @Test
  def testReadQueryInstances {
    val projectId = "foo"
    val queryId = 123L
    val authn = AuthenticationInfo("some-domain", "some-username", Credential("blarg", false))
    val req = ReadQueryInstancesRequest(projectId, 1L, authn, queryId)

    val service = new ShrineService(null, null, true, null, null)

    val response = service.readQueryInstances(req).asInstanceOf[ReadQueryInstancesResponse]

    response should not be (null)
    response.groupId should equal(projectId)
    response.queryMasterId should equal(queryId)
    response.userId should equal(authn.username)

    val Seq(instance) = response.queryInstances

    instance.startDate should not be (null)
    instance.endDate should not be (null)
    instance.startDate should equal(instance.endDate)
    instance.groupId should equal(projectId)
    instance.queryInstanceId should equal(queryId.toString)
    instance.queryMasterId should equal(queryId.toString)
    instance.userId should equal(authn.username)
  }

  private val authn = AuthenticationInfo("some-domain", "some-user", Credential("some-password", false))
  private val projectId = "projectId"
  private val queryDef = QueryDefinition("yo", Term("foo"))
  private val request = RunQueryRequest(projectId, 1L, authn, 0L, "topicId", Set.empty, queryDef)

  @Test
  def testRunQueryAggregatorFor {

    def doTestRunQueryAggregatorFor(addAggregatedResult: Boolean) {
      val service = new ShrineService(null, null, addAggregatedResult, null, null)

      val aggregator = service.runQueryAggregatorFor(request)

      aggregator should not be (null)

      aggregator.queryId should be(-1L)
      aggregator.groupId should be(projectId)
      aggregator.userId should be(authn.username)
      aggregator.requestQueryDefinition should be(queryDef)
      aggregator.addAggregatedResult should be(addAggregatedResult)
    }

    doTestRunQueryAggregatorFor(true)
    doTestRunQueryAggregatorFor(false)
  }

  @Test
  def testAuditTransactionally = afterMakingTables {
    def doTestAuditTransactionally(shouldThrow: Boolean) {
      val service = new ShrineService(auditDao, null, true, null, null)

      if (shouldThrow) {
        intercept[Exception] {
          service.auditTransactionally(request)(throw new Exception)
        }
      } else {
        val x = 1

        val actual = service.auditTransactionally(request)(x)

        actual should be(x)
      }

      //We should have recorded an audit entry no matter what
      val Seq(entry) = auditDao.findRecentEntries(1)

      entry.domain should be(authn.domain)
      entry.username should be(authn.username)
      entry.project should be(projectId)
      entry.queryText should be(Some(queryDef.toI2b2String))
      entry.queryTopic should be(Some(request.topicId))
      entry.time should not be (null)
    }

    doTestAuditTransactionally(false)
    doTestAuditTransactionally(true)
  }

  @Test
  def testAfterAuditingAndAuthorizing = afterMakingTables {

    final class MockAuthService(shouldWork: Boolean) extends QueryAuthorizationService {
      def authorizeRunQueryRequest(request: RunQueryRequest) {
        if (!shouldWork) {
          throw new Exception
        }
      }

      def readApprovedEntries(request: ReadApprovedQueryTopicsRequest): ReadApprovedQueryTopicsResponse = ???
    }

    def doAfterAuditingAndAuthorizing(shouldBeAuthorized: Boolean, shouldThrow: Boolean) {
      val service = new ShrineService(auditDao, new MockAuthService(shouldBeAuthorized), true, null, null)

      if (shouldThrow || !shouldBeAuthorized) {
        intercept[Exception] {
          service.afterAuditingAndAuthorizing(request)(throw new Exception)
        }
      } else {
        val x = 1

        val actual = service.afterAuditingAndAuthorizing(request)(x)

        actual should be(x)
      }

      //We should have recorded an audit entry no matter what
      val Seq(entry) = auditDao.findRecentEntries(1)

      entry.domain should be(authn.domain)
      entry.username should be(authn.username)
      entry.project should be(projectId)
      entry.queryText should be(Some(queryDef.toI2b2String))
      entry.queryTopic should be(Some(request.topicId))
      entry.time should not be (null)
    }

    doAfterAuditingAndAuthorizing(true, true)
    doAfterAuditingAndAuthorizing(true, false)
    doAfterAuditingAndAuthorizing(false, true)
    doAfterAuditingAndAuthorizing(false, false)
  }
}