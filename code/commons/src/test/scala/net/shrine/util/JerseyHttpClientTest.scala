package net.shrine.util

import junit.framework.TestCase
import org.junit.Test
import com.sun.jersey.api.client.config.DefaultClientConfig
import com.sun.jersey.client.urlconnection.HTTPSProperties
import org.scalatest.junit.ShouldMatchersForJUnit

/**
 * @author clint
 * @date Aug 2, 2012
 */
final class JerseyHttpClientTest extends TestCase with ShouldMatchersForJUnit {
  @Test
  def testTrustsAllCertsHostnameVerifier {
    import JerseyHttpClient.TrustsAllCertsHostnameVerifier._

    //These assertions aren't great, but they're about the best we can do;
    //TrustsAllCertsHostnameVerifier should return true for all input
    verify(null, null) should equal(true)
    verify("", null) should equal(true)
    verify("asklfjalksf", null) should equal(true)
  }

  @Test
  def testTrustsAllCertsTrustManager {
    import JerseyHttpClient.TrustsAllCertsTrustManager._

    getAcceptedIssuers should be(null)

    //We can't prove that these two don't have side effects, but we can check that they don't throw 
    checkClientTrusted(Array(), "")
    checkServerTrusted(Array(), "")
  }

  @Test
  def testCreateClientAndWebResource {
    import JerseyHttpClient.createJerseyClient
    import scala.collection.JavaConverters._

    val defaultClientConfig = new DefaultClientConfig

    type HasProperties = {
      def getProperties(): java.util.Map[String, AnyRef]
    }

    def doChecksCertsClientTest(client: HasProperties) {
      client should not be (null)
      //check that we only have default properties
      //turn property maps to Scala maps to get workable equals()
      client.getProperties.asScala should equal(defaultClientConfig.getProperties.asScala)
    }

    def doTrustsAllCertsClientTest(client: HasProperties) {
      client should not be (null)

      import HTTPSProperties.{ PROPERTY_HTTPS_PROPERTIES => httpsPropsKey }

      val clientProps = client.getProperties.asScala
      val propertiesWithoutHttpsProperties = clientProps - httpsPropsKey
      val httpsProperties = clientProps(httpsPropsKey).asInstanceOf[HTTPSProperties]

      propertiesWithoutHttpsProperties should equal(defaultClientConfig.getProperties.asScala)

      httpsProperties should not be (null)
      httpsProperties.getHostnameVerifier should be(JerseyHttpClient.TrustsAllCertsHostnameVerifier)
      httpsProperties.getSSLContext should not be (null)
      httpsProperties.getSSLContext.getProtocol should equal("TLS")
      //Would be nice to test that the SSLContext correctly uses TrustsAllCertsTrustManager, but this doesn't seem possible
    }

    val uri = "http://example.com"
    
    {
      val client = createJerseyClient(acceptAllSslCerts = false)
      
      doChecksCertsClientTest(client)
      
      val webResource = client.resource(uri)
      
      doChecksCertsClientTest(webResource)
      
      webResource.getURI.toString should equal(uri)
    }

    {
      val client = createJerseyClient(acceptAllSslCerts = true)
      
      doTrustsAllCertsClientTest(client)
      
      val webResource = client.resource(uri)

      doTrustsAllCertsClientTest(webResource)
      
      webResource.getURI.toString should equal(uri)
    }
  }
}