package net.shrine.protocol

import xml.Utility
import scala.xml.NodeSeq
import net.shrine.util.XmlUtil

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
final case class ReadApprovedQueryTopicsResponse(val approvedTopics: Seq[ApprovedTopic]) extends ShrineResponse {
  protected def i2b2MessageBody = XmlUtil.stripWhitespace(
    <ns7:sheriff_response xsi:type="ns7:sheriffResponseType" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
      {
        approvedTopics map {_.toI2b2}
      }
    </ns7:sheriff_response>
  )

  def toXml = XmlUtil.stripWhitespace(
    <readApprovedQueryTopicsResponse>
      <approvedTopics>
      {
        approvedTopics map {_.toXml}
      }
      </approvedTopics>
    </readApprovedQueryTopicsResponse>)
}

object ReadApprovedQueryTopicsResponse extends XmlUnmarshaller[ReadApprovedQueryTopicsResponse] {
  def fromXml(xml: NodeSeq): ReadApprovedQueryTopicsResponse = {
    val approvedTopics = (xml \\ "approvedTopic").map(ApprovedTopic.fromXml)
    
    new ReadApprovedQueryTopicsResponse(approvedTopics)
  }
}