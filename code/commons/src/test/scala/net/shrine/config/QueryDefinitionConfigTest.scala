package net.shrine.config

import org.scalatest.FunSuite
import org.scalatest.matchers.ShouldMatchers
import org.scalatest.junit.AssertionsForJUnit
import junit.framework.TestCase
import org.junit.Test
import net.liftweb.json._
import org.spin.tools.NetworkTime
import net.shrine.protocol.query.{QueryDefinition, Term, Panel}
import java.util.Calendar
import javax.xml.datatype.{DatatypeConstants, XMLGregorianCalendar}

/**
 * @author Bill Simons
 * @date 3/28/12
 * @link http://cbmi.med.harvard.edu
 * @link http://chip.org
 *       <p/>
 *       NOTICE: This software comes with NO guarantees whatsoever and is
 *       licensed as Lgpl Open Source
 * @link http://www.gnu.org/licenses/lgpl.html
 */
class QueryDefinitionConfigTest extends TestCase with AssertionsForJUnit with ShouldMatchers {

  @Test
  def testParsePanel() {
    val startDate = new NetworkTime().toString
    val endDate = new NetworkTime().toString
    val panelJson = String.format("""{"invert" : true,
      "minOccurrences" : 0,
      "start" : "%s",
      "end" : "%s",
      "terms" : [
        {"value" : "term1"},
        {"value" : "term2"}
      ]}""", startDate, endDate)
    val actual = QueryDefinitionConfig.parsePanel(1, parse(panelJson))
    actual should not be (null)
    actual.start.get.toString should equal(startDate)
    actual.end.get.toString should equal(endDate)
    actual.inverted should be(true)
    actual.minOccurrences should equal(0)
    actual.terms.size should equal(2)
    actual.terms should contain(Term("term1"))
    actual.terms should contain(Term("term2"))
  }

  def clearTime(date: XMLGregorianCalendar): XMLGregorianCalendar = {
    date.setHour(DatatypeConstants.FIELD_UNDEFINED)
    date.setMinute(DatatypeConstants.FIELD_UNDEFINED)
    date.setSecond(DatatypeConstants.FIELD_UNDEFINED)
    date.setMillisecond(DatatypeConstants.FIELD_UNDEFINED)
    date.setTimezone(DatatypeConstants.FIELD_UNDEFINED)

    date
  }

  @Test
  def testParseQueryDefinition() {
    val startCal = clearTime(new NetworkTime().getXMLGregorianCalendar)
    val endCal = clearTime(new NetworkTime().getXMLGregorianCalendar)
    val startDate = startCal.toString
    val endDate = endCal.toString
    val queryDefJson = String.format("""{"name": "query definition",
    "panels" : [
      {
        "start" : "%s",
        "end" : "%s",
        "terms" : [{"value" : "term1"}]
      },
      {
        "invert" : true,
        "minOccurrences" : 2,
        "terms" : [{"value" : "term2"}]
      }
    ]}""", startDate, endDate)
    val actual = QueryDefinitionConfig.parseQueryDefinition(parse(queryDefJson))
    actual.name should equal("query definition")
    val panels = QueryDefinition.toPanels(actual.expr)
    panels.size should equal(2)
    panels(0).number should equal(1)
    panels(0).inverted should be(false)
    panels(0).minOccurrences should equal(1)
    panels(0).start.get.toString should equal(startDate)
    panels(0).end.get.toString should equal(endDate)
    panels(0).terms should contain(Term("term1"))

    panels(1).number should equal(2)
    panels(1).inverted should be(true)
    panels(1).start should equal(None)
    panels(1).end should equal(None)
    panels(1).terms should contain(Term("term2"))
    panels(1).minOccurrences should equal(2)
  }

  @Test
  def testParseQueryDefinitionList() {
    val queryDefConfig= """{"queryDefinitions": [
      {
        "name": "query definition one",
        "panels" : [{"terms" : [{"value" : "term1"}]}]
      }
      {
        "name": "query definition two",
        "panels" : [{"terms" : [{"value" : "term2"}]}]
      }
    ]}"""
    val config = QueryDefinitionConfig.parseQueryDefinitionConfig(queryDefConfig)
    config.size should equal (2)
    config(0).name should equal ("query definition one")
    config(1).name should equal ("query definition two")
  }
}