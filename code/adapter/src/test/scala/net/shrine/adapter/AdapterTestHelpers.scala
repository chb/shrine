package net.shrine.adapter

import org.spin.tools.crypto.signature.Identity

import net.shrine.protocol.AuthenticationInfo
import net.shrine.protocol.Credential

/**
 * @author clint
 * @date Nov 28, 2012
 */
trait AdapterTestHelpers {
  val id = new Identity("some-domain", "some-user")
  val authn = AuthenticationInfo("Some-other-domain", "some-other-user", Credential("aslkdjkaljsd", false))
  val queryId = 123
  val localMasterId = "kasjdlsajdklajsdkljasd"
  val bogusQueryId = 999
}