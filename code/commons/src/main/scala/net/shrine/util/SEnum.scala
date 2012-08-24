package net.shrine.util

import scala.collection.mutable.ListBuffer
import scala.collection.mutable.Buffer

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
    override def compare(other: Value): Int = this.ordinal - other.asInstanceOf[Value].ordinal
  }

  def values: Seq[T] = constants.toSeq

  def valueOf(name: String): Option[T] = constantsByName.get(name)
  
  private var ordinalCounter = 0

  private def nextOrdinal() = {
    val current = ordinalCounter

    ordinalCounter += 1

    current
  }

  private def register(v: ValueType) {
    constants += v
    constantsByName += (v.name -> v)
  }
  
  private val constants: Buffer[ValueType] = new ListBuffer

  private var constantsByName: Map[String, ValueType] = Map.empty
}