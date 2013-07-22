package net.shrine.protocol.query

import javax.xml.datatype.XMLGregorianCalendar
import scala.xml.NodeSeq
import scala.xml.Utility
import net.shrine.protocol.I2b2Marshaller
import net.shrine.util.XmlUtil

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
private[query] final case class Panel(
  number: Int,
  inverted: Boolean,
  minOccurrences: Int,
  start: Option[XMLGregorianCalendar],
  end: Option[XMLGregorianCalendar],
  terms: Seq[Term]) extends I2b2Marshaller {

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
            <hlevel>{ computeHLevel(term) }</hlevel>
            <item_name>{ term.value }</item_name>
            <item_key>{ term.value }</item_key>
            <tooltip>{ term.value }</tooltip>
            <class>ENC</class>
            <constrain_by_date>
              { start.map(s => <date_from>{ s.toString }</date_from>).getOrElse(Nil) }
              { end.map(e => <date_to>{ e.toString }</date_to>).getOrElse(Nil) }
            </constrain_by_date>
            <item_icon>LA</item_icon>
            <item_is_synonym>false</item_is_synonym>
          </item>
        }
      }
    </panel>)
}

private[query] object Panel {
  def computeHLevel(term: Term): Int = {
    //Super-dumb way: calculate nesting level by dropping prefix and counting \'s
    term.value.drop("\\\\SHRINE\\SHRINE\\".length).count(_ == '\\')
  }
}