package net.shrine.protocol

import xml.NodeSeq
import net.shrine.serialization.{I2b2Marshaller, I2b2Unmarshaller, XmlMarshaller, XmlUnmarshaller}

/**
 * @author Bill Simons
 * @date 3/9/11
 * @link http://cbmi.med.harvard.edu
 * @link http://chip.org
 *       <p/>
 *       NOTICE: This software comes with NO guarantees whatsoever and is
 *       licensed as Lgpl Open Source
 * @link http://www.gnu.org/licenses/lgpl.html
 *
 * NB: this is a case class to get a structural equality contract in hashCode and equals, mostly for testing
 */
final case class Credential(val value: String, val isToken: Boolean) extends XmlMarshaller with I2b2Marshaller {

  def toXml = <credential isToken={isToken.toString}>{value}</credential>

  def toI2b2 = <password token_ms_timeout="1800000" is_token={isToken.toString}>{value}</password>
}

object Credential extends I2b2Unmarshaller[Credential] with XmlUnmarshaller[Credential] {

  def fromI2b2(nodeSeq: NodeSeq): Credential = {
    new Credential(nodeSeq.text, parseI2b2IsToken(nodeSeq))
  }

  private def parseI2b2IsToken(nodeSeq: NodeSeq): Boolean =
    if((nodeSeq \ "@is_token").isEmpty) {
      false
    }
    else {
      (nodeSeq \ "@is_token").text.toBoolean
    }

  def fromXml(nodeSeq: NodeSeq) = {
    new Credential(nodeSeq.text, parseShrineIsToken(nodeSeq))
  }

  private def parseShrineIsToken(nodeSeq: NodeSeq): Boolean =
    if((nodeSeq \ "@isToken").isEmpty) {
      false
    }
    else {
      (nodeSeq \ "@isToken").text.toBoolean
    }
}