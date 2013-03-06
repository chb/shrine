package net.shrine.protocol

import org.scalatest.junit.ShouldMatchersForJUnit
import junit.framework.TestCase
import org.junit.Test

/**
 * @author clint
 * @date Mar 6, 2013
 */
final class StatusTypeTest extends TestCase with ShouldMatchersForJUnit {
  import QueryResult.StatusType._
  
  @Test
  def testName {
    Error.name should be("ERROR")
    Finished.name should be("FINISHED")
    Processing.name should be("PROCESSING")
    Queued.name should be("QUEUED")
  }
  
  @Test
  def testIsDone {
    Error.isDone should be(true)
    Finished.isDone should be(true)
    Processing.isDone should be(false)
    Queued.isDone should be(false)
  }
  
  @Test
  def testIsError {
    Error.isError should be(true)
    Finished.isError should be(false)
    Processing.isError should be(false)
    Queued.isError should be(false)
  }
}