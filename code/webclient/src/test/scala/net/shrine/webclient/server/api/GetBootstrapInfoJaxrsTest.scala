package net.shrine.webclient.server.api

import org.scalatest.junit.AssertionsForJUnit
import org.scalatest.matchers.ShouldMatchers
import com.sun.jersey.test.framework.JerseyTest
import org.junit.Test
import net.shrine.webclient.server.QueryService
import net.shrine.webclient.server.OntologyService
import net.shrine.webclient.client.domain.BootstrapInfo

/**
 * @author clint
 * @date Aug 7, 2012
 */
final class GetBootstrapInfoJaxrsTest extends JerseyTest with ShrineWebclientApiJaxrsTest {
  
  private lazy val toReturn = Map("fooInst" -> 123, "barInst" -> 42)

  @Test
  def testSubmit {
    val bootstrapInfo = unmarshal[BootstrapInfo](resource.path("api/bootstrap").get(classOf[String])).get
    
    bootstrapInfo.loggedInUsername should equal(loggedInUser)
  }
}