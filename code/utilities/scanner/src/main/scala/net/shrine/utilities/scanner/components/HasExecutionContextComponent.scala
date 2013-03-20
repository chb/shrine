package net.shrine.utilities.scanner.components

import scala.concurrent.ExecutionContext

/**
 * @author clint
 * @date Mar 20, 2013
 */
trait HasExecutionContextComponent {
  implicit val executionContext: ExecutionContext
}