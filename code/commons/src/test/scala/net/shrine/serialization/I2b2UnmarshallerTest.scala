package net.shrine.serialization

import junit.framework.TestCase
import org.scalatest.matchers.ShouldMatchers
import org.scalatest.junit.AssertionsForJUnit
import org.junit.Test
import net.shrine.protocol.AuthenticationInfo
import net.shrine.protocol.Credential
import net.shrine.util.XmlUtil
import scala.xml.NodeSeq
import net.shrine.protocol.RequestHeader

/**
 * @author clint
 * @date Aug 16, 2012
 */
final class I2b2UnmarshallerTest extends TestCase with ShouldMatchers with AssertionsForJUnit {
  private val projectId = "jksahjksafhkafkla"
    
  private val waitTimeMs = 12345L
  
  private val authn = AuthenticationInfo("some-domain", "some-username", Credential("ksaljfksadlfjklsd", false))
  
  private val xml = XmlUtil.stripWhitespace(
    <foo>
      <message_header>
        <project_id>{ projectId }</project_id>
        { authn.toI2b2 }
      </message_header>
      <request_header>
        <result_waittime_ms>{ waitTimeMs }</result_waittime_ms>
      </request_header>
    </foo>)
  
  final class Foo
    
  private object MockUnmarshaller extends I2b2Unmarshaller[Foo] {
    override def fromI2b2(nodeSeq: NodeSeq): Foo = null
  }
    
  @Test
  def testShrineHeader {
    val RequestHeader(actualProjectId, actualWaitTimeMs, actualAuthn) = MockUnmarshaller.i2b2Header(xml)
    
    actualProjectId should equal(projectId)
    actualWaitTimeMs should equal(waitTimeMs)
    actualAuthn should equal(authn)
  }
  
  @Test
  def testShrineProjectId {
    MockUnmarshaller.i2b2ProjectId(xml) should equal(projectId)
  }
  
  @Test
  def testShrineWaitTimeMs {
    MockUnmarshaller.i2b2WaitTimeMs(xml) should equal(waitTimeMs)
  }
  
  @Test
  def testShrineAuthenticationInfo {
    MockUnmarshaller.i2b2AuthenticationInfo(xml) should equal(authn)
  }
}