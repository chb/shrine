package net.shrine.protocol.query

import org.spin.tools.NetworkTime
import java.util.GregorianCalendar

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
object Utils {
  def now = NetworkTime.makeXMLGregorianCalendar(new GregorianCalendar)
}