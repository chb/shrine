package net.shrine.utilities.scanner.commands

/**
 * @author clint
 * @date Mar 21, 2013
 */
final case class CompoundCommand[A, B, C](first: Command[A, B], second: Command[B, C]) extends Command[A, C] {
  override def apply(a: A): C = second(first(a))
}