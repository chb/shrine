package net.shrine.utilities.scanner.commands

/**
 * @author clint
 * @date Mar 21, 2013
 */
trait Command[A, B] extends (A => B) {
  def andThen[C](c: Command[B, C]): Command[A, C] = CompoundCommand(this, c)
}