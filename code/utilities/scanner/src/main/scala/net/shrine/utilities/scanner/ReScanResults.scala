package net.shrine.utilities.scanner

/**
 * @author clint
 * @date Mar 6, 2013
 */
final case class ReScanResults(stillNotFinished: Set[String], shouldHaveBeenMapped: Set[String])

object ReScanResults {
  val empty = ReScanResults(Set.empty, Set.empty)
}