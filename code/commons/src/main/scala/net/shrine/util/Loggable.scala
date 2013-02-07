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
  private[util] lazy val internaLogger: Logger = Logger.getLogger(this.getClass.getName)
  
  lazy val debugEnabled = internaLogger.isDebugEnabled
  
  lazy val infoEnabled = internaLogger.isInfoEnabled
  
  def debug(s: => Any): Unit = if(internaLogger.isDebugEnabled) internaLogger.debug(s)
  def info(s: => Any): Unit = if(internaLogger.isInfoEnabled) internaLogger.info(s)
  def warn(s: => Any): Unit = internaLogger.warn(s)
  def error(s: => Any): Unit = internaLogger.error(s)
  def error(s: => Any, e: Throwable): Unit = internaLogger.error(s, e)
}