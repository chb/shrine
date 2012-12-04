package net.shrine.webclient.server

import scala.collection.JavaConverters.mapAsJavaMapConverter

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Service

import net.shrine.client.ShrineClient
import net.shrine.protocol.AggregatedRunQueryResponse
import net.shrine.protocol.QueryResult
import net.shrine.protocol.ResultOutputType
import net.shrine.protocol.ResultOutputType.PATIENT_COUNT_XML
import net.shrine.protocol.query.Expression
import net.shrine.protocol.query.QueryDefinition
import net.shrine.webclient.shared.domain.MultiInstitutionQueryResult

/**
 * @author clint
 * @date Mar 23, 2012
 */
object QueryServiceImpl {
  object Defaults {
    val topicId = "4" //Magic

    import ResultOutputType._
      
    val outputTypes = Set(PATIENT_COUNT_XML) ++ ResultOutputType.values.filter(_.isBreakdown)
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

  override def performQuery(expr: String): MultiInstitutionQueryResult = {
    def toInstName(description: Option[String]) = description.getOrElse("Unknown Institution")
    
    import Helpers._
    
    def toNamedResult(result: QueryResult) = (toInstName(result.description), makeSingleInstitutionQueryResult(result))

    val response: AggregatedRunQueryResponse = doQuery(expr)
    
    val results = response.results.map(toNamedResult).toMap
    
    import scala.collection.JavaConverters._
    
    new MultiInstitutionQueryResult(results.asJava)
  }
}