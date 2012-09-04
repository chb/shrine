package net.shrine.webclient.server.api

import net.shrine.webclient.client.domain.BootstrapInfo
import net.shrine.webclient.server.BootstrapInfoSource

/**
 * @author clint
 * @date Sep 4, 2012
 */
final class MockBootstrapInfoSource(loggedInUser: String) extends BootstrapInfoSource {
  override def bootstrapInfo = new BootstrapInfo(loggedInUser)
}