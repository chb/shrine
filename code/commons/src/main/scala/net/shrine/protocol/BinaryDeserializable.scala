package net.shrine.protocol

import com.twitter.chill.ScalaKryoInstantiator
import org.apache.commons.codec.binary.Base64


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
trait BinaryDeserializable[T] {
  def fromBinaryBase64string(s: String): T = {
    ScalaKryoInstantiator.defaultPool.fromBytes(Base64.decodeBase64(s)).asInstanceOf[T]
  }
}


