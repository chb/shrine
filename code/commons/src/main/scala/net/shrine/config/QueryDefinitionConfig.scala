package net.shrine.config

import net.liftweb.json._
import org.spin.tools.NetworkTime
import net.shrine.protocol.query.{Term, And, QueryDefinition, Panel}

/**
 * @author Bill Simons
 * @date 3/28/12
 * @link http://cbmi.med.harvard.edu
 * @link http://chip.org
 *       <p/>
 *       NOTICE: This software comes with NO guarantees whatsoever and is
 *       licensed as Lgpl Open Source
 * @link http://www.gnu.org/licenses/lgpl.html
 */
object QueryDefinitionConfig {

  def parseQueryDefinitionConfig(s: String) = {
    val json = parse(s)
    (json \ "queryDefinitions").children.map(parseQueryDefinition(_))
  }

  def parseQueryDefinition(json: JValue) = {
    implicit val formats = DefaultFormats

    val name = (json \ "name").extract[String]
    val panels = (json \ "panels").children.zipWithIndex.map {
      case (panel: JValue, index: Int) => parsePanel(index + 1, panel)
    }
    val exprs = panels.map(_.toExpression)
    val consolidatedExpr = if(exprs.size == 1) exprs.head else And(exprs: _*)
    QueryDefinition(name, consolidatedExpr.normalize)
  }

  def parsePanel(panelNumber: Int, json: JValue): Panel = {
    implicit val formats = DefaultFormats

    val inverted = (json \ "invert").extractOpt[Boolean].getOrElse(false)
    val min = (json \ "minOccurrences").extractOpt[Int].getOrElse(1)
    val start = (json \ "start").extractOpt[String].map(NetworkTime.makeXMLGregorianCalendar(_))
    val end = (json \ "end").extractOpt[String].map(NetworkTime.makeXMLGregorianCalendar(_))
    val terms = (json \ "terms").extract[List[Term]]

    new Panel(panelNumber, inverted, min, start, end, terms)
  }

  def loadQueryDefinitionConfig(fileName: String): java.util.Iterator[QueryDefinition] = {
    import scala.collection.JavaConverters._

    val source = scala.io.Source.fromFile(fileName)
    val lines = source.mkString
    source.close()
    parseQueryDefinitionConfig(lines).toIterator.asJava
  }

}