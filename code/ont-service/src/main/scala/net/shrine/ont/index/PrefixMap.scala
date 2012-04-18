package net.shrine.ont.index

import scala.collection.mutable.{ Map => MMap, MapLike => MMapLike }
import scala.collection.mutable.{ Builder, MapBuilder }
import scala.collection.generic.CanBuildFrom
import scala.annotation.tailrec

/**
 * Adapted from
 * http://docs.scala-lang.org/overviews/core/architecture-of-scala-collections.html
 * 
 * Instead of Strings (sequences of chars), keyed on ontology terms (sequences of term fragments),
 * for example "SHRINE", "SHRINE", "Demographics", "Gender", "Male"
 * 
 * TODO: desperately needs unit test
 */
private[index] final class PrefixMap[T] extends MMap[Seq[String], T] with MMapLike[Seq[String], T, PrefixMap[T]] {
  private var suffixes: Map[String, PrefixMap[T]] = Map.empty

  private var _value: Option[T] = None

  def value = _value

  def childKeys: Iterable[Seq[String]] = {
    for {
      (suffix, childMap) <- suffixes
      (childTermFragment, _) <- childMap.headOption
    } yield {
      suffix +: childTermFragment
    }
  }
  
  def get(termParts: Seq[String]): Option[T] = {
    if (termParts.isEmpty) {
      value
    } else {
      suffixes.get(termParts.head).flatMap(_.get(termParts.tail))
    }
  }
  
  def withPrefix(termParts: Seq[String]): PrefixMap[T] = {
    withPrefix(termParts, Int.MaxValue)
  }
  
  @tailrec
  def withPrefix(termParts: Seq[String], level: Int): PrefixMap[T] = {
    if (termParts.isEmpty || level == 0) {
      this
    } else {
      val leading = termParts.head

      suffixes.get(leading) match {
        case None => suffixes += (leading -> empty)
        case _ =>
      }

      suffixes(leading).withPrefix(termParts.tail, level - 1)
    }
  }

  override def update(termParts: Seq[String], elem: T) = withPrefix(termParts)._value = Some(elem)

  override def remove(termParts: Seq[String]): Option[T] = {
    if (termParts.isEmpty) {
      val prev = value

      _value = None

      prev
    } else {
      suffixes.get(termParts.head).flatMap(_.remove(termParts.tail))
    }
  }

  def iterator: Iterator[(Seq[String], T)] = {
    (for (v <- value.iterator) yield (Seq.empty, v)) ++
      (for {
        (fragment, subTrie) <- suffixes.iterator
        (subParts, v) <- subTrie.iterator
      } yield (fragment +: subParts, v))
  }

  def +=(kv: (Seq[String], T)): this.type = {
    val (termParts, v) = kv

    update(termParts, v)

    this
  }

  def -=(termParts: Seq[String]): this.type = {
    remove(termParts)

    this
  }

  override def empty = new PrefixMap[T]
}

/**
 * Adapted from
 * http://docs.scala-lang.org/overviews/core/architecture-of-scala-collections.html
 */
private[index] object PrefixMap extends {
  def empty[T] = new PrefixMap[T]

  def apply[T](kvs: (Seq[String], T)*): PrefixMap[T] = {
    val m: PrefixMap[T] = empty
    
    for (kv <- kvs) m += kv
    
    m
  }

  def newBuilder[T]: Builder[(Seq[String], T), PrefixMap[T]] = new MapBuilder[Seq[String], T, PrefixMap[T]](empty)

  implicit def canBuildFrom[T]: CanBuildFrom[PrefixMap[_], (Seq[String], T), PrefixMap[T]] = {
    new CanBuildFrom[PrefixMap[_], (Seq[String], T), PrefixMap[T]] {
      def apply(from: PrefixMap[_]) = newBuilder[T]
      def apply() = newBuilder[T]
    }
  }
} 
