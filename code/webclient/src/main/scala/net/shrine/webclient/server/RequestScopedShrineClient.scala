package net.shrine.webclient.server

import net.shrine.service.{ JerseyShrineClient, ShrineClient }
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import net.shrine.i2b2.protocol.pm.User
import net.shrine.protocol.{ Credential, AuthenticationInfo }
import net.shrine.util.Loggable

/**
 * @author Bill Simons
 * @date 8/17/12
 * @link http://cbmi.med.harvard.edu
 * @link http://chip.org
 *       <p/>
 *       NOTICE: This software comes with NO guarantees whatsoever and is
 *       licensed as Lgpl Open Source
 * @link http://www.gnu.org/licenses/lgpl.html
 */
object RequestScopedShrineClient extends Loggable {
  def apply(shrineUrl: String, projectId: String): ShrineClient = {
    val clientOption = for {
      user <- SpringSecuritySessionUtil.loggedInUser
      authn = new AuthenticationInfo(user.domain, user.username, user.credential)
    } yield new JerseyShrineClient(shrineUrl, projectId, authn)

    clientOption.get
  }
}