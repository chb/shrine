package net.shrine.utilities.scanner.components

import scala.concurrent.ExecutionContext
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

/**
 * @author clint
 * @date Mar 20, 2013
 */
trait HasSingleThreadExecutionContextComponent extends HasExecutionContextComponent {
  private val executor = Executors.newSingleThreadExecutor
  
  override implicit lazy val executionContext: ExecutionContext = ExecutionContext.fromExecutorService(executor)
  
  def shutdownExecutor() {
    try {
      executor.shutdown()
      
      executor.awaitTermination(5, TimeUnit.SECONDS)
    } finally {
      executor.shutdownNow()
    }
  }
}