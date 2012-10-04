package net.shrine.config

import net.shrine.protocol.AuthenticationInfo
import net.shrine.protocol.Credential

/**
 * @author Bill Simons
 * @date 3/12/12
 * @link http://cbmi.med.harvard.edu
 * @link http://chip.org
 *       <p/>
 *       NOTICE: This software comes with NO guarantees whatsoever and is
 *       licensed as Lgpl Open Source
 * @link http://www.gnu.org/licenses/lgpl.html
 */
final case class HiveCredentials (val domain: String, val username: String, val password: String, val project: String) {
  def toAuthenticationInfo = AuthenticationInfo(domain, username, Credential(password, false)) 
}