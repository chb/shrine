package net.shrine.protocol.query

/**
 * @author clint
 * @date Nov 29, 2012
 */
sealed trait ExecutionPlan

final case class SimpleQuery(expr: Expression) extends ExecutionPlan

final case class CompoundQuery(conjunction: Conjunction, components: ExecutionPlan*) extends ExecutionPlan

object CompoundQuery {
  def Or(components: ExecutionPlan*) = CompoundQuery(Conjunction.Or, components: _*)
  
  def And(components: ExecutionPlan*) = CompoundQuery(Conjunction.And, components: _*)
}