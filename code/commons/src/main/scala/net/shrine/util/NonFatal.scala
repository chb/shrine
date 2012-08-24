package net.shrine.util

import scala.util.control.ControlThrowable

/**
 * Extractor of non-fatal Throwables. Will not match fatal errors like `VirtualMachineError`
 * (for example, `OutOfMemoryError`, a subclass of `VirtualMachineError`), `ThreadDeath`,
 * `LinkageError`, `InterruptedException`, `ControlThrowable`, or `NotImplementedError`.
 * However, `StackOverflowError` is matched, i.e. considered non-fatal.
 *
 * Note that [[scala.util.control.ControlThrowable]], an internal Throwable, is not matched by
 * `NonFatal` (and would therefore be thrown).
 *
 * For example, all harmless Throwables can be caught by:
 * {{{
 * try {
 * // dangerous stuff
 * } catch {
 * case NonFatal(e) => log.error(e, "Something not that bad.")
 * // or
 * case e if NonFatal(e) => log.error(e, "Something not that bad.")
 * }
 * }}}
 */
//TODO: Remove when we depend on Scala 2.10
// **** Taken from https://github.com/scala/scala/blob/3bd897ba0054fd2cfd580c7f87ff6488c9dca4ea/src/library/scala/util/control/NonFatal.scala#L1 ****
object NonFatal {
  /**
   * Returns true if the provided `Throwable` is to be considered non-fatal, or false if it is to be considered fatal
   */
  def apply(t: Throwable): Boolean = t match {
    case _: StackOverflowError => true // StackOverflowError ok even though it is a VirtualMachineError
    // VirtualMachineError includes OutOfMemoryError and other fatal errors
    case _: VirtualMachineError | _: ThreadDeath | _: InterruptedException | _: LinkageError | _: ControlThrowable /*| _: NotImplementedError*/ => false
    case _ => true
  }
  /**
   * Returns Some(t) if NonFatal(t) == true, otherwise None
   */
  def unapply(t: Throwable): Option[Throwable] = if (apply(t)) Some(t) else None
}