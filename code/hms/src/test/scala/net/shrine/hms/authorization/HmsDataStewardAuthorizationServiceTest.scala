package net.shrine.hms.authorization

import org.scalatest.junit.{ShouldMatchersForJUnit, AssertionsForJUnit}
import org.junit.Test
import net.shrine.protocol.ApprovedTopic
import org.scalatest.mock.EasyMockSugar
import junit.framework.TestCase

/**
 * @author Bill Simons
 * @date 1/30/12
 * @link http://cbmi.med.harvard.edu
 * @link http://chip.org
 *       <p/>
 *       NOTICE: This software comes with NO guarantees whatsoever and is
 *       licensed as Lgpl Open Source
 * @link http://www.gnu.org/licenses/lgpl.html
 */
final class HmsDataStewardAuthorizationServiceTest extends TestCase with AssertionsForJUnit with ShouldMatchersForJUnit with EasyMockSugar {

  @Test
  def testEscapeQueryText() {
    val queryText = """<query_definition><query_name>Acute posthemor@15:54:08</query_name><specificity_scale>0</specificity_scale><use_shrine>1</use_shrine><panel><panel_number>1</panel_number><invert>0</invert><total_item_occurrences>1</total_item_occurrences><item><hlevel>4</hlevel><item_name>Acute posthemorrhagic anemia</item_name><item_key>\\SHRINE\SHRINE\Diagnoses\Diseases of the blood and blood-forming organs\Anemia\Acute posthemorrhagic anemia\</item_key><tooltip>Diagnoses\Diseases of the blood and blood-forming organs\Anemia\Acute posthemorrhagic anemia</tooltip><class>ENC</class><constrain_by_date></constrain_by_date><item_icon>FA</item_icon><item_is_synonym>false</item_is_synonym></item></panel></query_definition>"""

    val actual = HmsDataStewardAuthorizationService.escapeQueryText(queryText)

    //NB: As of Scala 2.10, XmlUtility.stripWhitespace now collapses elements like
    //<foo></foo> to <foo/> :\
    actual should equal("""&lt;query_definition&gt;&lt;query_name&gt;Acute posthemor@15:54:08&lt;/query_name&gt;&lt;specificity_scale&gt;0&lt;/specificity_scale&gt;&lt;use_shrine&gt;1&lt;/use_shrine&gt;&lt;panel&gt;&lt;panel_number&gt;1&lt;/panel_number&gt;&lt;invert&gt;0&lt;/invert&gt;&lt;total_item_occurrences&gt;1&lt;/total_item_occurrences&gt;&lt;item&gt;&lt;hlevel&gt;4&lt;/hlevel&gt;&lt;item_name&gt;Acute posthemorrhagic anemia&lt;/item_name&gt;&lt;item_key&gt;\\\\SHRINE\\SHRINE\\Diagnoses\\Diseases of the blood and blood-forming organs\\Anemia\\Acute posthemorrhagic anemia\\&lt;/item_key&gt;&lt;tooltip&gt;Diagnoses\\Diseases of the blood and blood-forming organs\\Anemia\\Acute posthemorrhagic anemia&lt;/tooltip&gt;&lt;class&gt;ENC&lt;/class&gt;&lt;constrain_by_date/&gt;&lt;item_icon&gt;FA&lt;/item_icon&gt;&lt;item_is_synonym&gt;false&lt;/item_is_synonym&gt;&lt;/item&gt;&lt;/panel&gt;&lt;/query_definition&gt;""")
  }

  @Test
  def testApprovedParseTopics() {
    val parsedTopics = HmsDataStewardAuthorizationService.parseApprovedTopics("""[{"id":1,"name":"q1"},{"id":2,"name":"query0"},{"id":3,"name":"query1"},{"id":4,"name":"query2"}]""")
    parsedTopics should not be (null)
    parsedTopics.size should equal(4)
    parsedTopics should contain(new ApprovedTopic(1, "q1"))
  }

  @Test
  def testParseAuthorizationResponse() {
    HmsDataStewardAuthorizationService.parseAuthorizationResponse("""{"approved":true}""") should equal(true)
    HmsDataStewardAuthorizationService.parseAuthorizationResponse("""{"approved":false}""") should equal(false)
  }
}