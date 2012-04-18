package net.shrine.serialization

import xml.{XML, NodeSeq}
import net.shrine.protocol.AuthenticationInfo


/**
 * @author Bill Simons
 * @date 3/23/11
 * @link http://cbmi.med.harvard.edu
 * @link http://chip.org
 *       <p/>
 *       NOTICE: This software comes with NO guarantees whatsoever and is
 *       licensed as Lgpl Open Source
 * @link http://www.gnu.org/licenses/lgpl.html
 */
trait I2b2Unmarshaller[T] {

  def fromI2b2(xmlString: String): T = fromI2b2(XML.loadString(xmlString))

  def fromI2b2(nodeSeq: NodeSeq): T

  def i2b2ProjectId(nodeSeq: NodeSeq): String = (nodeSeq \ "message_header" \ "project_id").text

  def i2b2WaitTimeMs(nodeSeq: NodeSeq): Long = (nodeSeq \ "request_header" \ "result_waittime_ms").text.toLong

  def i2b2AuthenticationInfo(nodeSeq: NodeSeq): AuthenticationInfo = AuthenticationInfo.fromI2b2(nodeSeq \ "message_header" \ "security")
}