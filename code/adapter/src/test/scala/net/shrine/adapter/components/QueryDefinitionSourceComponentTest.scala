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

/**
 * @author clint
 * @date Apr 23, 2013
 */
final class QueryDefinitionSourceComponentTest extends AbstractShrineJUnitSpringTest with AdapterDbTest with AdapterTestHelpers with ShouldMatchersForJUnit {
  object TestQueryDefinitionSourceComponent extends QueryDefinitionSourceComponent {
    override def dao = QueryDefinitionSourceComponentTest.this.dao
  }
  
  val projectId = "foo"
    
  val waitTimeMs = 1000L
  
  @Test
  def testGetQueryIsPresent = afterCreatingTables {
    val expr = Term("foo")
    val queryName = "query-name"
    val masterId = "some-master-id"
      
    dao.insertQuery(masterId, queryId, queryName, authn, expr)
    
    val request = ReadQueryDefinitionRequest(projectId, waitTimeMs, authn, queryId)
    
    val resp = TestQueryDefinitionSourceComponent.QueryDefinitions.get(request).asInstanceOf[ReadQueryDefinitionResponse]
    
    resp should not be(null)
    resp.createDate should not be(null)
    resp.masterId should equal(queryId.toLong)
    resp.name should equal(queryName)
    resp.queryDefinition should equal(QueryDefinition(queryName, expr).toI2b2String)
    resp.userId should equal(authn.username)
  }
  
  @Test
  def testGetQueryIsNOTPresent = afterCreatingTables {
    val request = ReadQueryDefinitionRequest(projectId, waitTimeMs, authn, queryId)
    
    val resp = TestQueryDefinitionSourceComponent.QueryDefinitions.get(request)
    
    resp should not be(null)
    
    resp.isInstanceOf[ErrorResponse] should be(true)
  }
}