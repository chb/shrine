package net.shrine.webclient.server

import java.util.{List => JList, ArrayList => JArrayList, Map => JMap, HashMap => JHashMap}

/**
 * @author clint
 * @date Mar 23, 2012
 */
object Helpers {
  //Convert to a java list.  Needed because GWT serialization can only work on j.u.List impls
  //from java.util, not the wrappers from scala.collection.JavaConverters that implement j.u.List. :(
  def toJavaList[T](stuff: Iterable[T]): JList[T] = {
    val result = new JArrayList[T]
    
    stuff.foreach(result.add)
    
    result
  }
  //Convert to a java Map.  Needed because GWT serialization can only work on j.u.Map impls
  //from java.util, not the wrappers from scala.collection.JavaConverters that implement j.u.Map. :(
  def toJavaMap[K,V](stuff: Map[K,V]): JHashMap[K,V] = {
    val result = new JHashMap[K,V]
    
    stuff.foreach { case (key, value) => 
      result.put(key, value)
    }
    
    result
  }
}