package net.shrine.i2b2.protocol.pm

import org.scalatest.FunSuite
import net.shrine.util.XmlUtil
import org.scalatest.junit.{AssertionsForJUnit, ShouldMatchersForJUnit}
import org.junit.Test

/**
 * @author Bill Simons
 * @date 3/6/12
 * @link http://cbmi.med.harvard.edu
 * @link http://chip.org
 *       <p/>
 *       NOTICE: This software comes with NO guarantees whatsoever and is
 *       licensed as Lgpl Open Source
 * @link http://www.gnu.org/licenses/lgpl.html
 */
class UserTest extends AssertionsForJUnit with ShouldMatchersForJUnit {

  def response = XmlUtil.stripWhitespace(
            <ns4:response xmlns:ns2="http://www.i2b2.org/xsd/hive/pdo/1.1/" xmlns:ns3="http://www.i2b2.org/xsd/cell/crc/pdo/1.1/" xmlns:ns4="http://www.i2b2.org/xsd/hive/msg/1.1/" xmlns:ns5="http://www.i2b2.org/xsd/cell/crc/psm/1.1/" xmlns:ns6="http://www.i2b2.org/xsd/cell/pm/1.1/" xmlns:ns7="http://sheriff.shrine.net/" xmlns:ns8="http://www.i2b2.org/xsd/cell/crc/psm/querydefinition/1.1/" xmlns:ns9="http://www.i2b2.org/xsd/cell/crc/psm/analysisdefinition/1.1/" xmlns:ns10="http://www.i2b2.org/xsd/cell/ont/1.1/" xmlns:ns11="http://www.i2b2.org/xsd/hive/msg/result/1.1/">
              <message_header>
                <i2b2_version_compatible>1.1</i2b2_version_compatible>
                <hl7_version_compatible>2.4</hl7_version_compatible>
                <sending_application>
                  <application_name>SHRINE</application_name>
                  <application_version>1.3-compatible</application_version>
                </sending_application>
                <sending_facility>
                  <facility_name>SHRINE</facility_name>
                </sending_facility>
                <datetime_of_message>2011-04-08T16:21:12.251-04:00</datetime_of_message>
                <security/>
                <project_id xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:nil="true"/>
              </message_header>
              <response_header>
                <result_status>
                  <status type="DONE">DONE</status>
                </result_status>
              </response_header>
              <message_body>
                      <ns6:configure>
                          <environment>DEVELOPMENT</environment>
                          <helpURL>http://www.i2b2.org</helpURL>
                          <user>
                              <full_name>Full name</full_name>
                              <user_name>user name</user_name>
                              <password token_ms_timeout="1800000" is_token="true">SessionKey:key</password>
                              <domain>demo</domain>
                              <param name="ecommons_username">ecommons</param>
                              <param name="other_param">other</param>
                              <project id="Demo">
                                  <name>Demo Group</name>
                                  <wiki>http://www.i2b2.org</wiki>
                                  <role>DATA_OBFSC</role>
                              </project>
                          </user>
                          <cell_datas>
                              <cell_data id="CRC">
                                  <name>Data Repository</name>
                                  <url>http://localhost/crc</url>
                                  <method>REST</method>
                              </cell_data>
                              <cell_data id="ONT">
                                  <name>Ontology Cell</name>
                                  <url>http://localhost/ont</url>
                                  <method>REST</method>
                                  <param name="OntSynonyms">false</param>
                                  <param name="OntMax">200</param>
                                  <param name="OntHidden">false</param>
                              </cell_data>
                          </cell_datas>
                      </ns6:configure>
                  </message_body>
            </ns4:response>)
  
  @Test
  def testFromI2b2() {
    val user = User.fromI2b2(response)
    user.credential.value should equal("SessionKey:key")
    user.credential.isToken should be(true)
    user.params("ecommons_username") should equal ("ecommons")
    user.params("other_param") should equal ("other")
  }

}