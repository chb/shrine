package net.shrine.protocol

import net.shrine.util.SEnum

/**
 * @author clint
 * @date Apr 2, 2013
 */
final class RequestType private[RequestType] (override val name: String, val crcRequestType: Option[CrcRequestType]) extends RequestType.Value {
  def this(crcRequestType: CrcRequestType) = this(crcRequestType.name, Option(crcRequestType))
}

object RequestType extends SEnum[RequestType] {
  val SheriffRequest = new RequestType("SheriffRequest", None)

  val GetQueryResult = new RequestType("GetQueryResult", None)
  
  val ReadI2b2AdminPreviousQueriesRequest = new RequestType("ReadI2b2AdminPreviousQueriesRequest", None)
  
  val GetPDOFromInputListRequest = new RequestType(CrcRequestType.GetPDOFromInputListRequestType)
  
  val InstanceRequest = new RequestType(CrcRequestType.InstanceRequestType)
  
  val MasterRequest = new RequestType(CrcRequestType.MasterRequestType)
  
  val QueryDefinitionRequest = new RequestType(CrcRequestType.QueryDefinitionRequestType)
  
  val UserRequest = new RequestType(CrcRequestType.UserRequestType)
  
  val ResultRequest = new RequestType(CrcRequestType.ResultRequestType)
  
  val MasterDeleteRequest = new RequestType(CrcRequestType.MasterDeleteRequestType)
  
  val MasterRenameRequest = new RequestType(CrcRequestType.MasterRenameRequestType)
  
  val GetRequestXml = new RequestType(CrcRequestType.GetRequestXml)
}

