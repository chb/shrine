package net.shrine.webclient.server

import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter
import javax.servlet.http.{HttpServletResponse, HttpServletRequest}
import org.springframework.security.authentication.{UsernamePasswordAuthenticationToken, AuthenticationServiceException}

/**
 * @author Bill Simons
 * @date 7/17/12
 * @link http://cbmi.med.harvard.edu
 * @link http://chip.org
 *       <p/>
 *       NOTICE: This software comes with NO guarantees whatsoever and is
 *       licensed as Lgpl Open Source
 * @link http://www.gnu.org/licenses/lgpl.html
 */
object DomainUsernamePasswordAuthenticationProcessingFilter {
  private val domainParameter = "j_domain"
  private val usernameParameter = "j_username"
  private val passwordParameter = "j_password"
  private val securityCheckUrl = "/j_spring_security_check"
}

import DomainUsernamePasswordAuthenticationProcessingFilter._

final class DomainUsernamePasswordAuthenticationProcessingFilter extends AbstractAuthenticationProcessingFilter(securityCheckUrl) {

  def attemptAuthentication(request: HttpServletRequest, response: HttpServletResponse) = {
    if(request.getMethod != "POST") {
      throw new AuthenticationServiceException("Authentication method not supported: " + request.getMethod)
    }

    val domain = Option(request.getParameter(domainParameter)).getOrElse("")
    val username = Option(request.getParameter(usernameParameter)).getOrElse("")
    val password = Option(request.getParameter(passwordParameter)).getOrElse("")

    val authRequest = new DomainUsernamePasswordAuthenticationToken(domain, username, password)

    getAuthenticationManager.authenticate(authRequest)
  }
}