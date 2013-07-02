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
import java.io.File
import org.spin.tools.crypto.PKITool
import javax.net.ssl.TrustManagerFactory
import javax.net.ssl.KeyManagerFactory
import javax.net.ssl.X509KeyManager
import javax.net.ssl.KeyManager
import java.security.Principal
import java.net.Socket

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

  private def spinKeystore = PKITool.getInstance.getKeystore

  /**
   * From a SO post inspired from http://java.sun.com/javase/6/docs/technotes/guides/security/jsse/JSSERefGuide.html
   */
  private[util] lazy val spinTrustManager: X509TrustManager = {

    //The Spin PKIX X509TrustManager that we will delegate to.
    val trustManagerFactory: TrustManagerFactory = TrustManagerFactory.getInstance("PKIX")

    trustManagerFactory.init(spinKeystore)

    //Look for an instance of X509TrustManager.  If found, use that.
    trustManagerFactory.getTrustManagers.collect {
      case trustManager: X509TrustManager => trustManager
    }.headOption.getOrElse(throw new IllegalStateException("Couldn't initialize SSL TrustManager: No X509TrustManagers found"))
  }

  /**
   * From a SO post inspired from http://java.sun.com/javase/6/docs/technotes/guides/security/jsse/JSSERefGuide.html
   */
  private[util] lazy val spinKeyManager: X509KeyManager = {

    def spinKeystorePassword: Array[Char] = PKITool.getInstance.getConfig.getPasswordAsCharArray

    //The Spin PKIX X509KeyManager that we will delegate to.
    val keyManagerFactory: KeyManagerFactory = KeyManagerFactory.getInstance("SunX509", "SunJSSE")

    keyManagerFactory.init(spinKeystore, spinKeystorePassword)

    keyManagerFactory.getKeyManagers.collect {
      case keyManager: X509KeyManager => keyManager
    }.headOption.getOrElse(throw new IllegalStateException("Couldn't initialize SSL KeyManager: No X509KeyManagers found"))
  }

  def createJerseyClient(acceptAllSslCerts: Boolean): Client = {
    def tlsContext = SSLContext.getInstance("TLS")

    val (sslContext, hostNameVerifier) = {
      val context = tlsContext
      
      if (!acceptAllSslCerts) {
        context.init(Array(spinKeyManager), Array(spinTrustManager), null)

        (context, null.asInstanceOf[HostnameVerifier])
      } else {
        context.init(null, Array[TrustManager](TrustsAllCertsTrustManager), new SecureRandom)

        (context, TrustsAllCertsHostnameVerifier)
      }
    }

    val httpsProperties = new HTTPSProperties(hostNameVerifier, sslContext)

    val config: ClientConfig = new DefaultClientConfig

    config.getProperties.put(HTTPSProperties.PROPERTY_HTTPS_PROPERTIES, httpsProperties)

    Client.create(config)
  }
}
