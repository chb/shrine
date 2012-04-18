package net.shrine.serialization

import xml.{XML, NodeSeq}

/**
 * @author Bill Simons
 * @date 4/5/11
 * @link http://cbmi.med.harvard.edu
 * @link http://chip.org
 *       <p/>
 *       NOTICE: This software comes with NO guarantees whatsoever and is
 *       licensed as Lgpl Open Source
 * @link http://www.gnu.org/licenses/lgpl.html
 */
trait XmlUnmarshaller[T] {
  def fromXml(nodeSeq: NodeSeq): T

  def fromXml(xmlString: String): T = fromXml(XML.loadString(xmlString))
}