package net.shrine.protocol.query

import scala.xml.NodeSeq
import scala.xml.Utility.trim
import java.util.Date
import net.shrine.protocol.I2b2Marshaller
import net.shrine.protocol.XmlMarshaller
import net.shrine.protocol.XmlUnmarshaller
import javax.xml.datatype.XMLGregorianCalendar
import org.spin.tools.NetworkTime

/**
 *
 * @author Clint Gilbert
 * @date Jan 24, 2012
 *
 * @link http://cbmi.med.harvard.edu
 *
 * This software is licensed under the LGPL
 * @link http://www.gnu.org/licenses/lgpl.html
 *
 * Classes to form expression trees representing Shrine queries
 *
 * TODO: fromXml ?
 */
sealed trait Expression extends XmlMarshaller {
  def normalize: Expression = this
}

object Expression extends XmlUnmarshaller[Expression] {
  def fromXml(nodeSeq: NodeSeq): Expression = {
    def dateFromXml(dateString: String) = {
      if(dateString.trim.isEmpty) {
        None
      } else {
        Option(NetworkTime.makeXMLGregorianCalendar(dateString))
      }
    }

    val outerTag = nodeSeq.head

    nodeSeq.size match {
      case 0 => Or()
      case _ => {
        val childTags = outerTag.child

        outerTag.label match {
          case "term" => Term(outerTag.text)
          //childTags.head because only one child expr of <not> is allowed
          case "not" => Not(fromXml(childTags.head)) 
          case "and" => { And(childTags.map(fromXml): _*) }
          case "or" => { Or(childTags.map(fromXml): _*) }
          case "dateBounded" => {
            val start = dateFromXml((nodeSeq \ "start").text)
            val end = dateFromXml((nodeSeq \ "end").text)

            //drop(2) to lose <start> and <end>
            //childTags.drop(2).head because only one child expr of <dateBounded> is allowed
            DateBounded(start, end, fromXml(childTags.drop(2).head))
          }
          case "occurs" => {
            val min = (nodeSeq \ "min").text.toInt

            //drop(1) to lose <min>
            //childTags.drop(2).head because only one child expr of <occurs> is allowed
            OccuranceLimited(min, fromXml(childTags.drop(1).head))
          }
        }
      }
    }
  }
}

final case class Term(value: String) extends Expression {
  override def toXml: NodeSeq = trim(<term> { value } </term>)
}

final case class Not(expr: Expression) extends Expression {
  override def toXml: NodeSeq = trim(<not> { expr.toXml } </not>)

  override def normalize = {
    expr match {
      //Collapse repeated Nots: Not(Not(e)) => e
      case Not(e) => e.normalize
      case _ => {
        val normalizedSubExpr = expr.normalize

        if (normalizedSubExpr eq expr) this else Not(normalizedSubExpr)
      }
    }
  }
}

trait HasSubExpressions extends Expression {
  val exprs: Seq[Expression]
}

abstract class ComposeableExpression[T <: HasSubExpressions: Manifest](Op: (Expression*) => T, override val exprs: Expression*) extends HasSubExpressions {
  private def isT(x: AnyRef) = manifest[T].erasure.isAssignableFrom(x.getClass)

  override def normalize = exprs match {
    case x if x.isEmpty => this
    case Seq(expr) => expr.normalize
    case _ => Op(exprs.flatMap {
      case op: T if isT(op) => op.exprs.map(_.normalize)
      case e => Seq(e.normalize)
    }: _*)
  }
}

final case class And(override val exprs: Expression*) extends ComposeableExpression[And](And, exprs: _*) {
  override def toXml: NodeSeq = trim(<and> { exprs.map(_.toXml) } </and>)
}

final case class Or(override val exprs: Expression*) extends ComposeableExpression[Or](Or, exprs: _*) {
  override def toXml: NodeSeq = trim(<or> { exprs.map(_.toXml) } </or>)
}

final case class DateBounded(start: Option[XMLGregorianCalendar], end: Option[XMLGregorianCalendar], expr: Expression) extends Expression {
  override def toXml: NodeSeq = trim(<dateBounded>
                                       { start.map(x => <start>{ x }</start>).getOrElse(<start/>) }
                                       { end.map(x => <end>{ x }</end>).getOrElse(<end/>) }
                                       { expr.toXml }
                                     </dateBounded>)

  override def normalize = {
    def normalize(date: Option[XMLGregorianCalendar]) = date.map(_.normalize)

    if (start.isEmpty && end.isEmpty) {
      expr.normalize
    } else {
      //NB: Dates are normalized to UTC.  I don't know if this is right, but it's what the existing webclient seems to do.
      val normalizedSubExpr = expr.normalize
      val normalizedStart = normalize(start)
      val normalizedEnd = normalize(end)

      if ((normalizedSubExpr eq expr) && (normalizedStart eq start) && (normalizedEnd eq end)) {
        this
      } else {
        DateBounded(normalizedStart, normalizedEnd, normalizedSubExpr)
      }
    }
  }
}

final case class OccuranceLimited(min: Int, expr: Expression) extends Expression {
  require(min >= 0)

  override def toXml: NodeSeq = trim(<occurs>
                                       <min>{ min }</min>
                                       { expr.toXml }
                                     </occurs>)

  override def normalize = {
    if (min == 0) {
      expr.normalize
    } else {
      val normalizedSubExpr = expr.normalize

      if (normalizedSubExpr eq expr) this else OccuranceLimited(min, normalizedSubExpr)
    }
  }
}