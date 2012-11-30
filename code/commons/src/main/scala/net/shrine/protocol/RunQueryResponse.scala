package net.shrine.protocol

import javax.xml.datatype.XMLGregorianCalendar
import org.spin.tools.NetworkTime.makeXMLGregorianCalendar
import xml.NodeSeq
import net.shrine.util.XmlUtil
import net.shrine.protocol.query.QueryDefinition
import scala.xml.Elem
import scala.xml.Atom
import net.shrine.serialization.{I2b2Unmarshaller, XmlUnmarshaller}

/**
 * @author Bill Simons
 * @date 4/15/11
 * @link http://cbmi.med.harvard.edu
 * 
 * NB: this is a case class to get a structural equality contract in hashCode and equals, mostly for testing
 */
final case class RunQueryResponse(
    override val queryId: Long,
    override val createDate: XMLGregorianCalendar,
    override val userId: String,
    override val groupId: String,
    override val requestXml: QueryDefinition,
    override val queryInstanceId: Long,
    override val results: Seq[QueryResult]) extends AbstractRunQueryResponse(queryId, createDate, userId, groupId, requestXml, queryInstanceId) {

  type ActualResponseType = RunQueryResponse
  
  override def withId(id: Long) = this.copy(queryId = id)

  override def withInstanceId(id: Long) = this.copy(queryInstanceId = id)

  override def withResults(seq: Seq[QueryResult]) = this.copy(results = seq)
}

object RunQueryResponse extends 
    AbstractRunQueryResponse.Companion[RunQueryResponse] with 
    I2b2Unmarshaller[RunQueryResponse] with 
    XmlUnmarshaller[RunQueryResponse] {
  
  override def fromI2b2(nodeSeq: NodeSeq) = unmarshalFromI2b2(nodeSeq)
  
  override def fromXml(nodeSeq: NodeSeq) = unmarshalFromXml(nodeSeq)
}