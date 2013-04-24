package net.shrine.adapter.service

import net.shrine.adapter.AdapterTestHelpers
import org.scalatest.junit.ShouldMatchersForJUnit
import net.shrine.adapter.AdapterDbTest
import net.shrine.adapter.spring.AbstractShrineJUnitSpringTest
import net.shrine.util.HttpClient
import net.shrine.protocol.ErrorResponse
import net.shrine.util.JerseyHttpClient
import org.junit.Before
import org.junit.After
import org.junit.Test
import net.shrine.protocol.ReadQueryDefinitionRequest
import net.shrine.protocol.ReadI2b2AdminPreviousQueriesRequest

/**
 * @author clint
 * @date Apr 24, 2013
 */
final class I2b2AdminResourceEndToEndNotAuthorizedJaxrsTest extends AbstractI2b2AdminResourceJaxrsTest {
  
  override def makeHandler = new I2b2AdminService(dao, NeverAuthenticatesMockPmHttpClient, "")
  
  @Test
  def testReadQueryDefinitionNotAuthorized = afterLoadingTestData {
    //Query for a query def we know is present
    val req = ReadQueryDefinitionRequest(projectId, waitTimeMs, authn, networkQueryId1)
    
    val resp = adminClient.readQueryDefinition(req)
    
    resp.isInstanceOf[ErrorResponse] should be(true)
  }

  @Test
  def testReadI2b2AdminPreviousQueriesNotAuthorized = afterLoadingTestData {
    //Query for a queries we know are present
    val req = ReadI2b2AdminPreviousQueriesRequest(projectId, waitTimeMs, authn, queryName1, 10)
    
    val resp = adminClient.readI2b2AdminPreviousQueries(req)
    
    resp.isInstanceOf[ErrorResponse] should be(true)
  }
}
