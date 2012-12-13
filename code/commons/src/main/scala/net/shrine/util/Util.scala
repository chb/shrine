package net.shrine.util

import javax.xml.datatype.XMLGregorianCalendar
import org.spin.tools.NetworkTime
import org.apache.log4j.Logger

/**
 * @author clint
 * @date Oct 18, 2012
 */
object Util extends Loggable {
  type ??? = Nothing

  def ??? = sys.error("Unimplemented")

  //NB: Will use current locale
  def now: XMLGregorianCalendar = (new NetworkTime).getXMLGregorianCalendar

  def tryOrElse[T](default: => T)(f: => T): T = {
    try { f } catch {
      case e: Exception => {
        error("Exception: ", e)
        
        default
      }
    }
  }
}