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
    val hasPsmHeader = hasMessageBodySubElement(requestXml, "psmheader") 
    
    val hasRequestType = requestType(requestXml).nonEmpty
    
    hasPsmHeader && hasRequestType
  }

  protected def hasMessageBodySubElement(requestXml: NodeSeq, tagName: String): Boolean = {
    (requestXml \ "message_body" \ tagName).nonEmpty
  }
  
  private def requestType(requestXml: NodeSeq): NodeSeq = requestXml \ "message_body" \ "psmheader" \ "request_type"
  
  protected def parsePsmRequest(requestXml: NodeSeq): Req = {
    val incomingRequestType = requestType(requestXml).text

    crcRequestUnmarshallersByI2b2RequestType.get(incomingRequestType) match {
      case None => null.asInstanceOf[Req]
      case Some(u) => u.fromI2b2(requestXml)
    }
  }
}