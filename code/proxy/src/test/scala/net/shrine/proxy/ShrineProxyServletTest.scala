package net.shrine.proxy

import org.scalatest.junit.AssertionsForJUnit
import org.scalatest.matchers.ShouldMatchers
import junit.framework.TestCase
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import scala.xml.NodeSeq
import java.io.BufferedReader
import java.io.StringReader
import javax.servlet.http.Cookie
import java.io.PrintWriter
import java.io.StringWriter
import java.util.Locale
import javax.servlet.ServletException
import scala.xml.XML
import net.shrine.util.XmlUtil
import scala.xml.Node

/**
 * @author clint
 * @date Jun 25, 2012
 */
final class ShrineProxyServletTest extends TestCase with ShouldMatchers with AssertionsForJUnit {
  def testDefaultConstructor {
    val servlet = new ShrineProxyServlet
    
    servlet.proxy.isInstanceOf[DefaultShrineProxy] should be(true)
    
    val proxy = servlet.proxy.asInstanceOf[DefaultShrineProxy]
    
    proxy.urlPoster should be(DefaultShrineProxy.HttpClientUrlPoster)
    //whitelist values from shrine-proxy-acl.xml on this module's classpath
    proxy.whiteList should be(Set("http://127.0.0.1:7070/axis2/rest/", "http://localhost:7070/axis2/rest/", "http://webservices.i2b2.org/", "https://", "http://"))
    
    intercept[IllegalArgumentException] {
      new ShrineProxyServlet(null)
    }
  }
  
  private final class AlwaysFailsMockShrineProxy extends ShrineProxy {
    def isAllowableUrl(redirectURL: String): Boolean = false
  
    def redirect(request: NodeSeq): String = sys.error("foo")
  }
  
  def testDoPost {
    val whiteList = Set("http://example.com")
    
    //Should work 
    
    Seq("http://example.com", "http://example.com/foo", "http://example.com/lots/of/stuff").foreach { url =>

      val mockUrlPoster = new DefaultShrineProxyTest.MockUrlPoster
    
      val servlet = new ShrineProxyServlet(new DefaultShrineProxy(whiteList, mockUrlPoster))
      
      val mockRequest = new MockHttpServletRequest(url)
    
      val mockResponse = new MockHttpServletResponse
    
      servlet.doPost(mockRequest, mockResponse)
    
      mockResponse.buffer.toString should equal("OK")
      
      mockUrlPoster.url should equal(url)

      //NB: This hoop-jumping is necessary because xml-marchalling round-trips can produce
      //xml with semantically identical (but literally different) namespace declaration orders. :( 
      
      val actual = XML.loadString(mockUrlPoster.input).child.map((n: Node) => XmlUtil.stripNamespaces(n)).toSet.toString

      val expected = XML.loadString(mockRequest.reqXml).child.map((n: Node) => XmlUtil.stripNamespaces(n)).toSet.toString
      
      actual should equal(expected)
    }
    
    //Should fail
    Seq("http://google.com", null, "", "  ").foreach { url =>
      val mockUrlPoster = new DefaultShrineProxyTest.MockUrlPoster
    
      val servlet = new ShrineProxyServlet(new DefaultShrineProxy(whiteList, mockUrlPoster))
      
      val mockRequest = new MockHttpServletRequest(url)
    
      val mockResponse = new MockHttpServletResponse
    
      intercept[ServletException] {
        servlet.doPost(mockRequest, mockResponse)
      }
    }
    
    intercept[ServletException] {
      val mockRequest = new MockHttpServletRequest("http://example.com")
    
      val mockResponse = new MockHttpServletResponse
      
      new ShrineProxyServlet(new AlwaysFailsMockShrineProxy).doPost(mockRequest, mockResponse)
    }
  }
  
  //Ugh :(
  private final class MockHttpServletRequest(redirectUrl: String) extends HttpServletRequest {
    val reqXml = DefaultShrineProxyTest.getQuery(redirectUrl).toString
    
    def getAuthType = ""

    def getCookies = null

    def getDateHeader(name: String) = 0L

    def getHeader(name: String) = ""

    def getHeaders(name: String) = null

    def getHeaderNames() = null

    def getIntHeader(name: String) = 0

    def getMethod() = ""

    def getPathInfo() = ""

    def getPathTranslated() = ""

    def getContextPath() = ""

    def getQueryString() = ""

    def getRemoteUser() = ""

    def isUserInRole(role: String) = true

    def getUserPrincipal() = null

    def getRequestedSessionId() = ""

    def getRequestURI() = ""

    def getRequestURL() = null

    def getServletPath() = ""

    def getSession(create: Boolean) = null

    def getSession() = null

    def isRequestedSessionIdValid() = true

    def isRequestedSessionIdFromCookie() = true

    def isRequestedSessionIdFromURL() = false

    def isRequestedSessionIdFromUrl() = false
    
    def getAttribute(name: String) = null

    def getAttributeNames() = null

    def getCharacterEncoding() = null

    def setCharacterEncoding(env: String) = ()

    def getContentLength() = reqXml.size

    def getContentType() = ""

    def getInputStream() = null

    def getParameter(name: String) = ""

    def getParameterNames() = null

    def getParameterValues(name: String) = null

    def getParameterMap() = null

    def getProtocol() = ""

    def getScheme() = ""

    def getServerName() = ""

    def getServerPort() = 80

    def getReader() = new BufferedReader(new StringReader(reqXml))

    def getRemoteAddr() = ""

    def getRemoteHost() = ""

    def setAttribute(name: String, o: AnyRef) = ()

    def removeAttribute(name: String) = ()

    def getLocale() = null

    def getLocales() = null

    def isSecure() = false

    def getRequestDispatcher(path: String) = null

    def getRealPath(path: String) = ""

    def getRemotePort() = 12345

    def getLocalName() = ""

    def getLocalAddr() = ""

    def getLocalPort() = 12345
  }
  
  //Ugh :(
  private final class MockHttpServletResponse extends HttpServletResponse {
    val buffer = new StringWriter
    
    val writer = new PrintWriter(buffer)
    
    def addCookie(cookie: Cookie) = null

    def containsHeader(name: String) = false

    def encodeURL(url: String) = ""

    def encodeRedirectURL(url: String) = ""

    def encodeUrl(url: String) = ""

    def encodeRedirectUrl(url: String) = ""

    def sendError(sc: Int, msg: String) = ()

    def sendError(sc: Int) = ()

    def sendRedirect(location: String) = ()

    def setDateHeader(name: String, date: Long) = ()

    def addDateHeader(name: String, date: Long) = ()

    def setHeader(name: String, value: String) = ()

    def addHeader(name: String, value: String) = ()

    def setIntHeader(name: String, value: Int) = ()

    def addIntHeader(name: String, value: Int) = ()

    def setStatus(sc: Int) = ()

    def setStatus(sc: Int, sm: String) = ()
    
    def getCharacterEncoding() = ""

    def getContentType() = ""

    def getOutputStream() = null

    def getWriter() = writer

    def setCharacterEncoding(charset: String) = ()

    def setContentLength(len: Int) = ()

    def setContentType(t: String) = ()

    def setBufferSize(size: Int) = ()

    def getBufferSize() = 0

    def flushBuffer() = ()

    def resetBuffer() = ()

    def isCommitted() = true

    def reset() = ()

    def setLocale(loc: Locale) = ()

    def getLocale() = null
  }
}