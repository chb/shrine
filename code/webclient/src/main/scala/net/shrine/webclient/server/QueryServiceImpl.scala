package net.shrine.webclient.server

import net.shrine.protocol.AuthenticationInfo
import net.shrine.protocol.Credential
import net.shrine.protocol.QueryResult
import net.shrine.protocol.ResultOutputType
import net.shrine.protocol.RunQueryResponse
import net.shrine.protocol.query.Expression.fromXml
import net.shrine.protocol.query.QueryDefinition
import net.shrine.service.JerseyShrineClient
import net.shrine.service.ShrineClient
import net.shrine.protocol.query.Expression
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.{Service, Component}
import org.springframework.context.annotation.Scope

/**
 * @author clint
 * @date Mar 23, 2012
 */
object QueryServiceImpl {
  object Defaults {
    val topicId = "4" //Magic

    val outputTypes = Set(ResultOutputType.PATIENT_COUNT_XML)
  }
}

@Service
@Scope("singleton")
final class QueryServiceImpl @Autowired()(private[this] val client: ShrineClient) extends QueryService {
  override def toString = "QueryServiceImpl(" + client + ")"
  
  private[this] def uuid = java.util.UUID.randomUUID.toString

  import Expression.fromXml
  import QueryServiceImpl._

  //TODO: remove fragile .get call
  private def doQuery(expr: String) = client.runQuery(Defaults.topicId, Defaults.outputTypes, QueryDefinition(uuid, fromXml(expr).get))

  override def queryForBreakdown(expr: String): MultiInstitutionQueryResult = {
    val response: RunQueryResponse = doQuery(expr)

    def toNamedCount(result: QueryResult) = (result.description.getOrElse("Unknown Institution"), result.setSize.toInt)

    val results = response.results.map(toNamedCount).toMap
    
    MultiInstitutionQueryResult(results)
  }
}