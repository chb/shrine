package net.shrine.i2b2.protocol.pm

import net.shrine.serialization.I2b2Unmarshaller
import xml.NodeSeq
import net.shrine.protocol.Credential
import net.shrine.protocol.AuthenticationInfo

/**
 * @author Bill Simons
 * @date 3/6/12
 * @link http://cbmi.med.harvard.edu
 * @link http://chip.org
 *       <p/>
 *       NOTICE: This software comes with NO guarantees whatsoever and is
 *       licensed as Lgpl Open Source
 * @link http://www.gnu.org/licenses/lgpl.html
 */
final class User(val fullName: String, val username: String, val domain: String, val credential: Credential, val params: Map[String, String]) {
  def toAuthInfo = AuthenticationInfo(domain, username, credential)
}

object User extends I2b2Unmarshaller[User] {
  def fromI2b2(nodeSeq: NodeSeq) = {
    val params = Map.empty ++ (nodeSeq \ "message_body" \ "configure" \ "user" \ "param").map { param =>
      ((param \ "@name").text, param.text)
    }

    val userXml = nodeSeq \ "message_body" \ "configure" \ "user"
    
    new User(
      (userXml \ "full_name").text,
      (userXml \ "user_name").text,
      (userXml \ "domain").text,
      Credential.fromI2b2(userXml \ "password"),
      params)
  }
}