package net.shrine.protocol

import org.scalatest.junit.{AssertionsForJUnit, ShouldMatchersForJUnit}
import org.junit.Test
import org.junit.Assert.assertNotSame

/**
 * @author Bill Simons
 * @date 3/11/11
 * @link http://cbmi.med.harvard.edu
 * @link http://chip.org
 *       <p/>
 *       NOTICE: This software comes with NO guarantees whatsoever and is
 *       licensed as Lgpl Open Source
 * @link http://www.gnu.org/licenses/lgpl.html
 */
class AuthenticationInfoTest extends AssertionsForJUnit with ShouldMatchersForJUnit with XmlSerializableValidator with I2b2SerializableValidator {
  @Test
  def testStringConstructor {
    val auth = new AuthenticationInfo("xyz-domain", "xyz-user", new Credential("asjkdhkasjdfh", true))
    
    val copy = new AuthenticationInfo(auth.toHeader)
    
    assertNotSame(auth, copy)
    auth should equal(copy) 
  }
  
  @Test
  def testFromI2b2() {
    val passwd = "SessionKey:prFsw9A1zZTr2PZpFLh1"
    val domain = "test_domain"
    val username = "test_username"
    val auth = AuthenticationInfo.fromI2b2(
      <security>
        <domain>{domain}</domain>
        <username>{username}</username>
        <password token_ms_timeout="1800000" is_token="true">{passwd}</password>
      </security>)

    auth.domain should equal(domain)
    auth.username should equal(username)
    auth.credential.value should equal(passwd)
    auth.credential.isToken should equal(true)
  }

  @Test
  def testToI2b2() {
    val credential = new Credential("value", false)
    val domain = "domain"
    val username = "username"
    val authn = new AuthenticationInfo(domain, username, credential)
    authn.toI2b2 should equal(<security><domain>{domain}</domain><username>{username}</username>{credential.toI2b2}</security>)
  }

  @Test
  def testToXml() {
    val credential = new Credential("value", false)
    val authn = new AuthenticationInfo("domain", "username", credential)
    authn.toXml should equal(<authenticationInfo><domain>domain</domain><username>username</username>{credential.toXml}</authenticationInfo>)
  }
  
  @Test
  def testToHeader() {
    val passwd = "SessionKey:prFsw9A1zZTr2PZpFLh1"
    val domain = "test_domain"
    val username = "test_username"
    val credential = new Credential(passwd, false)
    
    import AuthenticationInfo.{headerDelimiter => delim}
    import AuthenticationInfo.{headerPrefix => prefix}
    
    val headerString = prefix + domain + delim + username + delim + credential.value + delim + credential.isToken
    
    val authn = new AuthenticationInfo(domain, username, credential)
    
    authn.toHeader should equal(headerString)
        
    val roundTripped = AuthenticationInfo.fromHeader(authn.toHeader)
    
    authn.domain should equal(roundTripped.domain)
    authn.username should equal(roundTripped.username)
    authn.credential should equal(roundTripped.credential)
  }

  @Test
  def testFromXml() {
    val credential = new Credential("value", false)
    val authn = AuthenticationInfo.fromXml(<authenticationInfo><domain>domain</domain><username>username</username>{credential.toXml}</authenticationInfo>)
    authn.domain should equal("domain")
    authn.username should equal("username")
    authn.credential should equal(credential)
  }
  
  @Test
  def testFromHeader() {
    val passwd = "SessionKey:prFsw9A1zZTr2PZpFLh1"
    val domain = "test_domain"
    val username = "test_username"
    val credential = new Credential(passwd, false)
    
    import AuthenticationInfo.{headerDelimiter => delim}
    import AuthenticationInfo.{headerPrefix => prefix}
    
    val headerString = prefix + domain + delim + username + delim + credential.value + delim + credential.isToken
    
    val authn = AuthenticationInfo.fromHeader(headerString)

    authn should not be null
    authn.domain should equal(domain)
    authn.username should equal(username)
    authn.credential should equal(credential)
  }
}