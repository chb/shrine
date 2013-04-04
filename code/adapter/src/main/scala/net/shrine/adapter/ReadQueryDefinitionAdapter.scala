package net.shrine.adapter

import org.spin.tools.crypto.signature.Identity

import net.shrine.adapter.components.QueryDefinitionSourceComponent
import net.shrine.adapter.dao.AdapterDao
import net.shrine.protocol.BroadcastMessage
import net.shrine.protocol.ReadQueryDefinitionRequest
import net.shrine.serialization.XmlMarshaller

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
class ReadQueryDefinitionAdapter(override val dao: AdapterDao) extends Adapter with QueryDefinitionSourceComponent {

  override protected[adapter] def processRequest(identity: Identity, message: BroadcastMessage): XmlMarshaller = {
    val request = message.request.asInstanceOf[ReadQueryDefinitionRequest]

    QueryDefinitions.get(request)
  }
}