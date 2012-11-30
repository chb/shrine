package net.shrine.protocol

import xml.NodeSeq
import net.shrine.util.XmlUtil
import net.shrine.serialization.{ I2b2Unmarshaller, XmlUnmarshaller }

/**
 * @author Bill Simons
 * @date 4/13/11
 * @link http://cbmi.med.harvard.edu
 * @link http://chip.org
 *       <p/>
 *       NOTICE: This software comes with NO guarantees whatsoever and is
 *       licensed as Lgpl Open Source
 * @link http://www.gnu.org/licenses/lgpl.html
 *
 * NB: this is a case class to get a structural equality contract in hashCode and equals, mostly for testing
 *
 * !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
 * NOTE: Now that the adapter caches/stores results from the CRC, Instead of an
 * i2b2 instance id, this class now contains the Shrine-generated, network-wide
 * id of a query, which was used to obtain results previously obtained from the
 * CRC from Shrine's datastore.
 * !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
 */
final case class ReadInstanceResultsResponse(
    /*
     * !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
     * NOTE: Now that the adapter caches/stores results from the CRC, Instead of an
     * i2b2 instance id, this class now contains the Shrine-generated, network-wide 
     * id of a query, which is used to obtain results previously obtained from the 
     * CRC from Shrine's datastore.
     * !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
     */
    override val shrineNetworkQueryId: Long,
    override val results: Seq[QueryResult]) extends AbstractReadInstanceResultsResponse(shrineNetworkQueryId) {
  
  type ActualResponseType = ReadInstanceResultsResponse

  override def withId(id: Long) = this.copy(shrineNetworkQueryId = id)

  override def withResults(seq: Seq[QueryResult]) = this.copy(results = seq)
}

object ReadInstanceResultsResponse extends 
    AbstractReadInstanceResultsResponse.Companion[ReadInstanceResultsResponse] with 
    I2b2Unmarshaller[ReadInstanceResultsResponse] with 
    XmlUnmarshaller[ReadInstanceResultsResponse] {
  
  override def fromI2b2(nodeSeq: NodeSeq) = unmarshalFromI2b2(nodeSeq)

  override def fromXml(nodeSeq: NodeSeq) = unmarshalFromXml(nodeSeq)
}
