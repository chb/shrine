package net.shrine.webclient.server

import org.springframework.security.authentication.{BadCredentialsException, UsernamePasswordAuthenticationToken, AuthenticationProvider}
import org.springframework.security.core.Authentication
import net.shrine.i2b2.protocol.pm.{User, GetUserConfigurationRequest}
import org.springframework.security.core.authority.SimpleGrantedAuthority
import net.shrine.util.HTTPClient

/**
 * @author Bill Simons
 * @date 7/12/12
 * @link http://cbmi.med.harvard.edu
 * @link http://chip.org
 *       <p/>
 *       NOTICE: This software comes with NO guarantees whatsoever and is
 *       licensed as Lgpl Open Source
 * @link http://www.gnu.org/licenses/lgpl.html
 */
final class PMAuthenticationProvider(val pmEndpoint: String) extends AuthenticationProvider {

  def validUser(token: DomainUsernamePasswordAuthenticationToken, user: User): Boolean = (token.domain == user.domain) && (token.principal == user.username)

  def authenticate(authentication: Authentication) = {
    val token = authentication.asInstanceOf[DomainUsernamePasswordAuthenticationToken]
    val domain = token.domain
    val pmRequest = new GetUserConfigurationRequest(domain, token.principal, authentication.getCredentials.asInstanceOf[String])

    val responseXml: String = HTTPClient.post(pmRequest.toI2b2String, pmEndpoint)
    val user = User.fromI2b2(responseXml)

    if(!validUser(token, user)) {
      throw new BadCredentialsException("Invalid credentials")
    }

    import scala.collection.JavaConverters._
    new UsernamePasswordAuthenticationToken(user, authentication.getCredentials, Seq(new SimpleGrantedAuthority("ROLE_USER")).asJava)

  }

  def supports(authenticationClass: Class[_]) = classOf[DomainUsernamePasswordAuthenticationToken].isAssignableFrom(authenticationClass)
}