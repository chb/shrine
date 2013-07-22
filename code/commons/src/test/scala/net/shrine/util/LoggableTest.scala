package net.shrine.util

import org.scalatest.junit.AssertionsForJUnit
import org.scalatest.junit.ShouldMatchersForJUnit
import scala.collection.mutable.{Map => MMap}
import org.apache.log4j.Priority
import junit.framework.TestCase

/**
 *
 * @author Clint Gilbert
 * @date Oct 11, 2011
 *
 * @link http://cbmi.med.harvard.edu
 *
 * This software is licensed under the LGPL
 * @link http://www.gnu.org/licenses/lgpl.html
 *
 */
final class LoggableTest extends TestCase with AssertionsForJUnit with ShouldMatchersForJUnit {
  
  import Priority.{DEBUG, INFO, WARN, ERROR}
  
  def testDebug {
    doTest(_.debug, DEBUG, logMessageIsLazy = true)
  }
  
  def testInfo {
    doTest(_.info, INFO, logMessageIsLazy = true)
  }
  
  def testWarn {
    doTest(_.warn, WARN, logMessageIsLazy = false)
  }
  
  def testError {
    doTest(_.error, ERROR, logMessageIsLazy = false)
  }
  
  private def doTest(log: Loggable => ( => String) => Unit, priority: Priority, logMessageIsLazy: Boolean) {
    val loggable = new MockLoggable
    
    var messageComputed = false
    
    val otherPriority = if(logMessageIsLazy) higher(priority) else not(priority)
    
    loggable.log.setPriority(otherPriority)
    
    log(loggable)({ messageComputed = true ; "message" })
    
    //the log message should NOT have been computed if this log method is lazy
    messageComputed should not(be(logMessageIsLazy))
    //but we should have tried to log
    loggable.loggedAt(priority) should be(true)
    
    //reset our mock
    loggable.reset()
    messageComputed = false
    
    loggable.log.setPriority(priority)
    
    log(loggable)({ messageComputed = true ; "message" })
    
    //the log message should have been computed 
    messageComputed should be(true)
    //we should have tried to log at the desired level
    loggable.loggedAt(priority) should be(true)
    //and at no other levels
    loggable.loggedAt.exists { case (priority, happened) => priority != priority && happened == true } should be(false)
  }
  
  private val priorities: Map[Priority, Int] = Seq(DEBUG, INFO, WARN, ERROR).zipWithIndex.toMap
  
  private def next(priority: Priority, adjust: Int => Int): Priority = {
    val numericalPriority = priorities(priority)
    
    priorities.find { case (p, index) => index == adjust(numericalPriority) }.map { case (p, i) => p}.get
  }
  
  private def not(priority: Priority): Priority = priorities.keys.find(_ != priority).get
  private def higher(priority: Priority): Priority = next(priority, _ + 1)
  private def lower(priority: Priority): Priority = next(priority, _ - 1)
  
  private final class MockLoggable extends Loggable {
    val loggedAt: MMap[Priority, Boolean] = MMap.empty
    
    def reset() {
      loggedAt.keys.foreach(loggedAt(_) = false)
    }
    
    override def debug(s: => String) {
      loggedAt(Priority.DEBUG) = true 
      super.debug(s)
    }
    
    override def info(s: => String) {
      loggedAt(Priority.INFO) = true 
      super.info(s)
    }
    
    override def warn(s: => String) {
      loggedAt(Priority.WARN) = true 
      super.warn(s)
    }
    
    override def error(s: => String) {
      loggedAt(Priority.ERROR) = true 
      super.error(s)
    }
  }
}