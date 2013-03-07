package net.shrine.utilities.scanner

/**
 * @author clint
 * @date Mar 6, 2013
 */
trait Scanner {
  def scan(): ScanResults
  
  def reScan(neverFinished: Set[TermResult]): ReScanResults
}