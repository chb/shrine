package net.shrine.protocol

import scala.xml.NodeSeq
import net.shrine.util.XmlUtil

/**
 * @author clint
 * @date Aug 15, 2012
 */
trait CrcRequest { self: ShrineRequest =>
  def i2b2PsmHeaderWithDomain: NodeSeq = makeI2b2PsmHeader(Option(authn.domain))
  
  def i2b2PsmHeader: NodeSeq = makeI2b2PsmHeader(None)
  
  private def makeI2b2PsmHeader(domainOption: Option[String] = None): NodeSeq = XmlUtil.stripWhitespace(
    <ns4:psmheader>
      {
        domainOption match {
          case Some(domain) => <user group={ authn.domain } login={ authn.username }>{ authn.username }</user>
          case None => <user login={ authn.username }>{ authn.username }</user>
        }
      }
      <patient_set_limit>0</patient_set_limit>
      <estimated_time>0</estimated_time>
      <request_type>{requestType.i2b2RequestType.get}</request_type>
    </ns4:psmheader>)
}