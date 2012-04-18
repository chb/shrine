package net.shrine.i2b2.protocol.pm

import net.shrine.util.XmlUtil
import net.shrine.serialization.{I2b2Unmarshaller, XmlMarshaller}
import xml.NodeSeq

/**
 * @author Bill Simons
 * @date 3/5/12
 * @link http://cbmi.med.harvard.edu
 * @link http://chip.org
 *       <p/>
 *       NOTICE: This software comes with NO guarantees whatsoever and is
 *       licensed as Lgpl Open Source
 * @link http://www.gnu.org/licenses/lgpl.html
 */
class HiveConfig(val crcUrl: String, val ontologyUrl: String) extends XmlMarshaller {
  def toXml = XmlUtil.stripWhitespace(
    <hiveConfig>
        <crcUrl>{crcUrl}</crcUrl>
      <ontUrl>{ontologyUrl}</ontUrl>
    </hiveConfig>)
}

object HiveConfig extends I2b2Unmarshaller[HiveConfig] {
  def fromI2b2(nodeSeq: NodeSeq) = {
    val cellDataSeq = nodeSeq \ "message_body" \ "configure" \ "cell_datas" \ "cell_data"
    //TODO review for error handling - dangerous Option.get?
    val crcUrl = (cellDataSeq.find{a =>(a \\ "@id").text =="CRC"}.get \ "url").text
    val ontUrl = (cellDataSeq.find{a =>(a \\ "@id").text =="ONT"}.get \ "url").text
    new HiveConfig(crcUrl, ontUrl)
  }
}