package net.shrine.protocol

import xml.{NodeSeq, Utility}
import net.shrine.util.XmlUtil

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
final case class ErrorResponse(val errorMessage: String) extends ShrineResponse with TranslatableResponse[ErrorResponse] {

  override protected def status = <status type="ERROR">{errorMessage}</status>

  protected def i2b2MessageBody = null

  def toXml = XmlUtil.stripWhitespace(
    <errorResponse>
      <message>{errorMessage}</message>
    </errorResponse>)
}

object ErrorResponse extends XmlUnmarshaller[ErrorResponse] {
  def fromXml(nodeSeq: NodeSeq) = new ErrorResponse((nodeSeq \ "message").text)
}