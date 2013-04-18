package net.shrine.protocol

import CrcRequestType._
import scala.xml.NodeSeq

/**
 * @author clint
 * @date Apr 17, 2013
 */
trait HandleableShrineRequest { self: ShrineRequest =>
  def handle(handler: ShrineRequestHandler, shouldBroadcast: Boolean): ShrineResponse
}

object HandleableShrineRequest extends AbstractUnmarshallerCompanion[ShrineRequest with HandleableShrineRequest](
  Map(
    InstanceRequestType -> ReadInstanceResultsRequest,
    UserRequestType -> ReadPreviousQueriesRequest,
    GetRequestXml -> ReadQueryDefinitionRequest,
    MasterRequestType -> ReadQueryInstancesRequest,
    QueryDefinitionRequestType -> RunQueryRequest,
    MasterRenameRequestType -> RenameQueryRequest,
    MasterDeleteRequestType -> DeleteQueryRequest)) {

  override def fromI2b2(i2b2Request: NodeSeq): ShrineRequest with HandleableShrineRequest = {
    if (isPsmRequest(i2b2Request)) {
      parsePsmRequest(i2b2Request)
    } else if (isPdoRequest(i2b2Request)) {
      parsePdoRequest(i2b2Request)
    } else if (isSheriffRequest(i2b2Request)) {
      parseSheriffRequest(i2b2Request)
    } else {
      throw new Exception(s"Request not understood: $i2b2Request")
    }
  }

  protected def isPdoRequest(requestXml: NodeSeq): Boolean = hasMessageBodySubElement(requestXml, "pdoheader")

  protected def isSheriffRequest(requestXml: NodeSeq): Boolean = hasMessageBodySubElement(requestXml, "sheriff_header")

  protected def isI2b2AdminPreviousQueriesRequest(requestXml: NodeSeq): Boolean = hasMessageBodySubElement(requestXml, "get_name_info")

  private def parsePdoRequest(requestXml: NodeSeq): ReadPdoRequest = {
    (requestXml \ "message_body" \ "pdoheader" \ "request_type").text match {
      case x if x == GetPDOFromInputListRequestType.i2b2RequestType => ReadPdoRequest.fromI2b2(requestXml)
      case _ => null
    }
  }

  private def parseSheriffRequest(xml: NodeSeq) = ReadApprovedQueryTopicsRequest.fromI2b2(xml)

  private def parseI2b2AdminPreviousQueriesRequest(xml: NodeSeq) = ReadI2b2AdminPreviousQueriesRequest.fromI2b2(xml)
}