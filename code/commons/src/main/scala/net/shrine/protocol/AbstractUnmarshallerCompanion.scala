package net.shrine.protocol

import net.shrine.serialization.I2b2Unmarshaller
import scala.xml.NodeSeq
import net.shrine.serialization.XmlUnmarshaller

/**
 * @author clint
 * @date Mar 29, 2013
 */
abstract class AbstractUnmarshallerCompanion[Req <: ShrineRequest](i2b2CrcRequestUnmarshallers: Map[CrcRequestType, I2b2Unmarshaller[Req]]) extends I2b2Unmarshaller[Req] {

  private val crcRequestUnmarshallersByI2b2RequestType: Map[String, I2b2Unmarshaller[Req]] = {
    i2b2CrcRequestUnmarshallers.map { case (rt, u) => (rt.i2b2RequestType, u) }
  }

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

    crcRequestUnmarshallersByI2b2RequestType.get(incomingRequestType) match {
      case None => null.asInstanceOf[Req]
      case Some(u) => u.fromI2b2(requestXml)
    }
  }
}