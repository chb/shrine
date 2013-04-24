package net.shrine.adapter.components

import net.shrine.adapter.AdapterDbTest
import net.shrine.adapter.AdapterTestHelpers
import org.scalatest.junit.ShouldMatchersForJUnit
import net.shrine.adapter.spring.AbstractShrineJUnitSpringTest
import org.junit.Test
import net.shrine.protocol.ReadQueryDefinitionRequest
import net.shrine.protocol.ErrorResponse
import net.shrine.protocol.query.Term
import net.shrine.protocol.ReadQueryDefinitionResponse
import net.shrine.protocol.query.QueryDefinition
import net.shrine.adapter.service.CanLoadTestData

/**
 * @author clint
 * @date Apr 23, 2013
 */
final class QueryDefinitionSourceComponentTest extends AbstractShrineJUnitSpringTest with AdapterDbTest with AdapterTestHelpers with ShouldMatchersForJUnit with CanLoadTestData {
  private object TestQueryDefinitionSourceComponent extends QueryDefinitionSourceComponent {
    override def dao = QueryDefinitionSourceComponentTest.this.dao
  }
  
  private def get = TestQueryDefinitionSourceComponent.QueryDefinitions.get _
  
  @Test
  def testGetQueryIsPresent = afterLoadingTestData {
    val request = ReadQueryDefinitionRequest(projectId, waitTimeMs, authn, networkQueryId1)
    
    val resp = get(request).asInstanceOf[ReadQueryDefinitionResponse]
    
    resp should not be(null)
    resp.createDate should not be(null)
    resp.masterId should equal(networkQueryId1.toLong)
    resp.name should equal(queryName1)
    resp.queryDefinition should equal(queryDef1.toI2b2String)
    resp.userId should equal(authn.username)
  }
  
  @Test
  def testGetQueryIsNOTPresent = afterCreatingTables {
    val request = ReadQueryDefinitionRequest(projectId, waitTimeMs, authn, queryId)
    
    val resp = get(request)
    
    resp should not be(null)
    
    resp.isInstanceOf[ErrorResponse] should be(true)
  }
}