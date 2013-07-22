package net.shrine.adapter

import org.scalatest.junit.{ShouldMatchersForJUnit, AssertionsForJUnit}
import org.junit.Test
import translators.DefaultConceptTranslator
import scala.collection.JavaConversions._
import net.shrine.protocol.RunQueryRequest
import xml.{XML, Utility}
import net.shrine.util.XmlUtil

/**
 * @author Bill Simons
 * @date 4/19/11
 * @link http://cbmi.med.harvard.edu
 * @link http://chip.org
 *       <p/>
 *       NOTICE: This software comes with NO guarantees whatsoever and is
 *       licensed as Lgpl Open Source
 * @link http://www.gnu.org/licenses/lgpl.html
 */
class RunQueryAdapterTest extends AssertionsForJUnit with ShouldMatchersForJUnit {
  @Test
  def testTranslateQueryDefinitionXml() {
    val arrayOfStrings:java.util.List[String] = Vector("local1a", "local1b")
    val mappings: java.util.Map[String, java.util.List[String]] = Map("network" -> arrayOfStrings)
    val translator: DefaultConceptTranslator = new DefaultConceptTranslator(mappings)
    val adapter = new RunQueryAdapter("", null, null, translator, null, true)
    
    val queryDefinitionString = XmlUtil.stripWhitespace(<ns4:query_definition xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:ns8="http://www.i2b2.org/xsd/cell/crc/psm/querydefinition/1.1/" xmlns:ns2="http://www.i2b2.org/xsd/hive/pdo/1.1/" xmlns:ns5="http://www.i2b2.org/xsd/hive/plugin/" xmlns:ns3="http://www.i2b2.org/xsd/cell/crc/pdo/1.1/" xmlns:ns7="http://www.i2b2.org/xsd/cell/ont/1.1/" xmlns:ns4="http://www.i2b2.org/xsd/cell/crc/psm/1.1/" xmlns:ns6="http://www.i2b2.org/xsd/hive/msg/1.1/">
      <query_name>10-17 years old@14:39:20</query_name>
      <specificity_scale>0</specificity_scale>
      <use_shrine>1</use_shrine>
      <panel name="test">
        <panel_number>1</panel_number>
        <invert>0</invert>
        <total_item_occurrences>1</total_item_occurrences>
        <item>
          <hlevel>3</hlevel>
          <item_name>10-17 years old</item_name>
          <item_key>network</item_key>
          <tooltip>Demographics\Age\10-17 years old</tooltip>
          <class>ENC</class>
          <constrain_by_date>
          </constrain_by_date>
          <item_icon>FA</item_icon>
          <item_is_synonym>false</item_is_synonym>
        </item>
      </panel>
    </ns4:query_definition>).toString
    val newDef = adapter.translateQueryDefinition(queryDefinitionString)

    //note that expected value has no namespacing and has elements ordered according to i2b2 spec
    val expected = XmlUtil.stripWhitespace(<query_definition>
      <query_name>10-17 years old@14:39:20</query_name>
      <specificity_scale>0</specificity_scale>
      <panel name="test">
        <panel_number>1</panel_number>
        <panel_accuracy_scale>0</panel_accuracy_scale>
        <invert>0</invert>
        <total_item_occurrences>1</total_item_occurrences>
        <item>
          <hlevel>3</hlevel>
          <item_name>10-17 years old</item_name>
          <item_key>local1a</item_key>
          <item_icon>FA</item_icon>
          <tooltip>Demographics\Age\10-17 years old</tooltip>
          <class>ENC</class>
          <constrain_by_date>
          </constrain_by_date>
          <item_is_synonym>false</item_is_synonym>
        </item>
        <item>
          <hlevel>3</hlevel>
          <item_name>10-17 years old</item_name>
          <item_key>local1b</item_key>
          <item_icon>FA</item_icon>
          <tooltip>Demographics\Age\10-17 years old</tooltip>
          <class>ENC</class>
          <constrain_by_date>
          </constrain_by_date>
          <item_is_synonym>false</item_is_synonym>
        </item>
      </panel>
    </query_definition>)

    XML.loadString(newDef) should equal(expected)
  }
}