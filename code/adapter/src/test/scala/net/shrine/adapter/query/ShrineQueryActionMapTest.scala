package net.shrine.adapter.query

import junit.framework.TestCase
import org.scalatest.junit.AssertionsForJUnit
import org.scalatest.junit.ShouldMatchersForJUnit
import org.spin.node.actions.EchoQueryAction
import org.spin.node.actions.discovery.DiscoveryQueryAction
import org.spin.node.actions.QueryAction

/**
 * @author Clint Gilbert
 * @date Feb 24, 2012
 * 
 */
final class ShrineQueryActionMapTest extends TestCase with AssertionsForJUnit with ShouldMatchersForJUnit {
  import scala.collection.JavaConverters._
  
  private val echo = new EchoQueryAction
  private val discovery = new DiscoveryQueryAction
  
  private val qas: Map[String, QueryAction[_]] = Map("echo" -> echo, "discovery" -> discovery)
  
  def testContainsQueryType {
    val map = new ShrineQueryActionMap(qas.asJava)
    
    map.containsQueryType("echo") should be(true)
    map.containsQueryType("discovery") should be(true)
    map.containsQueryType("jfhsdkhfksdf") should be(false)
    
    val empty = new ShrineQueryActionMap(Map.empty.asJava)
    
    empty.containsQueryType("echo") should be(false)
    empty.containsQueryType("discovery") should be(false)
    empty.containsQueryType("jfhsdkhfksdf") should be(false)
  }
  
  def testGetQueryAction {
    val map = new ShrineQueryActionMap(qas.asJava)
    
    map.getQueryAction("echo") should be(echo)
    map.getQueryAction("discovery") should be(discovery)
    map.getQueryAction("alskdjaklsdjl") should be(null)
    
    val empty = new ShrineQueryActionMap(Map.empty.asJava)
    
    empty.getQueryAction("echo") should be(null)
    empty.getQueryAction("discovery") should be(null)
    empty.getQueryAction("alskdjaklsdjl") should be(null)
  }
  
  def testGetQueryTypes {
    val map = new ShrineQueryActionMap(qas.asJava)
    
    map.getQueryTypes.asScala.toSet should equal(Set("echo", "discovery"))
    
    val empty = new ShrineQueryActionMap(Map.empty.asJava)
    
    empty.getQueryTypes.isEmpty should be(true)
  }
}