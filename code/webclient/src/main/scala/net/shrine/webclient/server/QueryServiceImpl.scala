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
import net.shrine.protocol.I2b2ResultEnvelope
import net.shrine.webclient.shared.domain.MultiInstitutionQueryResult
import net.shrine.webclient.shared.domain.SingleInstitutionQueryResult

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
    import Helpers._
    
    def toNamedResult(result: QueryResult) = (result.description.getOrElse("Unknown Institution"), makeSingleInstitutionQueryResult(result))

    import ResultOutputType._
    
    def addDummyBreakdowns(instResult: SingleInstitutionQueryResult): SingleInstitutionQueryResult = {
      val breakdowns = makeBreakdownsByTypeMap(Seq(new I2b2ResultEnvelope(PATIENT_GENDER_COUNT_XML, "foo" -> 123, "bar" -> 42),
                                                   new I2b2ResultEnvelope(PATIENT_AGE_COUNT_XML, "blarg" -> 123, "baz" -> 9876)))
      
      new SingleInstitutionQueryResult(instResult.getCount, breakdowns)
    }
    
    val response: RunQueryResponse = doQuery(expr)
    
    val results = response.results.map(toNamedResult).toMap
    
    //MultiInstitutionQueryResult(results)
    
    //TODO: NB: results with dummy breakdowns, until new shrine-dev2 is online  

    import scala.collection.JavaConverters._
    
    new MultiInstitutionQueryResult(results.mapValues(addDummyBreakdowns).asJava)
  }
}