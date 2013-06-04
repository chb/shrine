package net.shrine.adapter.service

import junit.framework.TestCase
import com.sun.jersey.test.framework.AppDescriptor
import net.shrine.util.JerseyAppDescriptor
import net.shrine.util.JerseyHttpClient
import com.sun.jersey.test.framework.JerseyTest
import org.junit.Before
import org.junit.After
import net.shrine.adapter.spring.AbstractShrineJUnitSpringTest
import net.shrine.adapter.dao.squeryl.AbstractSquerylAdapterTest

/**
 * @author clint
 * @date Apr 12, 2013
 */
trait JerseyTestComponent[H <: AnyRef] { self: AbstractSquerylAdapterTest with AbstractShrineJUnitSpringTest =>
  def makeHandler: H
  
  trait MixableJerseyTest[H] extends JerseyTest {
    var handler: H = _
  
    def resourceUrl = resource.getURI.toString + "i2b2/admin/request"
  }
  
  object JerseyTest extends MixableJerseyTest[H] {
    override def configure: AppDescriptor = {
      JerseyAppDescriptor.forResource[I2b2AdminResource].using {
        handler = makeHandler

        handler
      }
    }
  }
  
  def handler = JerseyTest.handler
  
  def resource = JerseyTest.resource
  
  def resourceUrl = JerseyTest.resourceUrl
}