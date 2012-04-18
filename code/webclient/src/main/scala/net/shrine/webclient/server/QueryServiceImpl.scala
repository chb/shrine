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

final class QueryServiceImpl extends RemoteServiceServlet with QueryService {
  import QueryServiceImpl._

  private[this] def client: ShrineClient = new JerseyShrineClient(Urls.shrineDev1, Defaults.projectId, Defaults.auth, true)

  private[this] def uuid = java.util.UUID.randomUUID.toString

  import Expression.fromXml
  
  private def doQuery(expr: String) = client.runQuery(Defaults.topicId, Defaults.outputTypes, QueryDefinition(uuid, fromXml(expr)))
  
  override def query(expr: String): Int = {
    val response: RunQueryResponse = doQuery(expr) 

    response.results.map(_.setSize).sum.toInt
  }
  
  override def queryForBreakdown(expr: String): JHashMap[String, IntWrapper] = {
    val response: RunQueryResponse = doQuery(expr)
    
    def toJInt(l: Long) = new IntWrapper(l.toInt)
    
    val breakDown = Map.empty ++ response.results.map(result => (result.description.getOrElse("Unknown Institution"), toJInt(result.setSize)))
    
    println(breakDown)
    
    val javaBreakDown = Helpers.toJava(breakDown)
    
    println(javaBreakDown)
    
    javaBreakDown
  }
}