package net.shrine.protocol.query

import scala.xml.NodeSeq
import javax.xml.datatype.XMLGregorianCalendar
import org.spin.tools.NetworkTime
import net.shrine.util.XmlUtil
import net.liftweb.json.JsonDSL._
import net.shrine.serialization.{ JsonUnmarshaller, JsonMarshaller, XmlMarshaller, XmlUnmarshaller }
import net.liftweb.json.JsonAST._
import net.shrine.util.Try
import net.shrine.util.Failure
import net.shrine.util.Util

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
 */
sealed trait Expression extends XmlMarshaller with JsonMarshaller {
  def normalize: Expression = this

  def hasDirectI2b2Representation: Boolean

  def toExecutionPlan: ExecutionPlan //= SimpleQuery(this.normalize)
}

object Expression extends XmlUnmarshaller[Try[Expression]] with JsonUnmarshaller[Try[Expression]] {

  private def to[C <: ComposeableExpression[C]](make: (Expression*) => C): Seq[Expression] => C = make(_: _*)

  private val toOr = to(Or)
  private val toAnd = to(And)

  import Try.sequence

  def fromJson(json: JValue): Try[Expression] = {
    def dateFromJson(json: JValue): Try[XMLGregorianCalendar] = Try {
      json match {
        case JString(value) => NetworkTime.makeXMLGregorianCalendar(value)
        case _ => throw new Exception("Cannot parse json date" + json) //TODO some sort of unmarshalling exception
      }
    }

    json.children.head match {
      case JField("term", JString(value)) => Try(Term(value))
      case JField("not", value) => fromJson(value).map(Not)
      case JField("and", value) => sequence(value.children.map(fromJson)).map(toAnd)
      case JField("or", value) => sequence(value.children.map(fromJson)).map(toOr)
      case JField("dateBounded", value) => {
        for {
          expr <- fromJson(value \ "expression")
          start = dateFromJson(value \ "start").toOption
          end = dateFromJson(value \ "end").toOption
        } yield DateBounded(start, end, expr)
      }
      case JField("occurs", value) => {
        val min = Try((value \ "min") match {
          case JInt(x) => x.intValue
          case x => throw new Exception("Cannot parse json: " + x.toString) //TODO some sort of unmarshalling exception
        })

        for {
          expr <- fromJson(value \ "expression")
          m <- min
        } yield OccuranceLimited(m, expr)
      }
      case x => Failure(new Exception("Cannot parse json: " + x.toString)) //TODO some sort of unmarshalling exception
    }
  }

  def fromXml(nodeSeq: NodeSeq): Try[Expression] = {
    def dateFromXml(dateString: String) = {
      if (dateString.trim.isEmpty) {
        None
      } else {
        Option(NetworkTime.makeXMLGregorianCalendar(dateString))
      }
    }

    val outerTag = nodeSeq.head

    nodeSeq.size match {
      case 0 => Try(Or())
      case _ => {
        val childTags = outerTag.child

        outerTag.label match {
          case "term" => Try(Term(outerTag.text))
          //childTags.head because only one child expr of <not> is allowed
          case "not" => fromXml(childTags.head).map(Not)
          case "and" => {
            sequence(childTags.map(fromXml)).map(toAnd)
          }
          case "or" => {
            sequence(childTags.map(fromXml)).map(toOr)
          }
          case "dateBounded" => {
            for {
              //drop(2) to lose <start> and <end>
              //childTags.drop(2).head because only one child expr of <dateBounded> is allowed
              expr <- fromXml(childTags.drop(2).head)
              start = dateFromXml((nodeSeq \ "start").text)
              end = dateFromXml((nodeSeq \ "end").text)
            } yield DateBounded(start, end, expr)
          }
          case "occurs" => {
            for {
              min <- Try((nodeSeq \ "min").text.toInt)
              //drop(1) to lose <min>
              //childTags.drop(2).head because only one child expr of <occurs> is allowed
              expr <- fromXml(childTags.drop(1).head)
            } yield OccuranceLimited(min, expr)
          }
        }
      }
    }
  }
}

trait SimpleExpression extends Expression {
  override def hasDirectI2b2Representation = true

  override def toExecutionPlan = SimpleQuery(this)
}

//NOTE - refactoring the field name value will break json deserialization for this case class
final case class Term(value: String) extends SimpleExpression {
  override def toXml: NodeSeq = XmlUtil.stripWhitespace(<term>{ value }</term>)

  override def toJson: JValue = ("term" -> value)
}

final case class Query(localMasterId: String) extends SimpleExpression {
  override def toXml: NodeSeq = XmlUtil.stripWhitespace(<query>{ localMasterId }</query>)

  override def toJson: JValue = ("query" -> localMasterId)
}

final case class Not(expr: Expression) extends Expression {
  def withExpr(newExpr: Expression) = if (newExpr eq expr) this else this.copy(expr = newExpr)

  override def toXml: NodeSeq = XmlUtil.stripWhitespace(<not>{ expr.toXml }</not>)

  override def toJson: JValue = ("not" -> expr.toJson)

  override def normalize = {
    expr match {
      //Collapse repeated Nots: Not(Not(e)) => e
      case Not(e) => e.normalize
      case _ => this.withExpr(expr.normalize)
    }
  }

  override def hasDirectI2b2Representation = expr.hasDirectI2b2Representation

  override def toExecutionPlan = Util.??? //SimpleQuery(this.normalize)
}

trait HasSubExpressions extends Expression {
  val exprs: Seq[Expression]
}

abstract class ComposeableExpression[T <: HasSubExpressions: Manifest](Op: (Expression*) => T, override val exprs: Expression*) extends HasSubExpressions {
  import ExpressionHelpers.is

  def containsA[E: Manifest] = exprs.exists(is[E])

  override def normalize = exprs match {
    case x if x.isEmpty => this
    case Seq(expr) => expr.normalize
    case _ => Op(exprs.flatMap {
      case op: T if is[T](op) => op.exprs.map(_.normalize)
      case e => Seq(e.normalize)
    }: _*)
  }
}

final case class And(override val exprs: Expression*) extends ComposeableExpression[And](And, exprs: _*) {

  override def toString = "And(" + exprs.mkString(",") + ")"
  
  override def toXml: NodeSeq = XmlUtil.stripWhitespace(<and>{ exprs.map(_.toXml) }</and>)

  override def toJson: JValue = ("and" -> exprs.map(_.toJson))

  override def hasDirectI2b2Representation = exprs.forall(_.hasDirectI2b2Representation)

  override def toExecutionPlan: ExecutionPlan = {
    //TODO: WRONG
    //if (hasDirectI2b2Representation) {
    SimpleQuery(this.normalize)
    //} else {
    //  CompoundQuery.And(exprs.map(_.toExecutionPlan): _*) //TODO: almost certainly wrong
    //}
  }
}

final case class Or(override val exprs: Expression*) extends ComposeableExpression[Or](Or, exprs: _*) {

  override def toString = "Or(" + exprs.mkString(",") + ")"
  
  override def toXml: NodeSeq = XmlUtil.stripWhitespace(<or>{ exprs.map(_.toXml) }</or>)

  override def toJson: JValue = ("or" -> exprs.map(_.toJson))

  import ExpressionHelpers.is

  override def hasDirectI2b2Representation: Boolean = exprs.forall(e => !is[And](e) && e.hasDirectI2b2Representation)

  override def toExecutionPlan: ExecutionPlan = {
    if (hasDirectI2b2Representation) {
      SimpleQuery(this.normalize)
    } else {
      val (ands, notAnds) = exprs.partition(is[And])

      val andPlans = ands.map(_.toExecutionPlan)

      val andCompound = CompoundQuery.Or(andPlans: _*)

      if (notAnds.isEmpty) {
        andCompound
      } else {
        val notAndPlans = notAnds.map(_.toExecutionPlan)

        val consolidatedNotAndPlan = notAndPlans.reduce(_ or _)
        
        val components: Seq[ExecutionPlan] = andPlans.size match {
          case 1 => andPlans :+ consolidatedNotAndPlan
          case _ => if(ands.isEmpty) Seq(consolidatedNotAndPlan) else Seq(andCompound, consolidatedNotAndPlan)
        }
        
        val result = components match {
          case Seq(plan: CompoundQuery) => plan
          case _ => CompoundQuery.Or(components: _*)
        }
        
        result.normalize
      }
    }
  }
}

final case class DateBounded(start: Option[XMLGregorianCalendar], end: Option[XMLGregorianCalendar], expr: Expression) extends Expression {

  def withExpr(newExpr: Expression) = if (newExpr eq expr) this else this.copy(expr = newExpr)

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

    if (start.isEmpty && end.isEmpty) {
      expr.normalize
    } else {
      //NB: Dates are normalized to UTC.  I don't know if this is right, but it's what the existing webclient seems to do.
      val normalizedSubExpr = expr.normalize
      val normalizedStart = normalize(start)
      val normalizedEnd = normalize(end)

      DateBounded(normalizedStart, normalizedEnd, normalizedSubExpr)
    }
  }

  override def toExecutionPlan = Util.???

  override def hasDirectI2b2Representation = expr.hasDirectI2b2Representation
}

final case class OccuranceLimited(min: Int, expr: Expression) extends Expression {
  require(min >= 1)

  def withExpr(newExpr: Expression) = if (newExpr eq expr) this else this.copy(expr = newExpr)

  override def toXml: NodeSeq = XmlUtil.stripWhitespace(<occurs>
                                                          <min>{ min }</min>
                                                          { expr.toXml }
                                                        </occurs>)

  override def toJson: JValue = (
    "occurs" -> ("min" -> min) ~
    ("expression" -> expr.toJson))

  override def normalize = if (min == 1) expr.normalize else this.withExpr(expr.normalize)

  override def toExecutionPlan = Util.???

  override def hasDirectI2b2Representation = expr.hasDirectI2b2Representation
}