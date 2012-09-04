package net.shrine.webclient.server

import net.shrine.webclient.client.domain.BootstrapInfo

/**
 * @author clint
 * @date Sep 4, 2012
 */
final class SpringSecurityBootstrapInfoSource extends BootstrapInfoSource {
  override def bootstrapInfo = new BootstrapInfo(SessionUtil.loggedInUser.map(_.username).orNull)
}