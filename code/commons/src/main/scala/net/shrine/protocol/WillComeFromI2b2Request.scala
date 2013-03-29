package net.shrine.protocol

import net.shrine.protocol.CRCRequestType._
import scala.xml.NodeSeq

/**
 * @author clint
 * @date Mar 29, 2013
 */
object WillComeFromI2b2ShrineRequest extends AbstractI2b2UnmarshallerCompanion[ShrineRequest](
  Map(
    InstanceRequestType -> ReadInstanceResultsRequest,
    UserRequestType -> ReadPreviousQueriesRequest,
    GetRequestXml -> ReadQueryDefinitionRequest,
    MasterRequestType -> ReadQueryInstancesRequest,
    QueryDefinitionRequestType -> RunQueryRequest,
    MasterRenameRequestType -> RenameQueryRequest,
    MasterDeleteRequestType -> DeleteQueryRequest,
    ResultRequestType -> ReadResultRequest)) {
  
  override def fromI2b2(i2b2Request: NodeSeq): ShrineRequest = {
    i2b2Request match {
      case x if isPsmRequest(x) => parsePsmRequest(x)
      case _ => throw new Exception(s"Request not understood: $i2b2Request")
    }
  }
}
 