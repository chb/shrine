package net.shrine.adapter.components

import net.shrine.adapter.dao.AdapterDao
import net.shrine.protocol.ShrineResponse
import net.shrine.protocol.ReadQueryDefinitionRequest
import net.shrine.protocol.ReadQueryDefinitionResponse
import net.shrine.protocol.ErrorResponse
import net.shrine.protocol.query.QueryDefinition

/**
 * @author clint
 * @date Apr 4, 2013
 */
trait QueryDefinitionSourceComponent {
  val dao: AdapterDao

  protected object QueryDefinitions {
    def get(request: ReadQueryDefinitionRequest): ShrineResponse = {
      val resultOption = for {
        shrineQuery <- dao.findQueryByNetworkId(request.queryId)
      } yield {
        ReadQueryDefinitionResponse(
          shrineQuery.networkId,
          shrineQuery.name,
          shrineQuery.username,
          shrineQuery.dateCreated,
          //TODO: I2b2 or Shrine format?
          QueryDefinition(shrineQuery.name, shrineQuery.queryExpr).toI2b2String)
      }

      resultOption.getOrElse(ErrorResponse(s"Couldn't find query with network id: ${request.queryId}"))
    }
  }
}