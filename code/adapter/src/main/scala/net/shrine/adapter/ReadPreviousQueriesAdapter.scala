package net.shrine.adapter

import net.shrine.protocol.{ReadPreviousQueriesResponse, BroadcastMessage}
import scala.collection.JavaConversions._
import org.spin.tools.crypto.signature.Identity
import net.shrine.config.HiveCredentials
import net.shrine.util.HttpClient
import net.shrine.adapter.dao.AdapterDao
import net.shrine.protocol.QueryMaster
import net.shrine.util.Util
import net.shrine.adapter.dao.model.ShrineQuery
import net.shrine.protocol.QueryMaster
import net.shrine.protocol.ShrineResponse
import net.shrine.serialization.XmlMarshaller
import net.shrine.util.Loggable
import net.shrine.protocol.ReadPreviousQueriesRequest

/**
 * @author Bill Simons
 * @author clint
 * @date 4/11/11
 * @link http://cbmi.med.harvard.edu
 * @link http://chip.org
 *       <p/>
 *       NOTICE: This software comes with NO guarantees whatsoever and is
 *       licensed as Lgpl Open Source
 * @link http://www.gnu.org/licenses/lgpl.html
 */
class ReadPreviousQueriesAdapter(dao: AdapterDao) extends Adapter with Loggable {
  
  override protected[adapter] def processRequest(identity: Identity, message: BroadcastMessage): XmlMarshaller = {
    val fetchSize = message.request.asInstanceOf[ReadPreviousQueriesRequest].fetchSize
    
    val previousQueries = dao.findQueriesByUserAndDomain(identity.getDomain, identity.getUsername, fetchSize)
    
    ReadPreviousQueriesResponse(identity.getUsername, identity.getDomain, previousQueries.map(_.toQueryMaster))
  }
}
