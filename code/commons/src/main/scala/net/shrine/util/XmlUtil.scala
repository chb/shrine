package net.shrine.util

import scala.xml.Utility._
import xml._

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

  //NB: As of Scala 2.10, now collapses elements like <foo></foo> to <foo/> :\
  def stripWhitespace(node: Node): Node = {
    def removeWhitespaceNodes(node: Node): NodeSeq = {
      node match {
        case text @ Text(t) => if(isSpace(t)) NodeSeq.Empty else text
        case e: Elem => e.copy(child = e.child.map(removeWhitespaceNodes).flatten)
        case _ => node
      }
    }
    
    removeWhitespaceNodes(node).headOption.getOrElse(node)
  }
  
  def renameRootTag(newRootTagName: String)(xml: Node): Node = xml match {
    case elem: Elem => elem.copy(label = newRootTagName)
    case _ => xml
  }
}