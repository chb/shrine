package net.shrine.authorization

import net.shrine.protocol.ReadApprovedQueryTopicsRequest
import net.shrine.protocol.ReadApprovedQueryTopicsResponse
import net.shrine.protocol.RunQueryRequest

/**
 * @author Bill Simons
 * @date Aug 24, 2010
 * @link http://cbmi.med.harvard.edu
 * @link http://chip.org
 * <p/>
 * NOTICE: This software comes with NO guarantees whatsoever and is
 * licensed as Lgpl Open Source
 * @link http://www.gnu.org/licenses/lgpl.html
 */
trait QueryAuthorizationService {
  @throws(classOf[AuthorizationException])
  def authorizeRunQueryRequest(request: RunQueryRequest): Unit

  def readApprovedEntries(request: ReadApprovedQueryTopicsRequest): ReadApprovedQueryTopicsResponse
}
