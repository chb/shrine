package net.shrine.webclient.server

import javax.servlet.Filter
import javax.servlet.FilterConfig
import javax.servlet.ServletRequest
import javax.servlet.ServletResponse
import javax.servlet.FilterChain
import javax.servlet.http.Cookie
import net.shrine.util.{ Versions => CommonsVersions }
import javax.servlet.http.HttpServletResponse
import net.shrine.webclient.shared.Versions
import net.shrine.webclient.shared.Cookies

/**
 * @author clint
 * @date Aug 31, 2012
 */
final class VersionCookieSettingServletFilter extends Filter {
  private lazy val versions = new Versions(CommonsVersions.version, CommonsVersions.scmRevision, CommonsVersions.scmBranch, CommonsVersions.buildDate)

  override def init(filterConfig: FilterConfig) = ()

  override def destroy() = ()

  override def doFilter(request: ServletRequest, response: ServletResponse, chain: FilterChain) {
    try {
      val cookie = new Cookie(Cookies.Version.cookieName, versions.toString)

      response.asInstanceOf[HttpServletResponse].addCookie(cookie)
    } finally {
      chain.doFilter(request, response)
    }
  }
}