package net.shrine.util

import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.Socket
import java.net.SocketAddress
import java.security.cert.X509Certificate
import java.security.KeyStore
import java.security.NoSuchAlgorithmException
import org.apache.commons.httpclient.methods.PostMethod
import org.apache.commons.httpclient.methods.RequestEntity
import org.apache.commons.httpclient.methods.StringRequestEntity
import org.apache.commons.httpclient.params.HttpConnectionParams
import org.apache.commons.httpclient.protocol.SecureProtocolSocketFactory
import org.apache.commons.httpclient.HttpClient
import org.apache.commons.httpclient.HttpClientError
import org.apache.log4j.Logger
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.TrustManagerFactory
import javax.net.ssl.X509TrustManager
import net.shrine.util.HTTPClient.EasySSLProtocolSocketFactory
import net.shrine.util.HTTPClient.EasyX509TrustManager
import org.apache.commons.httpclient.protocol.Protocol

/**
 * @author Bill Simons
 * @date Aug 3, 2010
 * @link http://cbmi.med.harvard.edu
 * @link http://chip.org
 * <p/>
 * NOTICE: This software comes with NO guarantees whatsoever and is
 * licensed as Lgpl Open Source
 * @link http://www.gnu.org/licenses/lgpl.html
 */
object HTTPClient {
  def post(input: String, url: String, trustAllSslCerts: Boolean = false): String = {
    restoringOldHttpsProtocol(trustAllSslCerts) {
      val method = new PostMethod(url)

      val entity: RequestEntity = new StringRequestEntity(input, "text/xml", null)

      method.setRequestEntity(entity)
      
      val client = new HttpClient

      client.executeMethod(method)

      method.getResponseBodyAsString()
    }
  }

  private[this] val httpsProtocolName = "https"
  
  private def restoringOldHttpsProtocol[T](trustAllSslCerts: Boolean)(f: => T): T = {
    synchronized {
      val oldProtocol = Protocol.getProtocol(httpsProtocolName)

      if (trustAllSslCerts) {
        Protocol.registerProtocol(httpsProtocolName, new Protocol(httpsProtocolName, new EasySSLProtocolSocketFactory, 443))
      }

      try {
        f
      } finally {
        if (trustAllSslCerts) {
          Protocol.registerProtocol(httpsProtocolName, oldProtocol)
        }
      }
    }
  }

  /*
	 * $HeadURL$
	 * $Revision$
	 * $Date$
	 * 
	 * ====================================================================
	 *
	 *  Licensed to the Apache Software Foundation (ASF) under one or more
	 *  contributor license agreements.  See the NOTICE file distributed with
	 *  this work for additional information regarding copyright ownership.
	 *  The ASF licenses this file to You under the Apache License, Version 2.0
	 *  (the "License") you may not use this file except in compliance with
	 *  the License.  You may obtain a copy of the License at
	 *
	 *      http://www.apache.org/licenses/LICENSE-2.0
	 *
	 *  Unless required by applicable law or agreed to in writing, software
	 *  distributed under the License is distributed on an "AS IS" BASIS,
	 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
	 *  See the License for the specific language governing permissions and
	 *  limitations under the License.
	 * ====================================================================
	 *
	 * This software consists of voluntary contributions made by many
	 * individuals on behalf of the Apache Software Foundation.  For more
	 * information on the Apache Software Foundation, please see
	 * <http://www.apache.org/>.
	 *
	 */
  /**
   * <p>
   * EasySSLProtocolSocketFactory can be used to creats SSL {@link Socket}s
   * that accept self-signed certificates.
   * </p>
   * <p>
   * This socket factory SHOULD NOT be used for productive systems
   * due to security reasons, unless it is a concious decision and
   * you are perfectly aware of security implications of accepting
   * self-signed certificates
   * </p>
   *
   * <p>
   * Example of using custom protocol socket factory for a specific host:
   *     <pre>
   *     Protocol easyhttps = new Protocol("https", new EasySSLProtocolSocketFactory(), 443)
   *
   *     URI uri = new URI("https://localhost/", true)
   *     // use relative url only
   *     GetMethod httpget = new GetMethod(uri.getPathQuery())
   *     HostConfiguration hc = new HostConfiguration()
   *     hc.setHost(uri.getHost(), uri.getPort(), easyhttps)
   *     HttpClient client = new HttpClient()
   *     client.executeMethod(hc, httpget)
   *     </pre>
   * </p>
   * <p>
   * Example of using custom protocol socket factory per default instead of the standard one:
   *     <pre>
   *     Protocol easyhttps = new Protocol("https", new EasySSLProtocolSocketFactory(), 443)
   *     Protocol.registerProtocol("https", easyhttps)
   *
   *     HttpClient client = new HttpClient()
   *     GetMethod httpget = new GetMethod("https://localhost/")
   *     client.executeMethod(httpget)
   *     </pre>
   * </p>
   *
   * @author <a href="mailto:oleg -at- ural.ru">Oleg Kalnichevski</a>
   *
   * <p>
   * DISCLAIMER: HttpClient developers DO NOT actively support this component.
   * The component is provided as a reference material, which may be inappropriate
   * for use without additional customization.
   * </p>
   */
  private final object EasySSLProtocolSocketFactory {
    /** Log object for this class. */
    private val LOG = Logger.getLogger(classOf[EasySSLProtocolSocketFactory])
  }

  private final class EasySSLProtocolSocketFactory extends SecureProtocolSocketFactory {
    import EasySSLProtocolSocketFactory._

    private lazy val sslContext: SSLContext = {
      try {
        val context = SSLContext.getInstance("SSL")

        context.init(
          null,
          Array[TrustManager](EasyX509TrustManager),
          null)

        context
      } catch {
        case e: Exception => {
          LOG.error(e.getMessage(), e)

          throw new HttpClientError(e.toString)
        }
      }
    }

    /**
     * @see SecureProtocolSocketFactory#createSocket(java.lang.String,int,java.net.InetAddress,int)
     */
    override def createSocket(host: String, port: Int, clientHost: InetAddress, clientPort: Int): Socket = {
      sslContext.getSocketFactory.createSocket(host, port, clientHost, clientPort)
    }

    /**
     * Attempts to get a new socket connection to the given host within the given time limit.
     * <p>
     * To circumvent the limitations of older JREs that do not support connect timeout a
     * controller thread is executed. The controller thread attempts to create a new socket
     * within the given limit of time. If socket constructor does not return until the
     * timeout expires, the controller terminates and throws an {@link ConnectTimeoutException}
     * </p>
     *
     * @param host the host name/IP
     * @param port the port on the host
     * @param clientHost the local host name/IP to bind the socket to
     * @param clientPort the port on the local machine
     * @param params {@link HttpConnectionParams Http connection parameters}
     *
     * @return Socket a new socket
     *
     * @throws IOException if an I/O error occurs while creating the socket
     * @throws UnknownHostException if the IP address of the host cannot be
     * determined
     */
    override def createSocket(host: String, port: Int, localAddress: InetAddress, localPort: Int, params: HttpConnectionParams): Socket = {
      if (params == null) {
        throw new IllegalArgumentException("Parameters may not be null")
      }

      val timeout = params.getConnectionTimeout

      val socketFactory = sslContext.getSocketFactory

      if (timeout == 0) {
        socketFactory.createSocket(host, port, localAddress, localPort)
      } else {
        val socket = socketFactory.createSocket()

        val localaddr: SocketAddress = new InetSocketAddress(localAddress, localPort)

        val remoteaddr: SocketAddress = new InetSocketAddress(host, port)

        socket.bind(localaddr)

        socket.connect(remoteaddr, timeout)

        socket
      }
    }

    /**
     * @see SecureProtocolSocketFactory#createSocket(java.lang.String,int)
     */
    override def createSocket(host: String, port: Int): Socket = sslContext.getSocketFactory.createSocket(host, port)

    /**
     * @see SecureProtocolSocketFactory#createSocket(java.net.Socket,java.lang.String,int,boolean)
     */
    override def createSocket(socket: Socket, host: String, port: Int, autoClose: Boolean): Socket = {
      sslContext.getSocketFactory.createSocket(socket, host, port, autoClose)
    }

    override def equals(obj: Any): Boolean = {
      ((obj != null) && obj.getClass.equals(classOf[EasySSLProtocolSocketFactory]))
    }

    override def hashCode: Int = classOf[EasySSLProtocolSocketFactory].hashCode
  }

  /*
 * ====================================================================
 *
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License") you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 */
  /**
   * <p>
   * EasyX509TrustManager unlike default {@link X509TrustManager} accepts
   * self-signed certificates.
   * </p>
   * <p>
   * This trust manager SHOULD NOT be used for productive systems
   * due to security reasons, unless it is a concious decision and
   * you are perfectly aware of security implications of accepting
   * self-signed certificates
   * </p>
   *
   * @author <a href="mailto:adrian.sutton@ephox.com">Adrian Sutton</a>
   * @author <a href="mailto:oleg@ural.ru">Oleg Kalnichevski</a>
   *
   * <p>
   * DISCLAIMER: HttpClient developers DO NOT actively support this component.
   * The component is provided as a reference material, which may be inappropriate
   * for use without additional customization.
   * </p>
   */
  private object EasyX509TrustManager extends X509TrustManager {

    private lazy val standardTrustManager: X509TrustManager = {
      val factory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm)

      factory.init(null.asInstanceOf[KeyStore])

      val trustmanagers = factory.getTrustManagers

      if (trustmanagers.length == 0) {
        throw new NoSuchAlgorithmException("no trust manager found")
      }

      trustmanagers.head.asInstanceOf[X509TrustManager]
    }

    /**
     * @see javax.net.ssl.X509TrustManager#checkClientTrusted(X509Certificate[],String authType)
     */
    override def checkClientTrusted(certificates: Array[X509Certificate], authType: String) {
      //standardTrustManager.checkClientTrusted(certificates, authType)
    }

    /**
     * @see javax.net.ssl.X509TrustManager#checkServerTrusted(X509Certificate[],String authType)
     */
    override def checkServerTrusted(certificates: Array[X509Certificate], authType: String) {
      /*if ((certificates != null) && (certificates.length == 1)) {
        certificates.head.checkValidity()
      } else {
        standardTrustManager.checkServerTrusted(certificates, authType)
      }*/
    }

    /**
     * @see javax.net.ssl.X509TrustManager#getAcceptedIssuers()
     */
    def getAcceptedIssuers: Array[X509Certificate] = standardTrustManager.getAcceptedIssuers
  }
}
