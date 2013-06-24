package net.shrine.protocol

import net.shrine.util.XmlUtil
import net.shrine.util.SEnum
import net.shrine.serialization.XmlUnmarshaller
import net.shrine.serialization.I2b2Unmarshaller
import net.shrine.serialization.XmlUnmarshaller
import net.shrine.protocol.handlers.ReadI2b2AdminPreviousQueriesHandler
import scala.xml.NodeSeq
import scala.util.Try

/**
 * @author clint
 * @date Mar 29, 2013
 */
final case class ReadI2b2AdminPreviousQueriesRequest(
  override val projectId: String,
  override val waitTimeMs: Long,
  override val authn: AuthenticationInfo,
  username: String,
  searchString: String,
  maxResults: Int,
  sortOrder: ReadI2b2AdminPreviousQueriesRequest.SortOrder = ReadI2b2AdminPreviousQueriesRequest.SortOrder.Descending,
  searchStrategy: ReadI2b2AdminPreviousQueriesRequest.Strategy = ReadI2b2AdminPreviousQueriesRequest.Strategy.Contains,
  categoryToSearchWithin: ReadI2b2AdminPreviousQueriesRequest.Category = ReadI2b2AdminPreviousQueriesRequest.Category.Top
  ) extends ShrineRequest(projectId, waitTimeMs, authn) with CrcRequest with HandleableAdminShrineRequest {

  override val requestType = RequestType.ReadI2b2AdminPreviousQueriesRequest

  override def handleAdmin(handler: I2b2AdminRequestHandler, shouldBroadcast: Boolean): ShrineResponse = handler.readI2b2AdminPreviousQueries(this, shouldBroadcast)
  
  protected override def i2b2MessageBody = XmlUtil.stripWhitespace {
    <message_body>
      { i2b2PsmHeader }
      <ns4:get_name_info category={ categoryToSearchWithin.toString } max={ maxResults.toString }>
        <match_str strategy={ searchStrategy.toString }>{ searchString }</match_str>
        <user_id>{ username }</user_id>
        <ascending>{ sortOrder.isAscending }</ascending>
      </ns4:get_name_info>
    </message_body>
  }

  override def toXml = XmlUtil.stripWhitespace {
    <readAdminPreviousQueries>
      { headerFragment }
      <username>{ username }</username>
      <searchString>{ searchString }</searchString>
      <maxResults>{ maxResults }</maxResults>
      <sortOrder>{ sortOrder }</sortOrder>
      <categoryToSearchWithin>{ categoryToSearchWithin }</categoryToSearchWithin>
      <searchStrategy>{ searchStrategy }</searchStrategy>
    </readAdminPreviousQueries>
  }
}

object ReadI2b2AdminPreviousQueriesRequest extends ShrineRequestUnmarshaller[ReadI2b2AdminPreviousQueriesRequest] with I2b2Unmarshaller[ReadI2b2AdminPreviousQueriesRequest] {
  val allUsers = "@"
  
  def enumValue[T](enumCompanion: SEnum[T])(name: String): T = {
    //TODO: Remove unsafe get call
    enumCompanion.valueOf(name).get
  }

  override def fromXml(xml: NodeSeq): ReadI2b2AdminPreviousQueriesRequest = {
    def textIn(tagName: String) = (xml \ tagName).text.trim
    
    ReadI2b2AdminPreviousQueriesRequest(
      shrineProjectId(xml),
      shrineWaitTimeMs(xml),
      shrineAuthenticationInfo(xml),
      textIn("username"),
      textIn("searchString"),
      textIn("maxResults").toInt,
      enumValue(SortOrder)(textIn("sortOrder")),
      enumValue(Strategy)(textIn("searchStrategy")),
      enumValue(Category)(textIn("categoryToSearchWithin")))
  }

  override def fromI2b2(xml: NodeSeq): ReadI2b2AdminPreviousQueriesRequest = {
    def enumValueFrom[T](enumCompanion: SEnum[T])(elem: NodeSeq) = enumValue(enumCompanion)(elem.text)
    
    val messageBody = xml \ "message_body"
    
    val getNameInfo = messageBody \ "get_name_info"
    
    val matchStrElem = getNameInfo \ "match_str"
    
    ReadI2b2AdminPreviousQueriesRequest(
      i2b2ProjectId(xml),
      i2b2WaitTimeMs(xml),
      i2b2AuthenticationInfo(xml),
      (getNameInfo \ "user_id").text.trim,
      (getNameInfo \ "match_str").text.trim,
      (getNameInfo \ "@max").text.toInt,
      if((getNameInfo \ "ascending").text.toBoolean) SortOrder.Ascending else SortOrder.Descending,
      enumValueFrom(Strategy)(matchStrElem \ "@strategy"),
      enumValueFrom(Category)(getNameInfo \ "@category"))
  }

  final case class Category private[Category] (override val name: String) extends Category.Value

  object Category extends SEnum[Category] {
    val All = new Category("@")
    val Top = new Category("top")
    val Results = new Category("results")
    val Pdo = new Category("pdo")
  }

  final case class Strategy private[Strategy] (override val name: String, compare: (String, String) => Boolean) extends Strategy.Value {
    def isMatch(field: String, searchTerm: String) = compare(field, searchTerm)
  }

  object Strategy extends SEnum[Strategy] {
    val Exact = new Strategy("exact", _ == _)
    val Left = new Strategy("left", _.startsWith(_))
    val Right = new Strategy("right", _.endsWith(_))
    val Contains = new Strategy("contains", _.contains(_))
  }

  final case class SortOrder private[SortOrder] (override val name: String) extends SortOrder.Value {
    def isAscending = this == SortOrder.Ascending
    def isDescending = this == SortOrder.Descending
  }

  object SortOrder extends SEnum[SortOrder] {
    val Ascending = new SortOrder("ascending")
    val Descending = new SortOrder("descending")
  }
}