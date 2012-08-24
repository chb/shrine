package net.shrine.config

import java.util.{ ArrayList => JArrayList }
import java.util.{ Collection => JCollection }
import java.util.{ Iterator => JIterator }
import java.util.{ AbstractList => JAbstractList }
import java.util.{ ListIterator => JListIterator }

import javax.xml.bind.annotation.XmlElement

/**
 * @author Andy McMurry
 */
final case class LocalKeys(key: String) extends JAbstractList[String] {

  @XmlElement(name = "local_key", required = true)
  private val keys: JArrayList[String] = new JArrayList

  if (key != null) {
    keys.add(key)
  }

  /*for JAXB*/
  private def this() = this(null)

  override def add(key: String) = keys.add(key)

  override def add(index: Int, element: String) = keys.add(index, element)

  override def addAll(c: JCollection[_ <: String]) = keys.addAll(c)

  override def addAll(index: Int, c: JCollection[_ <: String]) = keys.addAll(index, c)

  override def clear() = keys.clear()

  override def contains(o: AnyRef) = keys.contains(o)

  override def containsAll(c: JCollection[_]) = keys.containsAll(c)

  override def get(index: Int) = keys.get(index)

  override def indexOf(o: AnyRef) = keys.indexOf(o)

  override def isEmpty = keys.isEmpty

  override def iterator = keys.iterator

  override def lastIndexOf(o: AnyRef) = keys.lastIndexOf(o)

  override def listIterator = keys.listIterator

  override def listIterator(index: Int) = keys.listIterator(index)

  override def remove(o: AnyRef) = keys.remove(o)

  override def remove(index: Int) = keys.remove(index)

  override def removeAll(c: JCollection[_]) = keys.removeAll(c)

  override def retainAll(c: JCollection[_]) = keys.retainAll(c)

  override def set(index: Int, element: String) = keys.set(index, element)

  override def size = keys.size

  override def subList(fromIndex: Int, toIndex: Int) = keys.subList(fromIndex, toIndex)
}
    