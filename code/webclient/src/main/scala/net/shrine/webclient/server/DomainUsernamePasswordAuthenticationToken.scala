package net.shrine.webclient.server

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken

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
class DomainUsernamePasswordAuthenticationToken(
    val domain: String,
    val principal: String,
    val credentials: String) extends UsernamePasswordAuthenticationToken(principal, credentials)