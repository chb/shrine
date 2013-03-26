package net.shrine.util

import scala.collection.mutable.ListBuffer
import scala.collection.mutable.Buffer
import scala.math.Ordering

/**
 * @author clint
 * @date Mar 11, 2011
 *
 * Adapted from http://stackoverflow.com/questions/1898932/case-classes-vs-enumerations-in-scala/4958905#4958905
 *
 * Enum objects containing enum constants mix in SEnum, with T being the enum constant class
 */
//
trait SEnum[T] {
  private type ValueType = T with Value

  //Enum constants extend Value
  trait Value extends Ordered[Value] { self: T =>
    register(this)

    //name must be supplied somehow
    val name: String

    //ordinal field, like Java (is this valuable?) 
    val ordinal: Int = nextOrdinal()

    override def toString = name

    //Enums can be ordered by their ordinal field
    final override def compare(other: Value): Int = this.ordinal - other.asInstanceOf[Value].ordinal
    
    override def hashCode: Int = ordinal.hashCode
    
    override def equals(other: Any): Boolean = {
      val isRightType = other != null && other.isInstanceOf[ValueType]
      
      isRightType && other.asInstanceOf[ValueType].ordinal == this.ordinal
    }
  }
  
  implicit object ordering extends Ordering[ValueType] {
    final override def compare(x: ValueType, y: ValueType): Int = x.compare(y)
  }

  final def values: Seq[T] = constants.toSeq

  private def asKey(name: String): String = name.toLowerCase
  
  final def valueOf(name: String): Option[T] = constantsByName.get(asKey(name))
  
  private var ordinalCounter = 0

  private def nextOrdinal() = {
    val current = ordinalCounter

    ordinalCounter += 1

    current
  }

  private def register(v: ValueType) {
    constants += v
    constantsByName += (asKey(v.name) -> v)
  }
  
  private val constants: Buffer[ValueType] = new ListBuffer

  private var constantsByName: Map[String, ValueType] = Map.empty
}