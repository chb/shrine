package net.shrine.protocol

import net.shrine.serialization.I2b2Unmarshaller
import scala.xml.NodeSeq

/**
 * @author clint
 * @date Mar 29, 2013
 */
abstract class AbstractI2b2UnmarshallerCompanion[Req <: ShrineRequest](i2b2Unmarshallers: Map[CRCRequestType, I2b2Unmarshaller[Req]]) extends I2b2Unmarshaller[Req] {
 private val unmarshallersByStringRequestType = i2b2Unmarshallers.map { case (rt, u) => (rt.unsafeI2b2RequestType, u) }
  
  protected def isPsmRequest(requestXml: NodeSeq): Boolean = {
    (requestXml \ "message_body" \ "psmheader").nonEmpty
  }

  protected def isPdoRequest(requestXml: NodeSeq): Boolean = {
    (requestXml \ "message_body" \ "pdoheader").nonEmpty
  }

  protected def isSheriffRequest(requestXml: NodeSeq): Boolean = {
    (requestXml \ "message_body" \ "sheriff_header").nonEmpty
  }

  protected def parsePsmRequest(requestXml: NodeSeq): Req = {
    val incomingRequestType = (requestXml \ "message_body" \ "psmheader" \ "request_type").text

    unmarshallersByStringRequestType.get(incomingRequestType) match {
      case None => null.asInstanceOf[Req]
      case Some(u) => u.fromI2b2(requestXml)
    }
  }
}