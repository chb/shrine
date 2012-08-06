package net.shrine.webclient.server

/**
 * @author clint
 * @date Aug 6, 2012
 * 
 * Wrapper around a Map to make JAX-RS serialization less painful
 */
final case class MultiInstitutionQueryResult(val entries: Map[String, Int]) extends scala.Iterable[(String, Int)] {
  def toMap: Map[String, Int] = entries
  
  override def iterator = entries.iterator
  
  def contains(key: String) = entries.contains(key)
  
  def get(key: String) = entries.get(key)
}