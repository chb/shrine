package net.shrine.protocol

import net.shrine.util.XmlUtil
import net.shrine.util.SEnum
import net.shrine.serialization.XmlUnmarshaller
import net.shrine.serialization.I2b2Unmarshaller
import net.shrine.serialization.XmlUnmarshaller
import net.shrine.protocol.handlers.ReadI2b2AdminPreviousQueriesHandler
import scala.xml.NodeSeq

/**
 * @author clint
 * @date Mar 29, 2013
 */
final case class ReadI2b2AdminPreviousQueriesRequest(
  override val projectId: String,
  override val waitTimeMs: Long,
  override val authn: AuthenticationInfo,
  searchString: String,
  maxResults: Int,
  sortOrder: ReadI2b2AdminPreviousQueriesRequest.SortOrder = ReadI2b2AdminPreviousQueriesRequest.SortOrder.Descending,
  categoryToSearchWithin: ReadI2b2AdminPreviousQueriesRequest.Category = ReadI2b2AdminPreviousQueriesRequest.Category.Top,
  searchStrategy: ReadI2b2AdminPreviousQueriesRequest.Strategy = ReadI2b2AdminPreviousQueriesRequest.Strategy.Contains) extends DoubleDispatchingShrineRequest(projectId, waitTimeMs, authn) with CrcRequest {

  override val requestType = RequestType.ReadI2b2AdminPreviousQueriesRequest

  override type Handler = I2b2AdminRequestHandler 

  override def handle(handler: Handler, shouldBroadcast: Boolean): ShrineResponse = handler.readI2b2AdminPreviousQueries(this, shouldBroadcast)
  
  final override def isHandledBy[T : Manifest] = manifest[T].runtimeClass.isAssignableFrom(classOf[I2b2AdminRequestHandler])
  
  protected override def i2b2MessageBody = XmlUtil.stripWhitespace {
    <message_body>
      { i2b2PsmHeader }
      <ns4:get_name_info category={ categoryToSearchWithin.toString } max={ maxResults.toString }>
        <match_str strategy={ searchStrategy.toString }>{ searchString }</match_str>
        <ascending>{ sortOrder.isAscending }</ascending>
      </ns4:get_name_info>
    </message_body>
  }

  override def toXml = XmlUtil.stripWhitespace {
    <readAdminPreviousQueries>
      { headerFragment }
      <searchString>{ searchString }</searchString>
      <maxResults>{ maxResults }</maxResults>
      <sortOrder>{ sortOrder }</sortOrder>
      <categoryToSearchWithin>{ categoryToSearchWithin }</categoryToSearchWithin>
      <searchStrategy>{ searchStrategy }</searchStrategy>
    </readAdminPreviousQueries>
  }
}

object ReadI2b2AdminPreviousQueriesRequest extends ShrineRequestUnmarshaller[ReadI2b2AdminPreviousQueriesRequest] with I2b2Unmarshaller[ReadI2b2AdminPreviousQueriesRequest] {
  private def textIn(xml: NodeSeq)(tagName: String): String = (xml \ tagName).text.trim
  
  private def textInAttr(xml: NodeSeq)(tagName: String, attribute: String): String = (xml \ tagName).text.trim

  def enumValue[T](enumCompanion: SEnum[T])(name: String): T = {
    //TODO: Remove unsafe get call
    enumCompanion.valueOf(name).get
  }

  override def fromXml(xml: NodeSeq): ReadI2b2AdminPreviousQueriesRequest = {
    ReadI2b2AdminPreviousQueriesRequest(
      shrineProjectId(xml),
      shrineWaitTimeMs(xml),
      shrineAuthenticationInfo(xml),
      textIn(xml)("searchString"),
      textIn(xml)("maxResults").toInt,
      enumValue(SortOrder)(textIn(xml)("sortOrder")),
      enumValue(Category)(textIn(xml)("categoryToSearchWithin")),
      enumValue(Strategy)(textIn(xml)("searchStrategy")))
  }

  override def fromI2b2(xml: NodeSeq): ReadI2b2AdminPreviousQueriesRequest = {
    def enumValueFromAttr[T](enumCompanion: SEnum[T])(tagName: String, attribute: String) = enumValue(enumCompanion)((xml \ tagName \ s"@$attribute").text)
    
    ReadI2b2AdminPreviousQueriesRequest(
      i2b2ProjectId(xml),
      i2b2WaitTimeMs(xml),
      i2b2AuthenticationInfo(xml),
      textIn(xml)("match_str"),
      textInAttr(xml)("get_name_info", "max").toInt,
      if(textIn(xml)("ascending").toBoolean) SortOrder.Ascending else SortOrder.Descending,
      enumValueFromAttr(Category)("get_name_info", "category"),
      enumValueFromAttr(Strategy)("match_str", "strategy"))
  }

  sealed class Category(override val name: String) extends Category.Value

  object Category extends SEnum[Category] {
    val All = new Category("@")
    val Top = new Category("top")
    val Results = new Category("results")
    val Pdo = new Category("pdo")
  }

  sealed class Strategy(override val name: String) extends Strategy.Value

  object Strategy extends SEnum[Strategy] {
    val Exact = new Strategy("exact")
    val Left = new Strategy("left")
    val Right = new Strategy("right")
    val Contains = new Strategy("contains")
  }

  sealed class SortOrder(override val name: String) extends SortOrder.Value {
    def isAscending = this == SortOrder.Ascending
    def isDescending = this == SortOrder.Descending
  }

  object SortOrder extends SEnum[SortOrder] {
    val Ascending = new SortOrder("ascending")
    val Descending = new SortOrder("descending")
  }
}