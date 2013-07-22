package net.shrine.util

import xml.{XML, TopScope, Elem, Node, Text}
import scala.xml.Utility._

/**
 * @author Justin Quan
 * @date 8/24/11
 * @link http://chip.org
 *       <p/>
 *       NOTICE: This software comes with NO guarantees whatsoever and is
 *       licensed as Lgpl Open Source
 * @link http://www.gnu.org/licenses/lgpl.html
 */
object XmlUtil {
  def stripNamespace(s: String): String = {
    stripNamespaces(XML.loadString(s)).toString
  }

  def stripNamespaces(node: Node): Node = {
    node match {
      case e: Elem => e.copy(prefix = null, scope = TopScope, child = e.child map {
        stripNamespaces
      })
      case _ => node
    }
  }

  def stripWhitespace(node: Node): Node = {
    node match {
      case Text(t) => new Text(if(isSpace(t)) "" else t)
      case e: Elem => e.copy(child = e.child map {
        stripWhitespace
      })
      case _ => node
    }
  }
}