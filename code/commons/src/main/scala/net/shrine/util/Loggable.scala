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
  def debug(s: => Any, e: Throwable): Unit = if(internaLogger.isDebugEnabled) internaLogger.debug(s, e)
  
  def info(s: => Any): Unit = if(internaLogger.isInfoEnabled) internaLogger.info(s)
  def info(s: => Any, e: Throwable): Unit = if(internaLogger.isInfoEnabled) internaLogger.info(s, e)
  
  def warn(s: => Any): Unit = internaLogger.warn(s)
  def warn(s: => Any, e: Throwable): Unit = internaLogger.warn(s, e)
  
  def error(s: => Any): Unit = internaLogger.error(s)
  def error(s: => Any, e: Throwable): Unit = internaLogger.error(s, e)
}