package net.shrine.protocol

import CrcRequestType._
import scala.xml.NodeSeq

/**
 * @author clint
 * @date Apr 17, 2013
 */
trait HandleableAdminShrineRequest {self: ShrineRequest =>
  def handleAdmin(handler: I2b2AdminRequestHandler, shouldBroadcast: Boolean): ShrineResponse
}

object HandleableAdminShrineRequest extends AbstractUnmarshallerCompanion[ShrineRequest with HandleableAdminShrineRequest](
  Map(GetRequestXml -> ReadQueryDefinitionRequest)) {

  override def fromI2b2(i2b2Request: NodeSeq): ShrineRequest with HandleableAdminShrineRequest = {
    if (isPsmRequest(i2b2Request)) {
      parsePsmRequest(i2b2Request)
    } else if (isI2b2AdminPreviousQueriesRequest(i2b2Request)) {
      parseI2b2AdminPreviousQueriesRequest(i2b2Request)
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