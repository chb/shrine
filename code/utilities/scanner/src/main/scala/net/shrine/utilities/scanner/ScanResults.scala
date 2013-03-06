package net.shrine.utilities.scanner

/**
 * @author clint
 * @date Mar 5, 2013
 */
final case class ScanResults(
    //Terms that we expected to BE mapped, but were NOT mapped
    shouldHaveBeenMapped: Set[String],
    //Terms that we expected to NOT be mapped, but ARE mapped
    shouldNotHaveBeenMapped: Set[String],
    //Terms that never completed after some timeout period
    neverFinished: Set[String]) {
  
  def withShouldHaveBeenMappedTerms(newShouldHaveBeenMapped: Set[String]) = copy(shouldHaveBeenMapped = newShouldHaveBeenMapped)
  
  def withShouldNotHaveBeenMappedTerms(newShouldNotHaveBeenMapped: Set[String]) = copy(shouldNotHaveBeenMapped = newShouldNotHaveBeenMapped)
  
  def withNeverFinishedTerms(newNeverFinished: Set[String]) = copy(neverFinished = newNeverFinished)
}