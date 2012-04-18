package net.shrine.protocol.query

import scala.xml.NodeSeq
import javax.xml.datatype.XMLGregorianCalendar
import org.spin.tools.NetworkTime
import net.shrine.util.XmlUtil
import net.liftweb.json.JsonDSL._
import net.shrine.serialization.{JsonUnmarshaller, JsonMarshaller, XmlMarshaller, XmlUnmarshaller}
import net.liftweb.json.JsonAST._

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
sealed trait Expression extends XmlMarshaller with JsonMarshaller {
  def normalize: Expression = this
}

object Expression extends XmlUnmarshaller[Expression] with JsonUnmarshaller[Expression] {

  def fromJson(json: JValue): Expression = {
    def dateFromJson(json: JValue) = {
      json match {
        case JString(value) => Some(NetworkTime.makeXMLGregorianCalendar(value))
        case JNothing => None
        case _ => throw new Exception("Cannot parse json date" + json) //TODO some sort of unmarshalling exception
      }
    }

    json.children.head match {
      case JField("term", JString(value)) => Term(value)
      case JField("not", value) => Not(fromJson(value))
      case JField("and", value) => And(value.children.map(fromJson): _*)
      case JField("or", value) => Or(value.children.map(fromJson): _*)
      case JField("dateBounded", value) => {
        val start = dateFromJson(value \ "start")
        val end = dateFromJson(value \ "end")
        DateBounded(start, end, fromJson(value \ "expression"))
      }
      case JField("occurs", value) => {
        val min = (value \ "min") match {
          case JInt(x) => x.intValue()
          case _ => throw new Exception("Cannot parse json") //TODO some sort of unmarshalling exception
        }
        OccuranceLimited(min, fromJson(value \ "expression"))
      }
      case x => throw new Exception("Cannot parse json" + x.toString) //TODO some sort of unmarshalling exception
    }
  }

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
          case "and" => {
            And(childTags.map(fromXml): _*)
          }
          case "or" => {
            Or(childTags.map(fromXml): _*)
          }
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

//NOTE - refactoring the field name value will break json deserialization for this case class
final case class Term(value: String) extends Expression {
  override def toXml: NodeSeq = XmlUtil.stripWhitespace(<term>{ value }</term>)

  override def toJson: JValue = ("term" -> value)
}

final case class Not(expr: Expression) extends Expression {
  def withExpr(newExpr: Expression) = if(newExpr eq expr) this else this.copy(expr = newExpr)

  override def toXml: NodeSeq = XmlUtil.stripWhitespace(<not>{ expr.toXml }</not>)

  override def toJson: JValue = ("not" -> expr.toJson)

  override def normalize = {
    expr match {
      //Collapse repeated Nots: Not(Not(e)) => e
      case Not(e) => e.normalize
      case _ => this.withExpr(expr.normalize)
    }
  }
}

trait HasSubExpressions extends Expression {
  val exprs: Seq[Expression]
}

abstract class ComposeableExpression[T <: HasSubExpressions : Manifest](Op: (Expression *) => T, override val exprs: Expression*) extends HasSubExpressions {
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

  override def toXml: NodeSeq = XmlUtil.stripWhitespace(<and>{ exprs.map(_.toXml) }</and>)

  override def toJson: JValue = ("and" -> exprs.map(_.toJson))
}

final case class Or(override val exprs: Expression*) extends ComposeableExpression[Or](Or, exprs: _*) {

  override def toXml: NodeSeq = XmlUtil.stripWhitespace(<or>{ exprs.map(_.toXml) }</or>)

  override def toJson: JValue = ("or" -> exprs.map(_.toJson))
}

final case class DateBounded(start: Option[XMLGregorianCalendar], end: Option[XMLGregorianCalendar], expr: Expression) extends Expression {

  def withExpr(newExpr: Expression) = if(newExpr eq expr) this else this.copy(expr = newExpr)

  override def toXml: NodeSeq = XmlUtil.stripWhitespace(<dateBounded>
                                       { start.map(x => <start>{ x }</start>).getOrElse(<start/>) }
                                       { end.map(x => <end>{ x }</end>).getOrElse(<end/>) }
                                       { expr.toXml }
                                     </dateBounded>)

  override def toJson: JValue = ("dateBounded" ->
      ("start" -> start.map(_.toString)) ~
          ("end" -> end.map(_.toString)) ~
          ("expression" -> expr.toJson))

  override def normalize = {
    def normalize(date: Option[XMLGregorianCalendar]) = date.map(_.normalize)

    if(start.isEmpty && end.isEmpty) {
      expr.normalize
    } else {
      //NB: Dates are normalized to UTC.  I don't know if this is right, but it's what the existing webclient seems to do.
      val normalizedSubExpr = expr.normalize
      val normalizedStart = normalize(start)
      val normalizedEnd = normalize(end)

      DateBounded(normalizedStart, normalizedEnd, normalizedSubExpr)
    }
  }
}

final case class OccuranceLimited(min: Int, expr: Expression) extends Expression {
  require(min >= 1)

  def withExpr(newExpr: Expression) = if(newExpr eq expr) this else this.copy(expr = newExpr)

  override def toXml: NodeSeq = XmlUtil.stripWhitespace(<occurs>
                                       <min>{ min }</min>
                                       { expr.toXml }
                                     </occurs>)

  override def toJson: JValue = ("occurs" ->
      ("min" -> min) ~
          ("expression" -> expr.toJson))

  override def normalize = if(min == 1) expr.normalize else this.withExpr(expr.normalize)
}