package net.shrine.adapter

import org.spin.tools.crypto.signature.Identity

/**
 * @author Andrew McMurry
 * @author clint
 * @date Jan 6, 2010
 * @date Nov 21, 2012 (Scala Port)
 */
final class AdapterLockoutException(identity: Identity) extends AdapterException {
  override def getMessage = "AdapterLockoutException[" + "domain=" + identity.getDomain + ", " + "username=" + identity.getUsername + ", " + "]"
}
