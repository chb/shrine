package net.shrine.protocol

import org.scalatest.FunSuite
import org.junit.Test
import javax.xml.datatype.XMLGregorianCalendar
import org.spin.tools.NetworkTime._
import xml.Utility
import net.shrine.util.XmlUtil
import scala.xml.NodeSeq

/**
 * @author Bill Simons
 * @date 5/23/11
 * @link http://cbmi.med.harvard.edu
 * @link http://chip.org
 *       <p/>
 *       NOTICE: This software comes with NO guarantees whatsoever and is
 *       licensed as Lgpl Open Source
 * @link http://www.gnu.org/licenses/lgpl.html
 */
final class ReadQueryDefinitionResponseTest extends ShrineResponseI2b2SerializableValidator {

  val masterId = 965L
  val name = "Cerebrovascular@18:29:59"
  val userId = "user"
  val createDate = makeXMLGregorianCalendar("2011-05-16T11:58:17.767-04:00")

  def messageBody = XmlUtil.stripWhitespace {
    <message_body>
      <ns6:response xsi:type="ns6:master_responseType" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
        <status>
          <condition type="DONE">DONE</condition>
        </status>
        <query_master>
          <query_master_id>{ masterId }</query_master_id>
          <name>{ name }</name>
          <user_id>{ userId }</user_id>
          <create_date>{ createDate }</create_date>
          <request_xml>{ queryDefinition }</request_xml>
        </query_master>
      </ns6:response>
    </message_body>
  }

  val queryDefinition = XmlUtil.stripWhitespace {
    <ns6:query_definition>
      <query_name>Cerebrovascular@18:29:59</query_name>
      <specificity_scale>0</specificity_scale>
      <use_shrine>1</use_shrine>
      <panel>
        <panel_number>1</panel_number>
        <invert>0</invert>
        <total_item_occurrences>1</total_item_occurrences>
        <item>
          <hlevel>3</hlevel>
          <item_name>Cerebrovascular disease</item_name>
          <item_key>\\SHRINE\SHRINE\Diagnoses\Diseases of the circulatory system\Cerebrovascular disease\</item_key>
          <tooltip>Diagnoses\Diseases of the circulatory system\Cerebrovascular disease</tooltip>
          <class>ENC</class>
          <constrain_by_date></constrain_by_date>
          <item_icon>FA</item_icon>
          <item_is_synonym>false</item_is_synonym>
        </item>
      </panel>
    </ns6:query_definition>
  }

  val readQueryDefinitionResponse = XmlUtil.stripWhitespace {
    <readQueryDefinitionResponse>
      <masterId>{ masterId }</masterId>
      <name>{ name }</name>
      <userId>{ userId }</userId>
      <createDate>{ createDate }</createDate>
      <queryDefinition>{ queryDefinition.toString }</queryDefinition>
    </readQueryDefinitionResponse>
  }

  @Test
  def testEmpty {
    ReadQueryDefinitionResponse.Empty.createDate should be(None)
    ReadQueryDefinitionResponse.Empty.masterId should be(None)
    ReadQueryDefinitionResponse.Empty.name should be(None)
    ReadQueryDefinitionResponse.Empty.queryDefinition should be(None)
    ReadQueryDefinitionResponse.Empty.userId should be(None)
  }

  @Test
  def testFromXml {
    val actual = ReadQueryDefinitionResponse.fromXml(readQueryDefinitionResponse)

    actual.masterId.get should equal(masterId)
    actual.name.get should equal(name)
    actual.userId.get should equal(userId)
    actual.createDate.get should equal(createDate)
    actual.queryDefinition.get should equal(queryDefinition.toString)
  }

  @Test
  def testFromXmlEmpty {
    doTestFromNodeSeqEmpty(<readQueryDefinitionResponse></readQueryDefinitionResponse>)
  }

  @Test
  def testToXml {
    ReadQueryDefinitionResponse(Some(masterId), Some(name), Some(userId), Some(createDate), Some(queryDefinition.toString)).toXml should equal(readQueryDefinitionResponse)
  }

  @Test
  def testFromI2b2 {
    val actual = ReadQueryDefinitionResponse.fromI2b2(response)

    actual.masterId.get should equal(masterId)
    actual.name.get should equal(name)
    actual.userId.get should equal(userId)
    actual.createDate.get should equal(createDate)
    actual.queryDefinition.get should equal(queryDefinition.toString)
  }

  private def doTestFromNodeSeqEmpty(emptyXml: NodeSeq) {
    val actual = ReadQueryDefinitionResponse.fromXml(emptyXml)

    actual should equal(ReadQueryDefinitionResponse.Empty)
  }
  
  @Test
  def testFromI2b2Empty {
    val emptyI2b2Xml = XmlUtil.stripWhitespace {
      <message_body>
        <ns6:response xsi:type="ns6:master_responseType" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
          <status>
            <condition type="DONE">DONE</condition>
          </status>
        </ns6:response>
      </message_body>
    }

    doTestFromNodeSeqEmpty(emptyI2b2Xml)
  }

  @Test
  def testToI2b2 {
    ReadQueryDefinitionResponse(Some(masterId), Some(name), Some(userId), Some(createDate), Some(queryDefinition.toString)).toI2b2 should equal(response)
  }
}

