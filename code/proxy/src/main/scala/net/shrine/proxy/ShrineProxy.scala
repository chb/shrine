package net.shrine.proxy

import net.shrine.util.HTTPClient
import org.apache.log4j.Logger
import org.spin.tools.config.ConfigException
import org.spin.tools.config.ConfigTool
import java.io.BufferedReader
import java.io.File
import java.io.FileReader
import java.io.IOException
import java.util.ArrayList
import java.util.List
import scala.xml.XML
import scala.xml.NodeSeq

/**
 * [ Author ]
 *
 * @author Clint Gilbert
 * @author Ricardo Delima
 * @author Andrew McMurry
 * @author Britt Fitch
 *         <p/>
 *         Date: Apr 1, 2008
 *         Harvard Medical School Center for BioMedical Informatics
 * @link http://cbmi.med.harvard.edu
 * <p/>
 * NB: In the previous version of this class, the black list had no effect on the result of calling
 * isAllawableDomain (now isAllowableUrl).  This behavior is preserved here; furthermore, I took the 
 * liberty of removing the black list field, to simplify initialization. -Clint
 *
 */
object ShrineProxy {
  trait UrlPoster {
    def post(url: String, input: String): String
  }
}

trait ShrineProxy {
  def isAllowableUrl(redirectURL: String): Boolean
  
  def redirect(request: NodeSeq): String
}

object DefaultShrineProxy {
  private val log = Logger.getLogger(classOf[ShrineProxy])
  
  private val DEBUG = log.isDebugEnabled

  private[proxy] def loadWhiteList: Set[String] = {
    try {
      val confFile = ConfigTool.getConfigFileWithFailover("shrine-proxy-acl.xml")

      val confXml = XML.loadFile(confFile)

      (confXml \\ "lists" \ "whitelist" \ "host").map(_.text.trim).toSet
    } catch {
      case e: Exception => {
        log.error("ShrineProxy encountered a problem while checking ACL permissions: " + e)

        throw new ConfigException(e.getStackTrace.toString)
      }
    }
  }
  
  object HttpClientUrlPoster extends ShrineProxy.UrlPoster {
    override def post(url: String, input: String): String = HTTPClient.post(input, url, trustAllSslCerts = true)
  }
}

final class DefaultShrineProxy(val whiteList: Set[String], val urlPoster: ShrineProxy.UrlPoster ) extends ShrineProxy {

  def this() = this(DefaultShrineProxy.loadWhiteList, DefaultShrineProxy.HttpClientUrlPoster)

  import DefaultShrineProxy._
  
  whiteList.foreach(entry => log.info("Whitelist entry:" + entry))

  log.info("Loaded access control lists.")

  override def isAllowableUrl(redirectURL: String): Boolean = whiteList.exists(redirectURL.startsWith)

  /**
   * Redirect to a URL embedded within the I2B2 message
   *
   * @param request a chunk of xml with a <redirect_url> element, containing the url to redirect to.
   * @return the String result of accessing the url embedded in the passed request xml.
   * @throws ShrineMessageFormatException bad input XML
   */
  override def redirect(request: NodeSeq): String = {
    val redirectUrl = (request \\ "redirect_url").headOption.map(_.text.trim).getOrElse(throw new ShrineMessageFormatException("Error parsing redirect_url tag"))

    if (redirectUrl == null || redirectUrl.isEmpty) {
      log.error("ShrineAdapter detected missing redirect_url tag")

      throw new ShrineMessageFormatException("ShrineAdapter detected missing redirect_url tag")
    }

    //if redirectURL is not in the white list, do not proceed.
    if (!isAllowableUrl(redirectUrl)) {
      throw new ShrineMessageFormatException("redirectURL not in white list or is in black list: " + redirectUrl)
    }

    if (DEBUG) {
      log.debug("Proxy redirecting to " + redirectUrl)
    }

    return urlPoster.post(redirectUrl, request.toString)
  }
}
