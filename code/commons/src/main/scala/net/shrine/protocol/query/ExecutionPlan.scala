package net.shrine.protocol.query

/**
 * @author clint
 * @date Nov 29, 2012
 */
sealed trait ExecutionPlan {
  def or(other: ExecutionPlan): ExecutionPlan
  
  def and(other: ExecutionPlan): ExecutionPlan
  
  def normalize: ExecutionPlan = this
  
  def isSimple: Boolean
  
  def isCompound: Boolean
}

final case class SimpleQuery(expr: Expression) extends ExecutionPlan {
  override def or(other: ExecutionPlan) = combine(Conjunction.Or)(other)
  
  override def and(other: ExecutionPlan) = combine(Conjunction.And)(other) 
  
  private def combine(conjunction: Conjunction)(other: ExecutionPlan): ExecutionPlan = other match {
    case SimpleQuery(otherExpr) => SimpleQuery(conjunction.combine(expr, otherExpr).normalize)
    case _: CompoundQuery => CompoundQuery(conjunction, this, other)
  }
  
  override def isSimple: Boolean = true
  
  override def isCompound: Boolean = false
}

final case class CompoundQuery(conjunction: Conjunction, components: ExecutionPlan*) extends ExecutionPlan {
  override def toString = "CompoundQuery." + conjunction + "(" + components.mkString(",") + ")"
  
  override def or(other: ExecutionPlan) = CompoundQuery.Or(this, other)
  
  override def and(other: ExecutionPlan) = CompoundQuery.And(this, other)
  
  override def isSimple: Boolean = false
  
  override def isCompound: Boolean = true
  
  import ExpressionHelpers.is
  
  override def normalize: ExecutionPlan = components match {
    case Seq(singlePlan) => singlePlan.normalize
    case _ => CompoundQuery(conjunction, components.flatMap {
      case CompoundQuery(conj, comps @ _*) if comps.forall(_.isSimple) && conj == this.conjunction => comps
      case c => Seq(c)
    }: _*)
  }
}

object CompoundQuery {
  def Or(components: ExecutionPlan*) = CompoundQuery(Conjunction.Or, components: _*)
  
  def And(components: ExecutionPlan*) = CompoundQuery(Conjunction.And, components: _*)
}