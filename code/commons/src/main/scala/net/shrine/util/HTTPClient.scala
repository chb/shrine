package net.shrine.util

import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.Socket
import java.net.SocketAddress
import java.security.cert.X509Certificate
import java.security.KeyStore
import java.security.NoSuchAlgorithmException
import org.apache.log4j.Logger
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.TrustManagerFactory
import javax.net.ssl.X509TrustManager
import javax.net.ssl.HostnameVerifier
import javax.net.ssl.SSLSession
import com.sun.jersey.api.client.Client
import java.security.SecureRandom
import com.sun.jersey.client.urlconnection.HTTPSProperties
import com.sun.jersey.api.client.config.DefaultClientConfig
import com.sun.jersey.api.client.config.ClientConfig
import com.sun.jersey.api.client.WebResource
import javax.ws.rs.core.MediaType

/**
 * @author Bill Simons
 * @author clint
 * 
 * @date Aug 3, 2010
 * @link http://cbmi.med.harvard.edu
 * @link http://chip.org
 * <p/>
 * NOTICE: This software comes with NO guarantees whatsoever and is
 * licensed as Lgpl Open Source
 * @link http://www.gnu.org/licenses/lgpl.html
 */
object HTTPClient {
  def post(input: String, url: String, acceptAllSslCerts: Boolean = false): String = {
    createJerseyClient(acceptAllSslCerts).resource(url).entity(input, MediaType.TEXT_XML).post(classOf[String])
  }

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