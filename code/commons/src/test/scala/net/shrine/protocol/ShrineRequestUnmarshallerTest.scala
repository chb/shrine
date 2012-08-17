package net.shrine.protocol

import junit.framework.TestCase
import org.scalatest.junit.AssertionsForJUnit
import org.scalatest.matchers.ShouldMatchers
import org.junit.Test
import net.shrine.util.XmlUtil
import scala.xml.NodeSeq

/**
 * @author clint
 * @date Aug 16, 2012
 */
final class ShrineRequestUnmarshallerTest extends TestCase with AssertionsForJUnit with ShouldMatchers {
  private val projectId = "jksahjksafhkafkla"
    
  private val waitTimeMs = 12345L
  
  private val authn = AuthenticationInfo("some-domain", "some-username", Credential("ksaljfksadlfjklsd", false))
  
  private val xml = XmlUtil.stripWhitespace(
    <foo>
      <projectId>{ projectId }</projectId>
      <waitTimeMs>{ waitTimeMs }</waitTimeMs>
      { authn.toXml }
    </foo>)
  
  final class Foo
    
  private object MockUnmarshaller extends ShrineRequestUnmarshaller[Foo] {
    override def fromXml(nodeSeq: NodeSeq): Foo = null
  }
    
  @Test
  def testShrineHeader {
    val RequestHeader(actualProjectId, actualWaitTimeMs, actualAuthn) = MockUnmarshaller.shrineHeader(xml)
    
    actualProjectId should equal(projectId)
    actualWaitTimeMs should equal(waitTimeMs)
    actualAuthn should equal(authn)
  }
  
  @Test
  def testShrineProjectId {
    MockUnmarshaller.shrineProjectId(xml) should equal(projectId)
  }
  
  @Test
  def testShrineWaitTimeMs {
    MockUnmarshaller.shrineWaitTimeMs(xml) should equal(waitTimeMs)
  }
  
  @Test
  def testShrineAuthenticationInfo {
    MockUnmarshaller.shrineAuthenticationInfo(xml) should equal(authn)
  }
}