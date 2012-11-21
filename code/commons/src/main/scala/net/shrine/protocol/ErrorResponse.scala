package net.shrine.protocol

import xml.NodeSeq
import net.shrine.util.XmlUtil
import net.shrine.serialization.XmlUnmarshaller

/**
 * @author Bill Simons
 * @date 4/25/11
 * @link http://cbmi.med.harvard.edu
 * @link http://chip.org
 *       <p/>
 *       NOTICE: This software comes with NO guarantees whatsoever and is
 *       licensed as Lgpl Open Source
 * @link http://www.gnu.org/licenses/lgpl.html
 * 
 * NB: Now a case class for structural equality 
 */
final case class ErrorResponse(val errorMessage: String) extends ShrineResponse {

  override protected def status = <status type="ERROR">{errorMessage}</status>

  override protected def i2b2MessageBody = null

  override def toXml = XmlUtil.stripWhitespace(
    <errorResponse>
      <message>{errorMessage}</message>
    </errorResponse>)
}

object ErrorResponse extends XmlUnmarshaller[ErrorResponse] {
  override def fromXml(nodeSeq: NodeSeq) = new ErrorResponse((nodeSeq \ "message").text)
}