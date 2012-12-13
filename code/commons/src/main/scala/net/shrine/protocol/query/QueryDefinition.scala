package net.shrine.protocol.query

import scala.xml.NodeSeq
import javax.xml.datatype.XMLGregorianCalendar
import javax.xml.datatype.DatatypeConstants
import scala.xml.Elem
import net.shrine.util.XmlUtil
import net.liftweb.json._
import net.liftweb.json.JsonDSL._
import net.shrine.serialization.{JsonMarshaller, I2b2Marshaller, XmlMarshaller, XmlUnmarshaller}
import net.shrine.util.Try
import net.shrine.util.Success
import net.shrine.util.Util


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
final case class QueryDefinition(name: String, expr: Expression) extends I2b2Marshaller with XmlMarshaller  with JsonMarshaller {

  import QueryDefinition._

  def transform(f: Expression => Expression) = this.copy(expr = f(this.expr))

  override def toJson: JValue = ("name" -> name) ~ ("expression" -> expr.toJson)

  override def toXml: NodeSeq = {
    XmlUtil.stripWhitespace(
      <queryDefinition>
        <name>{name}</name>
        <expr>{expr.toXml}</expr>
      </queryDefinition>)
  }

  //TODO: Will <use_shrine> and <specificity_scale> ever change?
  override def toI2b2: NodeSeq = {
    XmlUtil.stripWhitespace(
      <query_definition>
        <query_name>{ name }</query_name>
        <specificity_scale>0</specificity_scale>
        <use_shrine>1</use_shrine>
        {
          //Sequence of <panel>s
          toPanels(expr).map(_.toI2b2)
        }
      </query_definition>).asInstanceOf[Elem]
  }
}

object QueryDefinition extends XmlUnmarshaller[Try[QueryDefinition]] {

  override def fromXml(nodeSeq: NodeSeq): Try[QueryDefinition] = Try {
    val outerTag = nodeSeq.head

    val name = (outerTag \ "name").text
    
    val exprXml = (outerTag \ "expr").head.asInstanceOf[Elem]
    
    val innerExprXml = exprXml.child.head
    
    Expression.fromXml(innerExprXml).map(QueryDefinition(name, _))
  }.flatten

  def fromI2b2(i2b2Xml: String): Try[QueryDefinition] = {
    Try(scala.xml.XML.loadString(i2b2Xml)).flatMap(fromI2b2)
  }

  //I2b2 query definition XML => Expression
  def fromI2b2(nodeSeq: NodeSeq): Try[QueryDefinition] = {
    val outerTag = nodeSeq.head
    
    val name = (outerTag \ "query_name").text

    val panelsXml = outerTag \ "panel"
    
    val panels = Try.sequence(panelsXml.map(Panel.fromI2b2))

    val exprs = panels.map(ps => ps.map(_.toExpression))
    
    val consolidatedExpr = exprs.map(es => if(es.size == 1) es.head else And(es: _*))

    consolidatedExpr.map(consolidated => QueryDefinition(name, consolidated.normalize))
  }

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

  def toPanels(expr: Expression): Seq[Panel] = {
    def panelWithDefaults(terms: Seq[Term]) = Panel(1, false, 1, None, None, terms)

    val resultPanels = expr.normalize match {
      case q: Query => Util.???
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
      case OccuranceLimited(min, e) => toPanels(e).map(_.withMinOccurrences(min))
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