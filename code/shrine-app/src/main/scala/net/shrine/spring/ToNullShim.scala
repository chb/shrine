package net.shrine.spring

/**
 * @author clint
 * @date Mar 4, 2013
 *
 * Work around not being able to invoke Option.orNull from Spring XML wiring
 */
object ToNullShim {
  def orNull[T <: AnyRef](option: Option[T]): T = option.getOrElse(null.asInstanceOf[T])
}