package net.shrine.webclient.server

import com.google.gwt.user.server.rpc.RemoteServiceServlet
import net.shrine.webclient.client.QueryService
import net.shrine.protocol.AuthenticationInfo
import net.shrine.protocol.ResultOutputType
import net.shrine.protocol.Credential
import net.shrine.protocol.RunQueryResponse
import net.shrine.service.ShrineClient
import net.shrine.service.JerseyShrineClient
import net.shrine.protocol.query.QueryDefinition
import net.shrine.protocol.query.Expression
import java.util.{Map => JMap}
import java.util.{HashMap => JHashMap}
import java.lang.{Long => JLong}
import java.lang.{Integer => JInt}
import net.shrine.webclient.client.domain.IntWrapper
import net.shrine.protocol.QueryResult

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

final class QueryServiceImpl(private[this] val client: ShrineClient) extends RemoteServiceServlet with QueryService {

  //Needed so this class can be instantiated by an app server
  def this() = this(new JerseyShrineClient(QueryServiceImpl.Urls.shrineDev1, QueryServiceImpl.Defaults.projectId, QueryServiceImpl.Defaults.auth, true))

  private[this] def uuid = java.util.UUID.randomUUID.toString

  import Expression.fromXml
  import QueryServiceImpl._
  
  private def doQuery(expr: String) = client.runQuery(Defaults.topicId, Defaults.outputTypes, QueryDefinition(uuid, fromXml(expr)))
  
  override def queryForBreakdown(expr: String): JHashMap[String, IntWrapper] = {
    val response: RunQueryResponse = doQuery(expr)
    
    def toJInt(l: Long) = new IntWrapper(l.toInt)
    
    def toNamedCount(result: QueryResult) = (result.description.getOrElse("Unknown Institution"), toJInt(result.setSize))
    
    val breakDown = Map.empty ++ response.results.map(toNamedCount)
    
    Helpers.toJavaMap(breakDown)
  }
}