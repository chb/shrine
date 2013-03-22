package net.shrine.utilities.scanner

/**
 * @author clint
 * @date Mar 21, 2013
 */
sealed trait Disposition

object Disposition {
  final case object ShouldNotHaveBeenMapped extends Disposition
  final case object ShouldHaveBeenMapped extends Disposition
  final case object NeverFinished extends Disposition
}