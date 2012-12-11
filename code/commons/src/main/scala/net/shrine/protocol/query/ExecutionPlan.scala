package net.shrine.protocol.query

/**
 * @author clint
 * @date Nov 29, 2012
 */
sealed trait ExecutionPlan {
  def or(other: ExecutionPlan): ExecutionPlan
  
  def and(other: ExecutionPlan): ExecutionPlan
}

final case class SimpleQuery(expr: Expression) extends ExecutionPlan {
  override def or(other: ExecutionPlan) = combine(Conjunction.Or)(other)
  
  override def and(other: ExecutionPlan) = combine(Conjunction.And)(other) 
  
  private def combine(conjunction: Conjunction)(other: ExecutionPlan): ExecutionPlan = other match {
    case SimpleQuery(otherExpr) => SimpleQuery(conjunction.combine(expr, otherExpr).normalize)
    case _: CompoundQuery => CompoundQuery(conjunction, this, other)
  }
}

final case class CompoundQuery(conjunction: Conjunction, components: ExecutionPlan*) extends ExecutionPlan {
  override def toString = "CompoundQuery." + conjunction + "(" + components.mkString(",") + ")"
  
  override def or(other: ExecutionPlan) = CompoundQuery.Or(this, other)
  
  override def and(other: ExecutionPlan) = CompoundQuery.And(this, other)
}

object CompoundQuery {
  def Or(components: ExecutionPlan*) = CompoundQuery(Conjunction.Or, components: _*)
  
  def And(components: ExecutionPlan*) = CompoundQuery(Conjunction.And, components: _*)
}