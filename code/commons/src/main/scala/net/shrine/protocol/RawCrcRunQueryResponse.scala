package net.shrine.protocol

import javax.xml.datatype.XMLGregorianCalendar
import net.shrine.protocol.query.QueryDefinition
import scala.xml.NodeSeq
import net.shrine.serialization.{I2b2Unmarshaller, XmlUnmarshaller}

/**
 * @author clint
 * @date Nov 30, 2012
 */
final case class RawCrcRunQueryResponse(
    override val queryId: Long,
    override val createDate: XMLGregorianCalendar,
    override val userId: String,
    override val groupId: String,
    override val requestXml: QueryDefinition,
    override val queryInstanceId: Long,
    val singleNodeResults: Map[ResultOutputType, Seq[QueryResult]]) extends AbstractRunQueryResponse("rawCrcRunQueryResponse", queryId, createDate, userId, groupId, requestXml, queryInstanceId) {

  override type ActualResponseType = RawCrcRunQueryResponse
  
  override def withId(id: Long) = this.copy(queryId = id)

  override def withInstanceId(id: Long) = this.copy(queryInstanceId = id)
  
  override def results = singleNodeResults.values.flatten.toSeq
  
  //NB: Will fail loudly if no PATIENT_COUNT_XML QueryResult is present
  def toRunQueryResponse = RunQueryResponse(queryId, createDate, userId, groupId, requestXml, queryInstanceId, singleNodeResults(ResultOutputType.PATIENT_COUNT_XML).head)
  
  private def clearResults = this.copy(singleNodeResults = Map.empty)
  
  import RawCrcRunQueryResponse._
  
  def withResults(results: Iterable[QueryResult]): RawCrcRunQueryResponse = this.copy(singleNodeResults = toQueryResultMap(results))
}

object RawCrcRunQueryResponse extends AbstractRunQueryResponse.Companion[RawCrcRunQueryResponse] {
  import ResultOutputType._
  
  def toQueryResultMap(results: Iterable[QueryResult]): Map[ResultOutputType, Seq[QueryResult]] = {
    results.groupBy(_.resultType.getOrElse(ERROR)).mapValues(_.toSeq)
  }
}