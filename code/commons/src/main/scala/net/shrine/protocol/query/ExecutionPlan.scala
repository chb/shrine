package net.shrine.protocol.query

import scala.tools.scalap.scalax.rules.OrElse
import net.shrine.util.Util

/**
 * @author clint
 * @date Nov 29, 2012
 */
sealed trait ExecutionPlan {
  type ComponentType

  type ContainerType

  def or(other: ExecutionPlan): ExecutionPlan

  def and(other: ExecutionPlan): ExecutionPlan

  def combine(conjunction: Conjunction)(other: ExecutionPlan): ExecutionPlan

  def normalize: ExecutionPlan = this

  def isSimple: Boolean

  final def isCompound: Boolean = !isSimple
}

final case class SimpleQuery(expr: Expression) extends ExecutionPlan {
  override type ComponentType = Expression

  override type ContainerType = SimpleQuery

  override def or(other: ExecutionPlan) = combine(Conjunction.Or)(other)

  override def and(other: ExecutionPlan) = combine(Conjunction.And)(other)

  override def combine(conjunction: Conjunction)(other: ExecutionPlan): ExecutionPlan = other match {
    case SimpleQuery(otherExpr) => SimpleQuery(conjunction.combine(expr, otherExpr).normalize)
    case _: CompoundQuery => CompoundQuery(conjunction, this, other)
  }

  override def isSimple: Boolean = true
}

final case class CompoundQuery(conjunction: Conjunction, components: ExecutionPlan*) extends ExecutionPlan {
  override type ComponentType = Seq[ExecutionPlan]

  override type ContainerType = CompoundQuery

  override def toString = "CompoundQuery." + conjunction + "(" + components.mkString(",") + ")"

  override def or(other: ExecutionPlan) = CompoundQuery.Or(this, other)

  override def and(other: ExecutionPlan) = CompoundQuery.And(this, other)

  override def combine(conj: Conjunction)(other: ExecutionPlan) = conj match {
    case Conjunction.Or => or(other)
    case Conjunction.And => and(other)
  }

  override def isSimple: Boolean = false

  import ExpressionHelpers.is

  override def normalize: ExecutionPlan = {
    components match {
      case Seq(singlePlan) => singlePlan.normalize
      case _ => CompoundQuery(conjunction, components.flatMap {
        case xyz @ CompoundQuery(conj, comps @ _*) if comps.forall(_.isSimple) && conj == this.conjunction => {
          val byExprType = comps.groupBy(p => p.asInstanceOf[SimpleQuery].expr.getClass)

          val simpleQueriesByExprType = byExprType.map { case (_, plans) => plans.collect { case p: SimpleQuery => p } }

          simpleQueriesByExprType.flatMap { plans =>
            def ands = plans.collect { case SimpleQuery(a: And) => a }
            def ors = plans.collect { case SimpleQuery(o: Or) => o }
            
            val (orExprs: Seq[Or], andExprs: Seq[And]) = conj match {
              case Conjunction.Or => {
                val consolidatedOrExpr = plans.collect { case SimpleQuery(o: Or) => o }.foldLeft(Or())(_ ++ _)
                (Seq(consolidatedOrExpr), ands)
              }
              case Conjunction.And => {
                val consolidatedAndExpr = plans.collect { case SimpleQuery(a: And) => a }.foldLeft(And())(_ ++ _)
                (ors, Seq(consolidatedAndExpr))
              }
            }

            val otherPlans = plans.collect { case p @ SimpleQuery(expr) if !is[And](expr) && !is[Or](expr) => p }

            val andPlans = andExprs.flatMap(and => if (and.exprs.isEmpty) Seq.empty else Seq(SimpleQuery(and)))

            val orPlans = orExprs.flatMap(or => if (or.exprs.isEmpty) Seq.empty else Seq(SimpleQuery(or)))

            orPlans ++ andPlans ++ otherPlans
          }
        }
        case c => Seq(c)
      }: _*)
    }
  }
}

object CompoundQuery {
  def Or(components: ExecutionPlan*) = CompoundQuery(Conjunction.Or, components: _*)

  def And(components: ExecutionPlan*) = CompoundQuery(Conjunction.And, components: _*)
}