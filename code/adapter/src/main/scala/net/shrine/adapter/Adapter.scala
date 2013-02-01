package net.shrine.adapter

import org.spin.node.QueryContext
import java.lang.String
import org.spin.tools.crypto.signature.Identity
import net.shrine.protocol.{ErrorResponse, ShrineResponse, BroadcastMessage}
import net.shrine.util.Loggable
import net.shrine.config.HiveCredentials
import org.spin.node.AbstractQueryAction
import net.shrine.serialization.XmlMarshaller

/**
 * @author Bill Simons
 * @date 4/8/11
 * @link http://cbmi.med.harvard.edu
 * @link http://chip.org
 *       <p/>
 *       NOTICE: This software comes with NO guarantees whatsoever and is
 *       licensed as Lgpl Open Source
 * @link http://www.gnu.org/licenses/lgpl.html
 */
abstract class Adapter extends AbstractQueryAction[BroadcastMessage] with Loggable {
  
  final override def unmarshal(serializedCriteria: String) = BroadcastMessage.fromXml(serializedCriteria)

  final override def perform(context: QueryContext, message: BroadcastMessage): String = {
    val shrineResponse = try {
      processRequest(context.getQueryInfo.getIdentity, message)
    } catch {
      case e: AdapterLockoutException => new ErrorResponse(e.getMessage)
      case e: Exception => {
        //for now we'll warn on all errors and work towards more specific logging later
        warn(String.format("Exception %s in Adapter with stack trace:\r\n%s caused on request\r\n %s", e.toString, e.getStackTraceString, message.toXmlString))
        
        ErrorResponse(e.getMessage)
      }
    }

    shrineResponse.toXmlString
  }

  protected[adapter] def processRequest(identity: Identity, message: BroadcastMessage): XmlMarshaller
}