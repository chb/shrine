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

/**
 * @author clint
 * @date Apr 10, 2013
 */
final case class I2b2AdminClient(url: String, httpClient: HttpClient) {
  def readI2b2AdminPreviousQueries(request: ReadI2b2AdminPreviousQueriesRequest) = doCall(request, ReadPreviousQueriesResponse)

  def readQueryDefinition(request: ReadQueryDefinitionRequest) = doCall(request, ReadQueryDefinitionResponse)

  private def doCall[Resp <: ShrineResponse](request: I2b2Marshaller, unmarshaller: I2b2Unmarshaller[Resp]): Resp = {
    unmarshaller.fromI2b2(httpClient.post(request.toI2b2String, url))
  }
}