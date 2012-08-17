package net.shrine.protocol

import scala.xml.NodeSeq
import net.shrine.serialization.XmlUnmarshaller
import net.shrine.serialization.I2b2Unmarshaller
import net.shrine.util.XmlUtil
import scala.xml.XML

/**
 * @author clint
 * @date Aug 17, 2012
 */
final case class ReadResultResponse(xmlResultId: Long, metadata: QueryResult, data: I2b2ResultEnvelope) extends ShrineResponse {
  protected override def i2b2MessageBody: NodeSeq = XmlUtil.stripWhitespace(
    <ns4:response xsi:type="ns4:crc_xml_result_responseType">
      <status>
        <condition type="DONE">DONE</condition>
      </status>
      { metadata.toI2b2 }
      <crc_xml_result>
        <xml_result_id>{ xmlResultId }</xml_result_id>
        <result_instance_id>{ metadata.resultId }</result_instance_id>
        <xml_value>
          <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
          { I2b2Workarounds.escape(data.toI2b2String) }
        </xml_value>
      </crc_xml_result>
    </ns4:response>)

  //xmlResultId doesn't seem necessary, but I wanted to allow Shrine => I2b2 => Shrine marshalling loops without losing anything.  
  //Maybe this isn't needed? 
  override def toXml: NodeSeq = XmlUtil.stripWhitespace(
    <readResultResponse>
      { metadata.toXml }
      <xmlResultId>{ xmlResultId }</xmlResultId>
      { data.toXml }
    </readResultResponse>)
}

object ReadResultResponse extends XmlUnmarshaller[ReadResultResponse] with I2b2Unmarshaller[ReadResultResponse] {
  override def fromXml(xml: NodeSeq): ReadResultResponse = unmarshal(xml, _ \ "resultEnvelope", I2b2ResultEnvelope.fromXml, _ \ "queryResult", QueryResult.fromXml, x => (x \ "xmlResultId").text.toLong)

  override def fromI2b2(xml: NodeSeq): ReadResultResponse = {
    val getXmlResultXml = (x: NodeSeq) => x \ "crc_xml_result"
    
    val envelopeXml = (x: NodeSeq) => XML.loadString(I2b2Workarounds.unescape((getXmlResultXml(x) \ "xml_value").text))
    
    val getXmlResultId = (x: NodeSeq) => (x \ "xml_result_id").text.toLong
    
    unmarshal(xml, envelopeXml, I2b2ResultEnvelope.fromI2b2, _ \ "query_result_instance", QueryResult.fromI2b2, getXmlResultXml andThen getXmlResultId)
  }
  
  private def unmarshal(xml: NodeSeq, 
                         dataXml: NodeSeq => NodeSeq, 
                         getData: NodeSeq => Option[I2b2ResultEnvelope], 
                         metadataXml: NodeSeq => NodeSeq, 
                         metadata: NodeSeq => QueryResult, 
                         xmlResultId: NodeSeq => Long): ReadResultResponse = {
    (for {
      data <- getData(dataXml(xml))
    } yield ReadResultResponse(xmlResultId(xml), metadata(metadataXml(xml)), data)).get
  }
}