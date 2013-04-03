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
final class CrcRequestType private[CrcRequestType] (val name: String, val i2b2RequestType: String) extends CrcRequestType.Value

//TODO: Rename these, add unit test

object CrcRequestType extends SEnum[CrcRequestType] {
  val GetPDOFromInputListRequestType = new CrcRequestType("GetPDOFromInputListRequestType", "getPDO_fromInputList")
  
  val InstanceRequestType = new CrcRequestType("InstanceRequestType", "CRC_QRY_getQueryResultInstanceList_fromQueryInstanceId")
  
  val MasterRequestType = new CrcRequestType("MasterRequestType", "CRC_QRY_getQueryInstanceList_fromQueryMasterId")
  
  val QueryDefinitionRequestType = new CrcRequestType("QueryDefinitionRequestType", "CRC_QRY_runQueryInstance_fromQueryDefinition")
  
  val UserRequestType = new CrcRequestType("UserRequestType", "CRC_QRY_getQueryMasterList_fromUserId")
  
  val ResultRequestType = new CrcRequestType("ResultRequestType", "CRC_QRY_getResultDocument_fromResultInstanceId")
  
  val MasterDeleteRequestType = new CrcRequestType("MasterDeleteRequestType", "CRC_QRY_deleteQueryMaster")
  
  val MasterRenameRequestType = new CrcRequestType("MasterRenameRequestType", "CRC_QRY_renameQueryMaster")
  
  val GetRequestXml = new CrcRequestType("GetRequestXml", "CRC_QRY_getRequestXml_fromQueryMasterId")
}
