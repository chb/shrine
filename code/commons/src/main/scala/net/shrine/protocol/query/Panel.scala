package net.shrine.protocol.query

import javax.xml.datatype.XMLGregorianCalendar
import scala.xml.NodeSeq
import net.shrine.util.XmlUtil
import org.spin.tools.NetworkTime
import net.liftweb.json.JsonDSL._
import net.shrine.serialization.{ JsonUnmarshaller, I2b2Marshaller }
import net.liftweb.json.JsonAST._
import scala.None
import net.liftweb.json.DefaultFormats
import net.shrine.util.Try

/**
 *
 * @author Clint Gilbert
 * @date Jan 25, 2012
 *
 * @link http://cbmi.med.harvard.edu
 *
 * This software is licensed under the LGPL
 * @link http://www.gnu.org/licenses/lgpl.html
 *
 */
final case class Panel(
  number: Int,
  inverted: Boolean,
  minOccurrences: Int,
  start: Option[XMLGregorianCalendar],
  end: Option[XMLGregorianCalendar],
  terms: Seq[SimpleExpression]) extends I2b2Marshaller {

  require(!terms.isEmpty)

  import Panel._

  def invert = this.copy(inverted = !this.inverted)

  def withStart(startDate: Option[XMLGregorianCalendar]) = this.copy(start = startDate)

  def withEnd(endDate: Option[XMLGregorianCalendar]) = this.copy(end = endDate)

  def withMinOccurrences(min: Int) = this.copy(minOccurrences = min)

  //TODO: Do dates have to be in UTC?  Ones sent by web client seem to be
  //TODO: <date_{from,to}> vs <panel_date_{from,to}>?? web client uses both
  //TODO: <class>ENC</class> on items: is it ever anything else?
  //TODO: Are <item_name>, <tooltip>, <item_icon>, and <item_is_synonym> on items needed?
  override def toI2b2: NodeSeq = XmlUtil.stripWhitespace(
    <panel>
      <panel_number>{ number }</panel_number>
      { start.map(s => <panel_date_from>{ s.toString }</panel_date_from>).getOrElse(Nil) }
      { end.map(e => <panel_date_to>{ e.toString }</panel_date_to>).getOrElse(Nil) }
      <invert>{ if (inverted) 1 else 0 }</invert>
      <total_item_occurrences>{ minOccurrences }</total_item_occurrences>
      {
        terms.map { term =>
          <item>
            <hlevel>{ term.computeHLevel.getOrElse(0) }</hlevel>
            <item_name>{ term.value }</item_name>
            <item_key>{ term.value }</item_key>
            <tooltip>{ term.value }</tooltip>
            <class>ENC</class>
            <constrain_by_date>
              { start.map(s => <date_from>{ s.toString }</date_from>).orNull }
              { end.map(e => <date_to>{ e.toString }</date_to>).orNull }
            </constrain_by_date>
            <item_icon>LA</item_icon>
            <item_is_synonym>false</item_is_synonym>
          </item>
        }
      }
    </panel>)

  def toExpression: Expression = {
    def limit(expr: Expression) = if (minOccurrences != 0) OccuranceLimited(minOccurrences, expr) else expr

    def dateBound(expr: Expression): Expression = {
      if (start.isDefined || end.isDefined) DateBounded(start, end, expr) else expr
    }

    def negate(expr: Expression) = if (inverted) Not(expr) else expr

    limit(dateBound(negate(Or(terms: _*)))).normalize
  }
}

object Panel {
  def fromI2b2(nodeSeq: NodeSeq): Try[Panel] = {
    import NetworkTime.makeXMLGregorianCalendar

    def toXmlGcOption(xml: NodeSeq) = xml.headOption.view.map(_.text).map(makeXMLGregorianCalendar).headOption

    for {
      outerTag <- Try(nodeSeq.head)
      number <- Try((outerTag \ "panel_number").text.toInt)
      inverted <- Try((outerTag \ "invert").text.toInt == 1)
      minOccurrences <- Try((outerTag \ "total_item_occurrences").text.toInt)
      start = toXmlGcOption(outerTag \ "panel_date_from")
      end = toXmlGcOption(outerTag \ "panel_date_to")
      terms = (outerTag \ "item").view.map(_ \ "item_key").map(_.text).map {
        case Query.prefixRegex(id) => Query(id)
        case x => Term(x)
      }.force
    } yield Panel(number, inverted, minOccurrences, start, end, terms)
  }
}