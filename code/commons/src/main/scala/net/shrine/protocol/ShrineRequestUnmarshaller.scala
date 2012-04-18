package net.shrine.protocol

import xml.NodeSeq
import net.shrine.serialization.XmlUnmarshaller

/**
 * @author Bill Simons
 * @date 3/30/11
 * @link http://cbmi.med.harvard.edu
 * @link http://chip.org
 *       <p/>
 *       NOTICE: This software comes with NO guarantees whatsoever and is
 *       licensed as Lgpl Open Source
 * @link http://www.gnu.org/licenses/lgpl.html
 */
trait ShrineRequestUnmarshaller[T] extends XmlUnmarshaller[T] {
  def shrineProjectId(nodeSeq: NodeSeq): String = (nodeSeq \ "projectId").text

  def shrineWaitTimeMs(nodeSeq: NodeSeq): Long = (nodeSeq \ "waitTimeMs").text.toLong

  def shrineAuthenticationInfo(nodeSeq: NodeSeq): AuthenticationInfo = AuthenticationInfo.fromXml(nodeSeq \ "authenticationInfo")
}
