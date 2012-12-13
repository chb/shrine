package net.shrine.protocol.query

import scala.tools.scalap.scalax.rules.OrElse
import net.shrine.util.Util

/**
 * @author clint
 * @date Nov 29, 2012
 */
sealed trait ExecutionPlan {
  def or(other: ExecutionPlan): ExecutionPlan

  def and(other: ExecutionPlan): ExecutionPlan

  def combine(conjunction: Conjunction)(other: ExecutionPlan): ExecutionPlan

  def normalize: ExecutionPlan = this

  def isSimple: Boolean

  final def isCompound: Boolean = !isSimple
}

final case class SimplePlan(expr: Expression) extends ExecutionPlan {
  override def or(other: ExecutionPlan) = combine(Conjunction.Or)(other)

  override def and(other: ExecutionPlan) = combine(Conjunction.And)(other)

  override def combine(conjunction: Conjunction)(other: ExecutionPlan): ExecutionPlan = other match {
    case SimplePlan(otherExpr) => SimplePlan(conjunction.combine(expr, otherExpr).normalize)
    case _: CompoundPlan => CompoundPlan(conjunction, this, other)
  }

  override def isSimple: Boolean = true
}

final case class CompoundPlan(conjunction: Conjunction, components: ExecutionPlan*) extends ExecutionPlan {
  override def toString = "CompoundPlan." + conjunction + "(" + components.mkString(",") + ")"

  override def or(other: ExecutionPlan) = CompoundPlan.Or(this, other)

  override def and(other: ExecutionPlan) = CompoundPlan.And(this, other)

  override def combine(conj: Conjunction)(other: ExecutionPlan) = conj match {
    case Conjunction.Or => or(other)
    case Conjunction.And => and(other)
  }

  override def isSimple: Boolean = false

  import ExpressionHelpers.is

  override def normalize: ExecutionPlan = {
    components match {
      case Seq(singlePlan) => singlePlan.normalize
      case _ => CompoundPlan(conjunction, components.flatMap {
        case xyz @ CompoundPlan(conj, comps @ _*) if comps.forall(_.isSimple) && conj == this.conjunction => {
          val byExprType = comps.groupBy { case SimplePlan(expr) => expr.getClass }

          val simpleQueriesByExprType = byExprType.map { case (_, plans) => plans.collect { case p: SimplePlan => p } }

          simpleQueriesByExprType.flatMap { plans =>
            def ands = plans.collect { case SimplePlan(a: And) => a }
            def ors = plans.collect { case SimplePlan(o: Or) => o }
            
            val otherPlans = plans.collect { case p @ SimplePlan(expr) if !is[And](expr) && !is[Or](expr) => p }
            
            val otherExprs = otherPlans.map(_.expr)
            
            val (orExprs: Seq[Or], andExprs: Seq[And]) = conj match {
              case Conjunction.Or => {
                val consolidatedOrExpr = plans.collect { case SimplePlan(o: Or) => o }.foldLeft(Or())(_ + _)
                (Seq(consolidatedOrExpr ++ otherExprs), ands)
              }
              case Conjunction.And => {
                val consolidatedAndExpr = plans.collect { case SimplePlan(a: And) => a }.foldLeft(And())(_ + _)
                (ors, Seq(consolidatedAndExpr ++ otherExprs))
              }
            }
            
            def toPlans[T <: HasSubExpressions](es: Seq[T]) = es.flatMap(e => if (e.exprs.isEmpty) Seq.empty else Seq(SimplePlan(e)))

            toPlans(orExprs) ++ toPlans(andExprs) ++ otherPlans
          }
        }
        case c => Seq(c)
      }: _*)
    }
  }
}

object CompoundPlan {
  def Or(components: ExecutionPlan*) = CompoundPlan(Conjunction.Or, components: _*)

  def And(components: ExecutionPlan*) = CompoundPlan(Conjunction.And, components: _*)
}