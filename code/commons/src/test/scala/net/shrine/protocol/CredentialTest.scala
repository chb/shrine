package net.shrine.protocol

import org.scalatest.junit.{AssertionsForJUnit, ShouldMatchersForJUnit}
import org.junit.Test

/**
 * @author Bill Simons
 * @date 3/10/11
 * @link http://cbmi.med.harvard.edu
 * @link http://chip.org
 *       <p/>
 *       NOTICE: This software comes with NO guarantees whatsoever and is
 *       licensed as Lgpl Open Source
 * @link http://www.gnu.org/licenses/lgpl.html
 */
class CredentialTest extends AssertionsForJUnit with ShouldMatchersForJUnit with XmlSerializableValidator with I2b2SerializableValidator {

  @Test
  def testFromI2b2() {
    val passwd = "SessionKey:prFsw9A1zZTr2PZpFLh1"
    var cred = Credential.fromI2b2(<password token_ms_timeout="1800000" is_token="true">{passwd}</password>)
    cred.value should equal(passwd)
    cred.isToken should equal(true)

    cred = Credential.fromI2b2(<password token_ms_timeout="1800000" is_token="false">{passwd}</password>)
    cred.value should equal(passwd)
    cred.isToken should equal(false)

    cred = Credential.fromI2b2(<password>{passwd}</password>)
    cred.value should equal(passwd)
    cred.isToken should equal(false)
  }

  @Test
  def testToI2b2() {
    val passwd = "passwd"
    val isToken = false
    val credential = new Credential(passwd, isToken)
    credential.toI2b2 should equal(<password token_ms_timeout="1800000" is_token={isToken.toString}>{passwd}</password>)
  }

  @Test
  def testToXml() {
    val credential = new Credential("passwd", false)
    credential.toXml should equal(<credential isToken="false">passwd</credential>)
  }

  @Test
  def testFromXml() {
    var credential = Credential.fromXml(<credential isToken="false">passwd</credential>)
    credential.value should equal("passwd")
    credential.isToken should equal(false)

    credential = Credential.fromXml(<credential isToken="true">passwd</credential>)
    credential.isToken should equal(true)
  }

  @Test
  def testEqualsAndHashcode() {
    val credsTrue = new Credential("creds", true)
    val credsTrue2 = new Credential("creds", true)
    credsTrue should equal(credsTrue2)
    credsTrue2 should equal(credsTrue)
    credsTrue.hashCode should equal(credsTrue2.hashCode)

    val credsFalse = new Credential("creds", false)
    val credsFalse2 = new Credential("creds", false)
    credsFalse should equal(credsFalse2)
    credsFalse2 should equal(credsFalse)
    credsFalse.hashCode should equal(credsFalse2.hashCode)

    credsTrue should not equal (credsFalse)
    credsFalse should not equal (credsTrue)
  }
}