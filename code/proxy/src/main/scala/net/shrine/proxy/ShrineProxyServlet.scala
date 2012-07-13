package net.shrine.proxy

import org.apache.log4j.Logger
import org.spin.tools.config.ConfigException
import javax.servlet.ServletConfig
import javax.servlet.ServletException
import javax.servlet.http.HttpServlet
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import java.io.IOException
import java.io.OutputStreamWriter
import scala.xml.XML
import org.spin.tools.crypto.PKITool

/**
 * @author Andrew McMurry
 * @author Clint Gilbert
 * ----------------------------------------------------------
 * [ All net.shrine.* code is available per the I2B2 license]
 * @link https://www.i2b2.org/software/i2b2_license.html
 * ----------------------------------------------------------
 */
object ShrineProxyServlet {
  private val logger = Logger.getLogger(classOf[ShrineProxyServlet])
}

final class ShrineProxyServlet(val proxy: ShrineProxy) extends HttpServlet {

  require(proxy != null)
  
  def this() = this(try {
    ShrineProxyServlet.logger.info("Starting ProxyServlet")

    new DefaultShrineProxy
  } catch {
    case e: ConfigException => {
      ShrineProxyServlet.logger.error("ProxyServlet error: " + e)

      throw new ServletException(e)
    }
  })

  import ShrineProxyServlet._

  override def doGet(req: HttpServletRequest, resp: HttpServletResponse) = doPost(req, resp)

  override def doPost(request: HttpServletRequest, response: HttpServletResponse) {
    response.setContentType("text/xml")

    val out = response.getWriter

    // Just forward the request

    try {
      val messageXml = XML.load(request.getReader)

      val responseFromProxiedUrl = proxy.redirect(messageXml)

      out.write(responseFromProxiedUrl)
    } catch {
      case e: Exception => {
        logger.error("ProxyServlet error:", e)

        throw new ServletException(e)
      }
    } finally {
      out.flush()
      out.close()
    }
  }
}

