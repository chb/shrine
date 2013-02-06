package net.shrine.service

/**
 * @author ?? (Bill Simons?)
 * @date ??
 * @link http://cbmi.med.harvard.edu
 * @link http://chip.org
 *       <p/>
 *       NOTICE: This software comes with NO guarantees whatsoever and is
 *       licensed as Lgpl Open Source
 * @link http://www.gnu.org/licenses/lgpl.html
 */
trait HappyShrineRequestHandler {
  def keystoreReport: String

  def routingReport: String

  def hiveReport: String

  def spinReport: String

  def adapterReport: String

  def auditReport: String

  def queryReport: String

  def versionReport: String

  def all: String
}