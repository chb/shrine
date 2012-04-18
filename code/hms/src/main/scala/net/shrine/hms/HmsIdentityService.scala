package net.shrine.hms

import net.shrine.serializers.HTTPClient
import net.shrine.i2b2.protocol.pm.{User, GetUserConfigurationRequest}
import org.spin.tools.crypto.signature.{XMLSignatureUtil, Identity}
import org.spin.query.message.identity.{IdentityServiceException, IdentityService}

/**
 * @author Bill Simons
 * @date 3/7/12
 * @link http://cbmi.med.harvard.edu
 * @link http://chip.org
 *       <p/>
 *       NOTICE: This software comes with NO guarantees whatsoever and is
 *       licensed as Lgpl Open Source
 * @link http://www.gnu.org/licenses/lgpl.html
 */
class HmsIdentityService(pmEndpoint: String) extends IdentityService {
  def certify(domain: String, username: String, password: String) = {
    try {
      val requestString = new GetUserConfigurationRequest(domain, username, password).toI2b2String
      val responseString = HTTPClient.post(requestString, pmEndpoint)
      val user = User.fromI2b2(responseString)
      val ecommonsUsername = user.params("ecommons_username")
      if(ecommonsUsername == null) {
        throw new IdentityServiceException(String.format("No ecommons id for user %s", username));
      }
      val identity = new Identity(domain, ecommonsUsername)
      XMLSignatureUtil.getDefaultInstance().sign(identity)
    }
    catch {
      case e:Exception => throw new IdentityServiceException(String.format("Failed to certify user %s", username), e)
    }
  }
}