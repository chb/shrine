package net.shrine.protocol

import net.shrine.protocol.CRCRequestType.QueryDefinitionRequestType
import xml._
import net.shrine.util.XmlUtil
import net.shrine.protocol.query.QueryDefinition
import net.shrine.serialization.I2b2Unmarshaller
import net.shrine.util.AsExtractor

/**
 * @author Bill Simons
 * @date 3/9/11
 * @link http://cbmi.med.harvard.edu
 * @link http://chip.org
 *       <p/>
 *       NOTICE: This software comes with NO guarantees whatsoever and is
 *       licensed as Lgpl Open Source
 * @link http://www.gnu.org/licenses/lgpl.html
 *
 * NB: this is a case class to get a structural equality contract in hashCode and equals, mostly for testing
 */
final case class RunQueryRequest(
  override val projectId: String,
  override val waitTimeMs: Long,
  override val authn: AuthenticationInfo,
  val topicId: String,
  val outputTypes: Set[ResultOutputType],
  val queryDefinition: QueryDefinition) extends ShrineRequest(projectId, waitTimeMs, authn) with CrcRequest with TranslatableRequest[RunQueryRequest] {

  val requestType = QueryDefinitionRequestType

  def toXml = XmlUtil.stripWhitespace(
    <runQuery>
      { headerFragment }
      <topicId>{ topicId }</topicId>
      <outputTypes>
        {
          outputTypes map { x =>
            <outputType>{ new Text(x.toString) }</outputType>
          }
        }
      </outputTypes>
      { queryDefinition.toXml }
    </runQuery>)

  def handle(handler: ShrineRequestHandler) = {
    handler.runQuery(this)
  }

  protected def i2b2MessageBody = XmlUtil.stripWhitespace(
    <message_body>
      { i2b2PsmHeaderWithDomain }
      <ns4:request xsi:type="ns4:query_definition_requestType" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
        { queryDefinition.toI2b2 }
        <result_output_list>
          {
            for {
              (outputType, i) <- outputTypes.zipWithIndex
              priorityIndex = i + 1
            } yield {
              <result_output priority_index={ priorityIndex.toString } name={ outputType.toString.toLowerCase }/>
            }
          }
        </result_output_list>
      </ns4:request>
      <shrine><queryTopicID>{ topicId }</queryTopicID></shrine>
    </message_body>)

  def withProject(proj: String) = this.copy(projectId = proj)

  def withAuthn(ai: AuthenticationInfo) = this.copy(authn = ai)

  def withQueryDefinition(qDef: QueryDefinition) = this.copy(queryDefinition = qDef)
}

object RunQueryRequest extends I2b2Unmarshaller[RunQueryRequest] with ShrineRequestUnmarshaller[RunQueryRequest] {
  
  val neededI2b2Namespace = "http://www.i2b2.org/xsd/cell/crc/psm/1.1/"

  def fromI2b2(nodeSeq: NodeSeq): RunQueryRequest = {
    val queryDefNode = nodeSeq \ "message_body" \ "request" \ "query_definition"

    val queryDefXml = queryDefNode.head match {
      //NB: elem.scope.getPrefix(neededI2b2Namespace) will return null if elem isn't part of a larger XML chunk that has
      //the http://www.i2b2.org/xsd/cell/crc/psm/1.1/ declared
      case elem: Elem => elem.copy(elem.scope.getPrefix(neededI2b2Namespace))
      case _ => throw new Exception("When unmarshalling a RunQueryRequest, encountered unexpected XML: '" + queryDefNode + "', <query_definition> might be missing.")
    }

    new RunQueryRequest(
      i2b2ProjectId(nodeSeq),
      i2b2WaitTimeMs(nodeSeq),
      i2b2AuthenticationInfo(nodeSeq),
      (nodeSeq \ "message_body" \ "shrine" \ "queryTopicID").text,
      determineI2b2OutputTypes(nodeSeq \ "message_body" \ "request" \ "result_output_list"),
      QueryDefinition.fromI2b2(queryDefXml).get) //TODO: Remove unsafe get call
  }

  private def determineI2b2OutputTypes(nodeSeq: NodeSeq): Set[ResultOutputType] = {
    import ResultOutputType._

    def matches(attribute: NodeSeq, resultOutputType: ResultOutputType): Boolean = {
      attribute.toString.equalsIgnoreCase(resultOutputType.name)
    }

    val sequence = (nodeSeq \ "result_output") collect {
      case x if matches(x \ "@name", PATIENTSET) => PATIENTSET
      case x if matches(x \ "@name", PATIENT_COUNT_XML) => PATIENT_COUNT_XML
    }

    sequence.toSet
  }

  private def determineShrineOutputTypes(nodeSeq: NodeSeq): Set[ResultOutputType] = {
    (nodeSeq \ "outputType").map(x => ResultOutputType.valueOf(x.text)).toSet
  }

  def fromXml(nodeSeq: NodeSeq) = {
    new RunQueryRequest(
      shrineProjectId(nodeSeq),
      shrineWaitTimeMs(nodeSeq),
      shrineAuthenticationInfo(nodeSeq),
      (nodeSeq \ "topicId").text,
      determineShrineOutputTypes(nodeSeq \ "outputTypes"),
      QueryDefinition.fromXml(nodeSeq \ "queryDefinition").get) //TODO: Remove unsafe get call
  }
}