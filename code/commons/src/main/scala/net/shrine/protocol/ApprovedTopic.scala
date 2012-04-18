package net.shrine.protocol

import scala.xml.NodeSeq
import net.shrine.util.XmlUtil
import net.shrine.serialization.{I2b2Marshaller, XmlMarshaller}


/**
 * @author Bill Simons
 * @date 8/29/11
 * @link http://cbmi.med.harvard.edu
 * @link http://chip.org
 *       <p/>
 *       NOTICE: This software comes with NO guarantees whatsoever and is
 *       licensed as Lgpl Open Source
 * @link http://www.gnu.org/licenses/lgpl.html
 * 
 * NB: this is a case class to get a structural equality contract in hashCode and equals, mostly for testing
 */
final case class ApprovedTopic(val queryTopicId: Long, val queryTopicName: String) extends XmlMarshaller with I2b2Marshaller {
  def toI2b2 = XmlUtil.stripWhitespace(
    <sheriffEntry>
      <approval>Approved</approval>
      <queryName>{queryTopicName}</queryName>
      <queryTopicID>{queryTopicId}</queryTopicID>
    </sheriffEntry>
  )

  def toXml = XmlUtil.stripWhitespace(
    <approvedTopic>
      <queryTopicId>{queryTopicId}</queryTopicId>
      <queryTopicName>{queryTopicName}</queryTopicName>
    </approvedTopic>
  )
}

object ApprovedTopic {
  def fromXml(xml: NodeSeq): ApprovedTopic = deserialize(xml, "queryTopicId", "queryTopicName")
  
  private def deserialize(xml: NodeSeq, idTagName: String, nameTagName: String): ApprovedTopic = {
    val queryTopicId = (xml \ idTagName).text.trim.toLong
    val queryTopicName = (xml \ nameTagName).text.trim
    
    new ApprovedTopic(queryTopicId, queryTopicName)
  }
}