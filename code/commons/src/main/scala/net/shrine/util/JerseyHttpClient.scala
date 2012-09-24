package net.shrine.util

import java.security.SecureRandom
import java.security.cert.X509Certificate

import com.sun.jersey.api.client.Client
import com.sun.jersey.api.client.config.ClientConfig
import com.sun.jersey.api.client.config.DefaultClientConfig
import com.sun.jersey.client.urlconnection.HTTPSProperties

import javax.net.ssl.HostnameVerifier
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLSession
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager
import javax.ws.rs.core.MediaType

/**
 * @author Bill Simons
 * @author clint
 *
 * @date Sep 20, 2012
 * @link http://cbmi.med.harvard.edu
 * @link http://chip.org
 * <p/>
 * NOTICE: This software comes with NO guarantees whatsoever and is
 * licensed as Lgpl Open Source
 * @link http://www.gnu.org/licenses/lgpl.html
 */
final class JerseyHttpClient(acceptAllSslCerts: Boolean = false) extends HttpClient {
  import JerseyHttpClient._

  private lazy val client = createJerseyClient(acceptAllSslCerts)

  override def post(input: String, url: String): String = {
    client.resource(url).entity(input, MediaType.TEXT_XML).post(classOf[String])
  }
}

object JerseyHttpClient {
  private[util] object TrustsAllCertsHostnameVerifier extends HostnameVerifier {
    override def verify(s: String, sslSession: SSLSession) = true
  }

  private[util] object TrustsAllCertsTrustManager extends X509TrustManager {
    override def getAcceptedIssuers(): Array[X509Certificate] = null
    override def checkClientTrusted(certs: Array[X509Certificate], authType: String): Unit = ()
    override def checkServerTrusted(certs: Array[X509Certificate], authType: String): Unit = ()
  }

  def createJerseyClient(acceptAllSslCerts: Boolean): Client = {
    if (!acceptAllSslCerts) {
      Client.create
    } else {
      // Create a trust manager that does not validate certificate chains
      val trustsAllCerts: Array[TrustManager] = Array(TrustsAllCertsTrustManager)

      // Install the all-trusting trust manager in an SSLContext
      val sslContext = {
        val context = SSLContext.getInstance("TLS")

        context.init(null, trustsAllCerts, new SecureRandom)

        context
      }

      val httpsProperties = new HTTPSProperties(TrustsAllCertsHostnameVerifier, sslContext)

      val config: ClientConfig = new DefaultClientConfig

      config.getProperties.put(HTTPSProperties.PROPERTY_HTTPS_PROPERTIES, httpsProperties)

      Client.create(config)
    }
  }
}
