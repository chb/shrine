package net.shrine.i2b2.protocol.pm

import net.shrine.serialization.I2b2Unmarshaller
import xml.NodeSeq

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
class User(val fullName: String, val username: String, val domain: String, val params: Map[String, String])

object User extends I2b2Unmarshaller[User] {
  def fromI2b2(nodeSeq: NodeSeq) = {
    val params = (nodeSeq \ "message_body" \ "configure" \ "user" \ "param") map {param =>
      ((param \ "@name").text, param.text)
    }

    new User(
      (nodeSeq \ "message_body" \ "configure" \ "user" \ "full_name").text,
      (nodeSeq \ "message_body" \ "configure" \ "user" \ "user_name").text,
      (nodeSeq \ "message_body" \ "configure" \ "user" \ "domain").text,
      params.toMap)
  }
}