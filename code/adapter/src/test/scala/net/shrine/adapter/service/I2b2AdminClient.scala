package net.shrine.adapter.service

import net.shrine.protocol.ReadI2b2AdminPreviousQueriesRequest
import net.shrine.protocol.ReadPreviousQueriesResponse
import net.shrine.protocol.ReadQueryDefinitionRequest
import net.shrine.protocol.ReadQueryDefinitionResponse
import net.shrine.util.HttpClient
import net.shrine.protocol.ShrineRequest
import net.shrine.protocol.ShrineResponse
import net.shrine.protocol.I2b2SerializableValidator
import net.shrine.serialization.I2b2Marshaller
import net.shrine.serialization.I2b2Unmarshaller
import scala.util.Try
import net.shrine.protocol.ErrorResponse
import scala.xml.NodeSeq

/**
 * @author clint
 * @date Apr 10, 2013
 */
final case class I2b2AdminClient(url: String, httpClient: HttpClient) {
  import I2b2AdminClient._
  
  def readI2b2AdminPreviousQueries(request: ReadI2b2AdminPreviousQueriesRequest) = doCall(request, ReadPreviousQueriesResponse orElse ErrorResponse)

  def readQueryDefinition(request: ReadQueryDefinitionRequest): ShrineResponse = doCall(request, ReadQueryDefinitionResponse orElse ErrorResponse)

  private def doCall(request: I2b2Marshaller, unmarshaller: I2b2Unmarshaller[ShrineResponse]): ShrineResponse = {
    unmarshaller.fromI2b2(httpClient.post(request.toI2b2String, url))
  }
}

object I2b2AdminClient {
  private final class FallsBackI2b2Unmarshaller[R <: ShrineResponse, S <: ShrineResponse](lhs: I2b2Unmarshaller[R], rhs: I2b2Unmarshaller[S]) extends I2b2Unmarshaller[ShrineResponse] {
    override def fromI2b2(nodeSeq: NodeSeq): ShrineResponse = Try(lhs.fromI2b2(nodeSeq)).getOrElse(rhs.fromI2b2(nodeSeq))
  } 
  
  private implicit class OrElse[R <: ShrineResponse](val lhs: I2b2Unmarshaller[R]) extends AnyVal {
    def orElse[S <: ShrineResponse](rhs: I2b2Unmarshaller[S]): I2b2Unmarshaller[ShrineResponse] = new FallsBackI2b2Unmarshaller(lhs, rhs)
  }
}