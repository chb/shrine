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

/**
 * @author clint
 * @date Mar 23, 2012
 */
object QueryServiceImpl {
  object Urls {
    val shrineDev1 = "https://shrine-dev1.chip.org:6060/shrine-cell/rest/"
    val shrineDev2 = "https://shrine-dev2.chip.org:6060/shrine-cell/rest/"
  }

  object Defaults {
    val topicId = "4" //Magic
    val projectId = "SHRINE"
    val domain = "i2b2demo"

    val auth = AuthenticationInfo(domain, "bsimons", Credential("testtest", true))
    val outputTypes = Set(ResultOutputType.PATIENT_COUNT_XML).toSet
  }
}

final class QueryServiceImpl(private[this] val client: ShrineClient) extends QueryService {
  //Needed so this class can be instantiated by an app server
  def this() = this(new JerseyShrineClient(QueryServiceImpl.Urls.shrineDev1, QueryServiceImpl.Defaults.projectId, QueryServiceImpl.Defaults.auth, true))

  private[this] def uuid = java.util.UUID.randomUUID.toString

  import Expression.fromXml
  import QueryServiceImpl._

  private def doQuery(expr: String) = client.runQuery(Defaults.topicId, Defaults.outputTypes, QueryDefinition(uuid, fromXml(expr)))

  override def queryForBreakdown(expr: String): Map[String, Int] = {
    val response: RunQueryResponse = doQuery(expr)

    def toNamedCount(result: QueryResult) = (result.description.getOrElse("Unknown Institution"), result.setSize.toInt)

    Map.empty ++ response.results.map(toNamedCount)
  }
}