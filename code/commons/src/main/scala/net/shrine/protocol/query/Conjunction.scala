package net.shrine.protocol.query

/**
 * @author clint
 * @date Dec 10, 2012
 */
abstract class Conjunction(combinator: (Expression*) => Expression) {
  val combine = combinator
}

object Conjunction {
  case object And extends Conjunction(net.shrine.protocol.query.And.apply)
  case object Or extends Conjunction(net.shrine.protocol.query.Or.apply)
}