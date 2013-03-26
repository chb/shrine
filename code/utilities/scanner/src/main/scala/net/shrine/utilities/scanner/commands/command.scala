package net.shrine.utilities.scanner.commands

/**
 * @author clint
 * @date Mar 21, 2013
 */
trait >>>[-A, +B] extends (A => B) {
  def andThen[C](toC: B >>> C): A >>> C = CompoundCommand(this, toC)
}