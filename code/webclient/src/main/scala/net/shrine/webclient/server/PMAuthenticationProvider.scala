package net.shrine.webclient.server

import org.springframework.security.authentication.{UsernamePasswordAuthenticationToken, AuthenticationProvider}
import org.springframework.security.core.Authentication
import net.shrine.i2b2.protocol.pm.{User, HiveConfig, GetUserConfigurationRequest}
import org.springframework.security.core.authority.{SimpleGrantedAuthority, GrantedAuthorityImpl}
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
class PMAuthenticationProvider(val pmEndpoint: String) extends AuthenticationProvider{
  def authenticate(authentication: Authentication) = {
    val domain = authentication.asInstanceOf[DomainUsernamePasswordAuthenticationToken].domain
    val pmRequest = new GetUserConfigurationRequest(domain, authentication.getPrincipal.asInstanceOf[String], authentication.getCredentials.asInstanceOf[String])

    val responseXml: String = HTTPClient.post(pmRequest.toI2b2String, pmEndpoint)
    val user = User.fromI2b2(responseXml) //TODO need error checking in parser

    //TODO validate user

    import scala.collection.JavaConversions._
    new UsernamePasswordAuthenticationToken(authentication.getPrincipal, authentication.getCredentials, Seq(new SimpleGrantedAuthority("ROLE_USER")))
  }

  def supports(authenticationClass: Class[_]) = classOf[DomainUsernamePasswordAuthenticationToken].isAssignableFrom(authenticationClass)
}