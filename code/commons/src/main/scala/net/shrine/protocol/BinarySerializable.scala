package net.shrine.protocol

import org.apache.commons.codec.binary.Base64
import com.twitter.chill.ScalaKryoInstantiator

/**
 * @author David Ortiz
 * @date 7/23/13
 * @link http://cbmi.med.harvard.edu
 * @link http://chip.org
 *       <p/>
 *       NOTICE: This software comes with NO guarantees whatsoever and is
 *       licensed as Lgpl Open Source
 * @link http://www.gnu.org/licenses/lgpl.html
 */
trait BinarySerializable[T] {
  self: ShrineResponse =>

  def serializeToBase64Binary(): String = {
    Base64.encodeBase64String(ScalaKryoInstantiator.defaultPool.toBytesWithClass(self))
  }

}
