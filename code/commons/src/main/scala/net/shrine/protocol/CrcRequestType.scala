package net.shrine.protocol

import net.shrine.util.SEnum

/**
 * Simple enum listing the types of CRC requests that Shrine knows how to
 * handle. The enum names are deliberately the same as the jaxb generated class
 * of the request.
 *
 * @author Justin Quan
 * @author clint
 * @date Jun 14, 2010
 * @link http://cbmi.med.harvard.edu
 * @link http://chip.org
 * <p/>
 * NOTICE: This software comes with NO guarantees whatsoever and is
 * licensed as Lgpl Open Source
 * @link http://www.gnu.org/licenses/lgpl.html
 */
final class CRCRequestType private[CRCRequestType] (val name: String, val i2b2RequestType: Option[String]) extends CRCRequestType.Value {
  def this(name: String, i2b2RequestType: String) = this(name, Option(i2b2RequestType))
  
  def unsafeI2b2RequestType = i2b2RequestType.get
}

object CRCRequestType extends SEnum[CRCRequestType] {
  val GetPDOFromInputListRequestType = new CRCRequestType("GetPDOFromInputListRequestType", "getPDO_fromInputList")
  
  val InstanceRequestType = new CRCRequestType("InstanceRequestType", "CRC_QRY_getQueryResultInstanceList_fromQueryInstanceId")
  
  val MasterRequestType = new CRCRequestType("MasterRequestType", "CRC_QRY_getQueryInstanceList_fromQueryMasterId")
  
  val QueryDefinitionRequestType = new CRCRequestType("QueryDefinitionRequestType", "CRC_QRY_runQueryInstance_fromQueryDefinition")
  
  val UserRequestType = new CRCRequestType("UserRequestType", "CRC_QRY_getQueryMasterList_fromUserId")
  
  val SheriffRequestType = new CRCRequestType("SheriffRequestType", None)
  
  val ResultRequestType = new CRCRequestType("ResultRequestType", "CRC_QRY_getResultDocument_fromResultInstanceId")
  
  val MasterDeleteRequestType = new CRCRequestType("MasterDeleteRequestType", "CRC_QRY_deleteQueryMaster")
  
  val MasterRenameRequestType = new CRCRequestType("MasterRenameRequestType", "CRC_QRY_renameQueryMaster")
  
  val GetRequestXml = new CRCRequestType("GetRequestXml", "CRC_QRY_getRequestXml_fromQueryMasterId")
}
