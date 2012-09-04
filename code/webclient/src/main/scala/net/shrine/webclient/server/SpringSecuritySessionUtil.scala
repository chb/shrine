package net.shrine.webclient.server

import org.springframework.security.core.context.SecurityContextHolder
import net.shrine.i2b2.protocol.pm.User
import net.shrine.util.Try

/**
 * @author clint
 * @date Sep 4, 2012
 */
object SpringSecuritySessionUtil {
  def loggedInUser: Option[User] = try {
    Option(SecurityContextHolder.getContext.getAuthentication.getPrincipal.asInstanceOf[User])
  } catch {
    case e: Exception => None
  }
}