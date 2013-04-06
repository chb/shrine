package net.shrine.protocol

import xml.NodeSeq
import net.shrine.util.XmlUtil
import net.shrine.serialization.XmlUnmarshaller
import net.shrine.serialization.I2b2Unmarshaller

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

  override def toXml = XmlUtil.stripWhitespace {
    <errorResponse>
      <message>{errorMessage}</message>
    </errorResponse>
  }
}

object ErrorResponse extends XmlUnmarshaller[ErrorResponse] with I2b2Unmarshaller[ErrorResponse] {
  override def fromXml(xml: NodeSeq): ErrorResponse = {
    val messageXml = (xml \ "message")
    
    //NB: Fail fast
    require(messageXml.nonEmpty)
    
    ErrorResponse(messageXml.text)
  }
  
  override def fromI2b2(xml: NodeSeq): ErrorResponse = {
    val statusXml = xml \ "response_header" \ "result_status" \ "status"
    
    //NB: Fail fast
    require((statusXml \ "@type").text == "ERROR")
    
    ErrorResponse((xml \ "response_header" \ "result_status" \ "status").text)
  }
}