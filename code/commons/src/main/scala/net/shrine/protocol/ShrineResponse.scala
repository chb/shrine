package net.shrine.protocol

import net.shrine.serialization.XmlMarshaller
import net.shrine.serialization.XmlUnmarshaller
import scala.xml.NodeSeq
import net.shrine.serialization.I2b2Marshaller
import net.shrine.util.XmlUtil

/**
 * @author clint
 * @date Nov 5, 2012
 */
trait ShrineResponse extends XmlMarshaller with I2b2Marshaller {
  protected def i2b2MessageBody: NodeSeq

  protected def status = <status type="DONE">DONE</status>

  //TODO better xmlns strategy
  override def toI2b2: NodeSeq = XmlUtil.stripWhitespace(
    <ns4:response xmlns:ns2="http://www.i2b2.org/xsd/hive/pdo/1.1/" xmlns:ns3="http://www.i2b2.org/xsd/cell/crc/pdo/1.1/" xmlns:ns4="http://www.i2b2.org/xsd/hive/msg/1.1/" xmlns:ns5="http://www.i2b2.org/xsd/cell/crc/psm/1.1/" xmlns:ns6="http://www.i2b2.org/xsd/cell/pm/1.1/" xmlns:ns7="http://sheriff.shrine.net/" xmlns:ns8="http://www.i2b2.org/xsd/cell/crc/psm/querydefinition/1.1/" xmlns:ns9="http://www.i2b2.org/xsd/cell/crc/psm/analysisdefinition/1.1/" xmlns:ns10="http://www.i2b2.org/xsd/cell/ont/1.1/" xmlns:ns11="http://www.i2b2.org/xsd/hive/msg/result/1.1/">
      <message_header>
        <i2b2_version_compatible>1.1</i2b2_version_compatible>
        <hl7_version_compatible>2.4</hl7_version_compatible>
        <sending_application>
          <application_name>SHRINE</application_name>
          <application_version>1.3-compatible</application_version>
        </sending_application>
        <sending_facility>
          <facility_name>SHRINE</facility_name>
        </sending_facility>
        <datetime_of_message>2011-04-08T16:21:12.251-04:00</datetime_of_message>
        <security/>
        <project_id xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:nil="true"/>
      </message_header>
      <response_header>
        <result_status>
          {status}
        </result_status>
      </response_header>
      <message_body>
        {i2b2MessageBody}
      </message_body>
    </ns4:response>
  )
}

object ShrineResponse extends XmlUnmarshaller[Option[ShrineResponse]] {
  override def fromXml(nodeSeq: NodeSeq) = {
    //.headOption handles possibly-empty NodeSeqs
    //.collect returns a Some of the result of the pattern match, or None if no cases match
    nodeSeq match {
      case null => None
      case _ => nodeSeq.headOption.map(_.label).collect {
        case "deleteQueryResponse" => DeleteQueryResponse.fromXml(nodeSeq)
        case "readInstanceResultsResponse" => ReadInstanceResultsResponse.fromXml(nodeSeq)
        case "readPreviousQueriesResponse" => ReadPreviousQueriesResponse.fromXml(nodeSeq)
        case "readQueryDefinitionResponse" => ReadQueryDefinitionResponse.fromXml(nodeSeq)
        case "readQueryInstancesResponse" => ReadQueryInstancesResponse.fromXml(nodeSeq)
        case "renameQueryResponse" => RenameQueryResponse.fromXml(nodeSeq)
        case "runQueryResponse" => RunQueryResponse.fromXml(nodeSeq)
        case "errorResponse" => ErrorResponse.fromXml(nodeSeq)
      }
    }
  }
}