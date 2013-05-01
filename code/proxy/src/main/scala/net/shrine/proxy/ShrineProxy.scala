package net.shrine.proxy

import org.spin.tools.config.ConfigTool
import java.io.BufferedReader
import java.io.File
import java.io.FileReader
import java.io.IOException
import java.util.ArrayList
import java.util.List
import scala.xml.XML
import scala.xml.NodeSeq
import net.shrine.util.JerseyHttpClient
import net.shrine.util.Loggable

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

object DefaultShrineProxy extends Loggable {

  private[proxy] def loadWhiteList: Set[String] = loadList("whitelist")
  
  private[proxy] def loadBlackList: Set[String] = loadList("blacklist")
  
  private def loadList(listname: String): Set[String] = {
    try {
      val confFile = ConfigTool.getConfigFileWithFailover("shrine-proxy-acl.xml")

      val confXml = XML.loadFile(confFile)

      (confXml \\ "lists" \ listname \ "host").map(_.text.trim).toSet
    } catch {
      case e: Exception => {
        error("ShrineProxy encountered a problem while checking ACL permissions: " + e)

        throw new ShrineProxyException(e.getStackTrace.toString, e)
      }
    }
  }
  
  object JerseyHttpClientUrlPoster extends ShrineProxy.UrlPoster {
    private val httpClient = new JerseyHttpClient(acceptAllSslCerts = true)
    
    override def post(url: String, input: String): String = httpClient.post(input, url)
  }
}

final class DefaultShrineProxy(val whiteList: Set[String], val blackList: Set[String], val urlPoster: ShrineProxy.UrlPoster ) extends ShrineProxy with Loggable {

  def this() = this(DefaultShrineProxy.loadWhiteList, DefaultShrineProxy.loadBlackList, DefaultShrineProxy.JerseyHttpClientUrlPoster)

  import DefaultShrineProxy._
  
  whiteList.foreach(entry => info(s"Whitelist entry: $entry"))
  whiteList.foreach(entry => info(s"Blacklist entry: $entry"))

  info("Loaded access control lists.")

  override def isAllowableUrl(redirectURL: String): Boolean = whiteList.exists(redirectURL.startsWith) && !blackList.exists(redirectURL.startsWith)

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
      error("ShrineAdapter detected missing redirect_url tag")

      throw new ShrineMessageFormatException("ShrineAdapter detected missing redirect_url tag")
    }

    //if redirectURL is not in the white list, do not proceed.
    if (!isAllowableUrl(redirectUrl)) {
      throw new ShrineMessageFormatException("redirectURL not in white list or is in black list: " + redirectUrl)
    }

    debug(s"Proxy redirecting to $redirectUrl")

    urlPoster.post(redirectUrl, request.toString)
  }
}
