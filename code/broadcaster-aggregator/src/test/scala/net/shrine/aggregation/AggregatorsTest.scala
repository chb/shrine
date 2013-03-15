package net.shrine.aggregation

import junit.framework.TestCase
import org.scalatest.junit.ShouldMatchersForJUnit
import org.junit.Test
import net.shrine.protocol.AuthenticationInfo
import net.shrine.protocol.Credential
import net.shrine.protocol.query.QueryDefinition
import net.shrine.protocol.query.Term
import net.shrine.protocol.RunQueryRequest

/**
 * @author clint
 * @date Mar 14, 2013
 */
final class AggregatorsTest extends TestCase with ShouldMatchersForJUnit {
  @Test
  def testForRunQueryRequest {

    val authn = AuthenticationInfo("some-domain", "some-user", Credential("some-password", false))
    val projectId = "projectId"
    val queryDef = QueryDefinition("yo", Term("foo"))
    val request = RunQueryRequest(projectId, 1L, authn, 0L, "topicId", Set.empty, queryDef)
    
    def doTestRunQueryAggregatorFor(addAggregatedResult: Boolean) {
      val aggregator = Aggregators.forRunQueryRequest(addAggregatedResult)(request)

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
}