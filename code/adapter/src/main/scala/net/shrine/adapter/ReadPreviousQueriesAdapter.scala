package net.shrine.adapter

import dao.AdapterDAO
import net.shrine.protocol.{ReadPreviousQueriesResponse, BroadcastMessage}
import scala.collection.JavaConversions._
import org.spin.tools.crypto.signature.Identity
import net.shrine.config.HiveCredentials

/**
 * @author Bill Simons
 * @date 4/11/11
 * @link http://cbmi.med.harvard.edu
 * @link http://chip.org
 *       <p/>
 *       NOTICE: This software comes with NO guarantees whatsoever and is
 *       licensed as Lgpl Open Source
 * @link http://www.gnu.org/licenses/lgpl.html
 */
class ReadPreviousQueriesAdapter(
    override protected val crcUrl: String,
    override protected val dao: AdapterDAO,
    override protected val hiveCredentials: HiveCredentials) extends Adapter(crcUrl, dao, hiveCredentials) {

  protected def processRequest(identity: Identity, message: BroadcastMessage) = {
    val networkMasterDefs = dao.findNetworkMasterDefinitions(identity.getDomain, identity.getUsername)
    val response = new ReadPreviousQueriesResponse(identity.getUsername, identity.getDomain, networkMasterDefs)
    response
  }
}