package net.shrine.protocol.query

import net.shrine.protocol.I2b2Marshaller
import scala.xml.NodeSeq
import scala.xml.Utility
import javax.xml.datatype.XMLGregorianCalendar
import javax.xml.datatype.DatatypeConstants
import scala.xml.Elem
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
 * Classes to form expression trees representing Shrine queries
 */
final case class QueryDefinition(name: String, expr: Expression) extends I2b2Marshaller {

  import QueryDefinition._


  //TODO: Will <use_shrine> and <specificity_scale> ever change?
  override def toI2b2: NodeSeq = {
    def addNamespace(elem: Elem, prefix: String, uri: String): Elem = {
      import scala.xml.NamespaceBinding

      elem.copy(scope = NamespaceBinding(prefix, uri, elem.scope)).copy(prefix = prefix)
    }

    val xml = XmlUtil.stripWhitespace(<query_definition>
    <query_name>{ name }</query_name>
    <specificity_scale>0</specificity_scale>
    <use_shrine>1</use_shrine>
    {
    //Sequence of <panel>s
      toPanels(expr).map(_.toI2b2)
    }
    </query_definition>).asInstanceOf[Elem]

    //Add namespace here to appease i2b2
    addNamespace(xml, i2b2NamespacePrefix, i2b2Namespace)
  }
}

object QueryDefinition {
  val i2b2Namespace = "http://www.i2b2.org/xsd/cell/crc/psm/1.1/"
  val i2b2NamespacePrefix = "i2b2-crc-psm"

  private[query] def isAllTerms(exprs: Seq[Expression]) = !exprs.isEmpty && exprs.forall(_.isInstanceOf[Term])

  //NB: Chop off hour, minute, and second parts from dates by unsetting those fields. 
  //I don't know if this is right, but it is what the existing webclient seems to do.
  //NB: Mutates passed-in XmlGC
  private[query] def truncateDate(date: XMLGregorianCalendar): XMLGregorianCalendar = {
    date.setHour(DatatypeConstants.FIELD_UNDEFINED)
    date.setMinute(DatatypeConstants.FIELD_UNDEFINED)
    date.setSecond(DatatypeConstants.FIELD_UNDEFINED)
    date.setMillisecond(DatatypeConstants.FIELD_UNDEFINED)

    date
  }

  private[query] def toPanels(expr: Expression): Seq[Panel] = {
    def panelWithDefaults(terms: Seq[Term]) = Panel(1, false, 1, None, None, terms)

    val resultPanels = expr.normalize match {
      case t: Term => Seq(panelWithDefaults(Seq(t)))
      case Not(e) => toPanels(e).map(_.invert)
      case And(exprs@_*) => {
        exprs.flatMap(e => toPanels(e)).toSeq
      }
      case Or(exprs@_*) => exprs match {
        case Nil => Nil
        case _ => {
          //Or-expressions must be comprised of terms only, dues to limitations in i2b2's query representation
          require(isAllTerms(exprs), "Or-expressions must be comprised of Terms *only*.  Sorry.")

          //Terms only :\
          Seq(panelWithDefaults(exprs.collect {
            case t: Term => t
          }))
        }
      }
      case DateBounded(start, end, e) => {
        val truncatedStart = start.map(truncateDate)
        val truncatedEnd = end.map(truncateDate)

        toPanels(e).map(_.withStart(truncatedStart)).map(_.withEnd(truncatedEnd))
      }
      case OccuranceLimited(min, e) => {
        toPanels(e).map(_.withMinOccurrences(min))
      }
    }

    //Assign indicies; i2b2 indicies start from 1 
    resultPanels.size match {
      case 0 | 1 => resultPanels
      case _ => resultPanels.zipWithIndex.map {
        case (panel, index) => panel.copy(number = index + 1)
      }
    }
  }
}