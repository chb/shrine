package net.shrine.utilities.scanner.components

import scala.concurrent.ExecutionContext
import java.util.concurrent.Executors

/**
 * @author clint
 * @date Mar 20, 2013
 */
trait HasSingleThreadExecutionContextComponent extends HasExecutionContextComponent {
  override implicit val executionContext = ExecutionContext.fromExecutorService(Executors.newFixedThreadPool(1))
}