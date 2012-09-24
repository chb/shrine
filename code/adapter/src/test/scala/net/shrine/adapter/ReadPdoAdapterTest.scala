package net.shrine.adapter

import dao.{AdapterDAO}
import org.scalatest.junit.{ShouldMatchersForJUnit, AssertionsForJUnit}
import net.shrine.protocol.{ReadPdoRequest, Credential, AuthenticationInfo}
import org.junit.Test
import org.scalatest.mock.EasyMockSugar

/**
 *
 *
 * @author Justin Quan
 * @link http://chip.org
 * Date: 8/5/11
 */
class ReadPdoAdapterTest extends AssertionsForJUnit with ShouldMatchersForJUnit with EasyMockSugar {
  @Test
  def testPdoLocalTranslate {
    val networkId = "1234"
    val localId = "localId"
    val adapterDAO = mock[AdapterDAO]

    val adapter : ReadPdoAdapter = new ReadPdoAdapter("crcurl", MockHttpClient, adapterDAO, null)
    val xmlRequest =
      <ns3:request xsi:type="ns3:GetPDOFromInputList_requestType"
                   xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
        <input_list>
          <patient_list max="6" min="1">
            <patient_set_coll_id>{networkId}</patient_set_coll_id>
          </patient_list>
        </input_list>
        <filter_list>
          <panel name="\\i2b2\i2b2\Demographics\Age\0-9 years old\">
            <panel_number>0</panel_number>
            <panel_accuracy_scale>0</panel_accuracy_scale>
            <invert>0</invert>
            <item>
              <hlevel>3</hlevel>
              <item_key>\\i2b2\i2b2\Demographics\Age\0-9 years old\</item_key>
              <dim_tablename>concept_dimension</dim_tablename>
              <dim_dimcode>\i2b2\Demographics\Age\0-9 years old\</dim_dimcode>
              <item_is_synonym>N</item_is_synonym>
            </item>
          </panel>
        </filter_list>
        <output_option>
            <patient_set select="using_input_list" onlykeys="false"/>
            <event_set select="using_input_list" onlykeys="false"/>
            <observation_set blob="false" onlykeys="false"/>
        </output_option>
      </ns3:request>

    val credential: Credential = new Credential("pass", false)
    val info: AuthenticationInfo = new AuthenticationInfo("domain", "user", credential)
    val request : ReadPdoRequest = new ReadPdoRequest("project", 1L, info, networkId, xmlRequest)

    expecting {
      adapterDAO.findLocalResultID(1234L).andReturn(localId)
    }
    whenExecuting(adapterDAO) {
      val localRequest : ReadPdoRequest = adapter.translateNetworkToLocal(request)
      localRequest.patientSetCollId should equal(localId)
    }
  }
}