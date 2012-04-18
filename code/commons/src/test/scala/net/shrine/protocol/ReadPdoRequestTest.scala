package net.shrine.protocol

import org.junit.Assert.assertTrue
import org.junit.Test
import xml.Utility
import net.shrine.util.XmlUtil

/**
 * @author Bill Simons
 * @date 3/11/11
 * @link http://cbmi.med.harvard.edu
 * @link http://chip.org
 *       <p/>
 *       NOTICE: This software comes with NO guarantees whatsoever and is
 *       licensed as Lgpl Open Source
 * @link http://www.gnu.org/licenses/lgpl.html
 */
class ReadPdoRequestTest extends ShrineRequestValidator {
  val pdoRequest = XmlUtil.stripWhitespace(
    <ns3:request xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:type="ns3:GetPDOFromInputList_requestType">
      <input_list>
        <patient_list max="123" min="1">
          <patient_set_coll_id>blah</patient_set_coll_id>
        </patient_list>
      </input_list>
      <filter_list>
        <panel name="\\i2b2\i2b2\Demographics\Gender\Female\">
          <panel_number>0</panel_number>
          <panel_accuracy_scale>0</panel_accuracy_scale>
          <invert>0</invert>
          <item>
            <hlevel>0</hlevel>
            <item_key>\\i2b2\i2b2\Demographics\Gender\Female\</item_key>
            <dim_tablename>concept_dimension</dim_tablename>
            <dim_dimcode>\i2b2\Demographics\Gender\Female\</dim_dimcode>
            <item_is_synonym>false</item_is_synonym>
          </item>
        </panel>
      </filter_list>
      <output_option>
          <patient_set onlykeys="false" select="using_input_list"/>
      </output_option>
    </ns3:request>)

  def messageBody = XmlUtil.stripWhitespace(
    <message_body>
      <ns3:pdoheader>
        <patient_set_limit>0</patient_set_limit>
        <estimated_time>180000</estimated_time>
        <request_type>getPDO_fromInputList</request_type>
      </ns3:pdoheader>{pdoRequest}
    </message_body>)

  val readPdoRequest = XmlUtil.stripWhitespace(
    <readPdo>
      {requestHeaderFragment}<optionsXml>
      {pdoRequest}
    </optionsXml>
    <patientSetCollId>blah</patientSetCollId>
    </readPdo>)

  @Test
  def testFromI2b2() {
    val translatedRequest = ReadPdoRequest.fromI2b2(request)
    validateRequestWith(translatedRequest) {
      translatedRequest.optionsXml.toString should equal(pdoRequest.toString)
    }
  }

  @Test
  def testShrineRequestFromI2b2() {
    val shrineRequest = ShrineRequest.fromI2b2(request)
    assertTrue(shrineRequest.isInstanceOf[ReadPdoRequest])
  }

  @Test
  def testToXml() {
    new ReadPdoRequest(projectId, waitTimeMs, authn, "blah", pdoRequest).toXml.toString() should equal(readPdoRequest.toString())
  }

  @Test
  def testToI2b2() {
    new ReadPdoRequest(projectId, waitTimeMs, authn, "blah", pdoRequest).toI2b2.toString() should equal(request.toString())
  }

  @Test
  def testFromXml() {
    val actual = ReadPdoRequest.fromXml(readPdoRequest)
    validateRequestWith(actual) {
      (actual.optionsXml).toString() should equal(pdoRequest.toString())
      actual.patientSetCollId should equal("blah")
    }
  }

  @Test
  def testShrineRequestFromXml() {
    assertTrue(ShrineRequest.fromXml(readPdoRequest).isInstanceOf[ReadPdoRequest])
  }

  @Test
  def testReplacePatientSetColId(): Unit = {
    val xml = XmlUtil.stripWhitespace(
      <message_body xmlns:ns3="http://www.i2b2.org/xsd/cell/crc/pdo/1.1/">
        <ns3:pdoheader>
          <patient_set_limit>0</patient_set_limit>
          <estimated_time>180000</estimated_time>
          <request_type>getPDO_fromInputList</request_type>
        </ns3:pdoheader>
        <ns3:request xsi:type="ns3:GetPDOFromInputList_requestType"
                     xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
          <input_list>
            <patient_list max="1000000" min="0">
              <patient_set_coll_id>5475</patient_set_coll_id>
            </patient_list>
          </input_list>
        </ns3:request>
      </message_body>)

    val transformed = ReadPdoRequest.updateCollId(xml, "test")
    transformed.isDefined should be(true)
    
    (transformed.get \ "request" \ "input_list" \ "patient_list" \ "patient_set_coll_id").text should equal("test")

    var request = new ReadPdoRequest(projectId, waitTimeMs, authn, "blah", pdoRequest)
    request = request.withPatientSetCollId("test")
    request.patientSetCollId should equal("test")
    request = ReadPdoRequest.fromXml(request.toXml)
    (request.optionsXml \ "input_list" \ "patient_list" \ "patient_set_coll_id").text should equal("test")
    request.patientSetCollId should equal("test")
    request = ReadPdoRequest.fromI2b2(request.toI2b2)
    (request.optionsXml \ "input_list" \ "patient_list" \ "patient_set_coll_id").text should equal("test")
    request.patientSetCollId should equal("test")

  }
}