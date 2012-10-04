package net.shrine.config

import junit.framework.TestCase
import org.scalatest.matchers.ShouldMatchers
import org.scalatest.junit.AssertionsForJUnit

/**
 * @author clint
 * @date Oct 4, 2012
 */
final class HiveCredentialsTest extends TestCase with ShouldMatchers with AssertionsForJUnit {
  def testToAuthenticationInfo {
    val creds = HiveCredentials("domain", "username", "password", "project")
    
    val authn = creds.toAuthenticationInfo
    
    authn should not be(null)
    
    authn.domain should equal(creds.domain)
    authn.username should equal(creds.username)
    authn.credential.value should equal(creds.password)
    authn.credential.isToken should be(false)
  }
}