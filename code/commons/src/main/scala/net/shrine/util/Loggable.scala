package net.shrine.util

import org.apache.log4j.Logger

/**
 * Apparently this is how the scala community likes to boilerplate their loggers
 *
 * @author Justin Quan
 * @link http://chip.org
 * Date: 8/8/11
 */
trait Loggable {
  private[util] lazy val log: Logger = Logger.getLogger(this.getClass.getName)
  
  def debug(s: => String): Unit = if(log.isDebugEnabled) log.debug(s)
  def info(s: => String): Unit = if(log.isInfoEnabled) log.info(s)
  def warn(s: => String): Unit = log.warn(s)
  def error(s: => String): Unit = log.error(s)
  def error(e: Throwable, s: => String): Unit = log.error(s, e)
}