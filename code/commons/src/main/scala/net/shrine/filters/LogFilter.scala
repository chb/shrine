package net.shrine.filters

import org.apache.log4j.Logger
import org.apache.log4j.MDC

import javax.servlet._
import java.util.Random
import java.lang.Long.toHexString
import java.lang.Boolean.parseBoolean

/**
 * @author Justin Quan
 * @date Nov 30, 2010
 * @link http://cbmi.med.harvard.edu
 * @link http://chip.org
 * <p/>
 * NOTICE: This software comes with NO guarantees whatsoever and is
 * licensed as Lgpl Open Source
 * @link http://www.gnu.org/licenses/lgpl.html
 */
object LogFilter {
  private val log = Logger.getLogger(classOf[LogFilter])

  val GRID = "globalRequestId"

  val LRID = "localRequestId"

  private val IS_GLOBAL_HEAD = "isGlobalHead"
}

final class LogFilter extends Filter {
  import LogFilter._

  private val rand = new Random

  private var isGlobalHead: Boolean = _

  override def init(filterConfig: FilterConfig) {
    val stringGlobal = Option(filterConfig.getInitParameter(IS_GLOBAL_HEAD))

    isGlobalHead = stringGlobal.map(parseBoolean).getOrElse(false)
  }

  override def doFilter(servletRequest: ServletRequest, servletResponse: ServletResponse, filterChain: FilterChain) {
    if (isGlobalHead) {
      MDC.put(GRID, toHexString(rand.nextLong))
    } else {
      // TODO: when we start to pass along the globalRequestId along w/ subsequent requests, read it out here
      // Unfortunately shrine sends messages out via SPIN which doesn't give us a facility yet to pass in our own tracking ids, yet
    }

    filterChain.doFilter(servletRequest, servletResponse)
  }

  override def destroy() {}
}
