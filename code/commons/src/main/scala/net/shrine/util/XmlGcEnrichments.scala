package net.shrine.util

import javax.xml.datatype.XMLGregorianCalendar
import org.spin.tools.NetworkTime

/**
 * @author clint
 * @date Dec 21, 2012
 */
object XmlGcEnrichments {
  final case class Milliseconds(value: Long)
  
  //TODO: replace with inline implicit class in Scala 2.10
  final class EnrichedXmlGc(xmlGc: XMLGregorianCalendar) {
    def +(millis: Milliseconds): XMLGregorianCalendar = (new NetworkTime(xmlGc)).addMilliseconds(millis.value).getXMLGregorianCalendar
  }
  
  //TODO: replace with inline implicit class in Scala 2.10
  final class EnrichedLong(l: Long) {
    def milliseconds = Milliseconds(l)
  }
  
  implicit def long2EnrichedLong(l: Long): EnrichedLong = new EnrichedLong(l)
  
  implicit def xmlGc2EnrichedXmlGc(xmlGc: XMLGregorianCalendar): EnrichedXmlGc = new EnrichedXmlGc(xmlGc)
}