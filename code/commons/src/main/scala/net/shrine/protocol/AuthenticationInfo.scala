package net.shrine.protocol

import xml.NodeSeq
import net.shrine.util.XmlUtil
import net.shrine.serialization.{I2b2Marshaller, I2b2Unmarshaller, XmlMarshaller, XmlUnmarshaller}

/**
 * @author Bill Simons
 * @date 3/9/11
 * @link http://cbmi.med.harvard.edu
 * @link http://chip.org
 *       <p/>
 *       NOTICE: This software comes with NO guarantees whatsoever and is
 *       licensed as Lgpl Open Source
 * @link http://www.gnu.org/licenses/lgpl.html
 * 
 * NB: this is a case class to get a structural equality contract in hashCode and equals, mostly for testing
 * 
 * NB: Exposes a constructor that takes a String, so that JAXRS can automatically unmarshal an instance of this
 * class from a String
 */
final case class AuthenticationInfo(
    val domain: String,
    val username: String,
    val credential: Credential) extends XmlMarshaller with I2b2Marshaller {

  //NB: For JAXRS 
  private def this(other: AuthenticationInfo) = this(other.domain, other.username, other.credential)
  
  //NB: For JAXRS
  def this(serializedForm: String) = this(AuthenticationInfo.fromHeader(serializedForm))
  
  def toXml = XmlUtil.stripWhitespace(
    <authenticationInfo>
      <domain>{domain}</domain>
      <username>{username}</username>
      {credential.toXml}
    </authenticationInfo>)

  def toI2b2 = XmlUtil.stripWhitespace(
    <security>
      <domain>{domain}</domain>
      <username>{username}</username>
      {credential.toI2b2}
    </security>)
    
  def toHeader = {
    import AuthenticationInfo.{headerPrefix => prefix, headerDelimiter => delim}

    prefix + Seq(domain, username, credential.value, credential.isToken).mkString(delim)
  }
}

object AuthenticationInfo extends I2b2Unmarshaller[AuthenticationInfo] with XmlUnmarshaller[AuthenticationInfo] {

  def fromI2b2(nodeSeq: NodeSeq): AuthenticationInfo = {
    new AuthenticationInfo((nodeSeq \ "domain").text, (nodeSeq \ "username").text, Credential.fromI2b2(nodeSeq \ "password"))
  }

  def fromXml(nodeSeq: NodeSeq) = {
    new AuthenticationInfo((nodeSeq \ "domain").text, (nodeSeq \ "username").text, Credential.fromXml(nodeSeq \ "credential"))
  }
  
  private[protocol] val headerPrefix = "SHRINE "
    
  private[protocol] val headerDelimiter = ","
  
  def fromHeader(header: String) = {
    val Array(_, headerValue) = header.split(headerPrefix)

    val Array(domain, username, password, isTokenStr) = headerValue.split(headerDelimiter)
  
    new AuthenticationInfo(domain, username, new Credential(password, isTokenStr.toBoolean))
  }
}