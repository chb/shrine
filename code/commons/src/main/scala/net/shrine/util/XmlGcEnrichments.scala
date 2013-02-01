package net.shrine.util

import javax.xml.datatype.XMLGregorianCalendar
import org.spin.tools.NetworkTime

/**
 * @author clint
 * @date Dec 21, 2012
 */
object XmlGcEnrichments {
  final case class Milliseconds(value: Long)
  
  final implicit class EnrichedXmlGc(val xmlGc: XMLGregorianCalendar) extends AnyVal {
    def +(millis: Milliseconds): XMLGregorianCalendar = (new NetworkTime(xmlGc)).addMilliseconds(millis.value).getXMLGregorianCalendar
  }
  
  final implicit class EnrichedLong(val l: Long) extends AnyVal {
    def milliseconds = Milliseconds(l)
  }
}