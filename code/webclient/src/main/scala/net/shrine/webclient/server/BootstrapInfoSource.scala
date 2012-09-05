package net.shrine.webclient.server

import net.shrine.webclient.client.domain.BootstrapInfo

/**
 * @author clint
 * @date Sep 4, 2012
 */
trait BootstrapInfoSource {
  def bootstrapInfo: BootstrapInfo
}