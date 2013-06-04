package net.shrine.adapter.service

import com.sun.jersey.test.framework.JerseyTest
import org.scalatest.junit.ShouldMatchersForJUnit
import junit.framework.TestCase
import com.sun.jersey.test.framework.AppDescriptor
import net.shrine.util.JerseyAppDescriptor
import net.shrine.util.JerseyHttpClient
import org.junit.Test
import net.shrine.protocol.ReadQueryDefinitionRequest
import net.shrine.protocol.ReadI2b2AdminPreviousQueriesRequest
import net.shrine.protocol.query.QueryDefinition
import net.shrine.protocol.query.Term
import net.shrine.protocol.AuthenticationInfo
import net.shrine.protocol.Credential
import net.shrine.protocol.QueryMaster
import net.shrine.util.Util
import net.shrine.adapter.spring.AbstractShrineJUnitSpringTest
import net.shrine.util.HttpClient
import net.shrine.util.XmlUtil
import net.shrine.protocol.ErrorResponse
import org.junit.Before
import org.junit.After
import net.shrine.protocol.ReadQueryDefinitionResponse
import scala.xml.XML
import net.shrine.protocol.ReadPreviousQueriesResponse
import net.shrine.adapter.AdapterTestHelpers
import net.shrine.adapter.dao.I2b2AdminPreviousQueriesDao
import net.shrine.adapter.HasI2b2AdminPreviousQueriesDao

/**
 * @author clint
 * @date Apr 12, 2013
 *
 * NB: Ideally we would extend JerseyTest here, but since we have to extend AbstractDependencyInjectionSpringContextTests,
 * we get into a diamond-problem when extending JerseyTest as well, even when both of them are extended by shim traits.
 *
 * We work around this issue by mising in JerseyTestCOmponent, which brings in a JerseyTest by composition, and ensures
 * that it is set up and torn down properly.
 */
final class I2b2AdminResourceEndToEndJaxrsTest extends AbstractI2b2AdminResourceJaxrsTest with HasI2b2AdminPreviousQueriesDao {

  override def makeHandler = new I2b2AdminService(dao, i2b2AdminDao, AlwaysAuthenticatesMockPmHttpClient, "")
  
  @Test
  def testReadQueryDefinition = afterLoadingTestData {
    doTestReadQueryDefinition(networkQueryId1, Some((queryName1, queryDef1)))
  }
  
  @Test
  def testReadQueryDefinitionUnknownQueryId = afterLoadingTestData {
    doTestReadQueryDefinition(87134682364L, None)
  }
  
  @Test
  def testReadI2b2AdminPreviousQueries = afterLoadingTestData {
    val searchString = queryName1
    val maxResults = 123
    val sortOrder = ReadI2b2AdminPreviousQueriesRequest.SortOrder.Ascending
    val categoryToSearchWithin = ReadI2b2AdminPreviousQueriesRequest.Category.All
    val searchStrategy = ReadI2b2AdminPreviousQueriesRequest.Strategy.Exact

    val request = ReadI2b2AdminPreviousQueriesRequest(projectId, waitTimeMs, authn, searchString, maxResults, sortOrder, searchStrategy, categoryToSearchWithin)

    doTestReadI2b2AdminPreviousQueries(request, Some(queryMaster1))
  }
  
  @Test
  def testReadI2b2AdminPreviousQueriesNoResultsExpected = afterLoadingTestData {
    //A request that won't return anything
    val request = ReadI2b2AdminPreviousQueriesRequest(projectId, waitTimeMs, authn, "askjdhakfgkafgkasf", 123)
    
    doTestReadI2b2AdminPreviousQueries(request, None)
  }
}
