package net.shrine.protocol.query

import junit.framework.TestCase
import org.scalatest.junit.AssertionsForJUnit
import org.scalatest.matchers.ShouldMatchers
import org.spin.tools.NetworkTime
import javax.xml.datatype.DatatypeConstants
import scala.xml.Elem
import scala.xml.Utility

/**
 *
 * @author Clint Gilbert
 * @date Jan 26, 2012
 *
 * @link http://cbmi.med.harvard.edu
 *
 * This software is licensed under the LGPL
 * @link http://www.gnu.org/licenses/lgpl.html
 *
 */
final class QueryDefinitionTest extends TestCase with AssertionsForJUnit with ShouldMatchers {
  private val t1 = Term("""\\SHRINE\SHRINE\Diagnoses\Congenital anomalies\Cardiac and circulatory congenital anomalies\Aortic valve stenosis\Congenital stenosis of aortic valve\""")
  private val t2 = Term("""\\SHRINE\SHRINE\Demographics\Language\Bosnian\""")
  private val t3 = Term("""\\SHRINE\SHRINE\Demographics\Age\18-34 years old\30 years old\""")
  private val t4 = Term("foo")
  private val t5 = Term("blarg")
  private val t6 = Term("nuh")
  
  private val q1 = QueryDefinition("blarg", t1)
  
  def testToXml {
    val expected = Utility.trim(<queryDefinition>
    			   				  <name>blarg</name>
    			                  <expr>
    							    <term>{t1.value}</term>
    							  </expr>
  	             				</queryDefinition>)
  
    assert(q1.toXmlString === expected.toString) 	             				
  }

  def testFromXml {
    def now = QueryDefinition.truncateDate(Utils.now)
  
    val startDate = Some(now)
    val endDate = Some(now)
    
    val expr = And(OccuranceLimited(99, DateBounded(startDate, endDate, Not(t1))),
    			   OccuranceLimited(88, DateBounded(startDate, endDate, Not(t2))),
    			   OccuranceLimited(77, DateBounded(startDate, endDate, Not(t3))))
    					   
    val queryDef = QueryDefinition("foo", expr)
    
    val unmarshalled = QueryDefinition.fromXml(queryDef.toXml)
    
    assert(unmarshalled === queryDef)
  }
  
  private val i2b2Xml = {
    def now = QueryDefinition.truncateDate(Utils.now)
  
    val startDate = Some(now)
    val endDate = Some(now)
    
    val expr = And(OccuranceLimited(99, DateBounded(startDate, endDate, Not(t1))),
    					   OccuranceLimited(88, DateBounded(startDate, endDate, Not(t2))),
    					   OccuranceLimited(77, DateBounded(startDate, endDate, Not(t3))))
    					   
    <query_definition><query_name>foo</query_name><specificity_scale>0</specificity_scale><use_shrine>1</use_shrine>{ QueryDefinition.toPanels(expr).map(_.toI2b2) }</query_definition>
  }
  
  def testFromI2b2String {
    assert(QueryDefinition.fromI2b2(i2b2Xml.toString).toI2b2 === i2b2Xml)
  }
  
  def testFromI2b2 {
    assert(QueryDefinition.fromI2b2(i2b2Xml).toI2b2 === i2b2Xml) 
  }
  
  def testTruncateDate {
    val time = NetworkTime.makeXMLGregorianCalendar("2012-01-26T12:39:45.123Z")
  
    val truncated = QueryDefinition.truncateDate(time)
  
    truncated should not be (null)
    truncated should be(time)
  
    def isDefined(field: Int) = field != DatatypeConstants.FIELD_UNDEFINED
  
    isDefined(truncated.getHour) should be(false)
    isDefined(truncated.getMinute) should be(false)
    isDefined(truncated.getSecond) should be(false)
    isDefined(truncated.getMillisecond) should be(false)

    isDefined(truncated.getDay) should be(true)
    isDefined(truncated.getMonth) should be(true)
    isDefined(truncated.getYear) should be(true)

    truncated.toString should equal("2012-01-26Z")
  }

  def testIsAllTerms {
    import QueryDefinition.isAllTerms

    isAllTerms(Nil) should be(false)

    isAllTerms(Seq(t1)) should be(true)
    isAllTerms(Seq(t1, t2, t3)) should be(true)

    isAllTerms(Seq(And(t1, t2))) should be(false)
    isAllTerms(Seq(t1, And(), t2)) should be(false)
    isAllTerms(Seq(Not(t1), Not(t2))) should be(false)
  }

  def testToI2b2 {
    q1.toI2b2.head.label should equal("query_definition")

    //query_name    
    (q1.toI2b2 \ "query_name").text should equal(q1.name)

    //defaults
    (q1.toI2b2 \ "specificity_scale").text should equal("0")
    (q1.toI2b2 \ "use_shrine").text should equal("1")

    //panels
    import QueryDefinition.toPanels

    (q1.toI2b2 \ "panel").toString should equal(toPanels(t1).head.toI2b2.toString)

    val Seq(panel1, panel2) = (q1.copy(expr = And(t1, t2)).toI2b2 \\ "panel")

    panel1.toString should equal(toPanels(t1).head.toI2b2.toString)
    panel2.toString should equal(toPanels(t2).head.copy(number = 2).toI2b2.toString)
  }

  def testToPanels {
    import QueryDefinition.toPanels

    //A plain Term
    {
      val Seq(panel1) = toPanels(t1)

      panel1.number should be(1)
      panel1.inverted should be(false)
      panel1.minOccurrences should be(1)
      panel1.start should be(None)
      panel1.end should be(None)
      panel1.terms should be(Seq(t1))
    }

    //Not
    {
      val Seq(panel1) = toPanels(Not(t1))
      
      panel1.number should be(1)
      panel1.inverted should be(true)
      panel1.minOccurrences should be(1)
      panel1.start should be(None)
      panel1.end should be(None)
      panel1.terms should be(Seq(t1))
      
      //normalized?
      val Seq(panel2) = toPanels(Not(Not(t1)))
      
      panel2.number should be(1)
      panel2.inverted should be(false)
      panel2.minOccurrences should be(1)
      panel2.start should be(None)
      panel2.end should be(None)
      panel2.terms should be(Seq(t1))
    }
    
    //Or
    {
      //Or'ed Terms give a panel 
      val Seq(panel1) = toPanels(Or(t1, t2, t3))
      
      panel1.number should be(1)
      panel1.inverted should be(false)
      panel1.minOccurrences should be(1)
      panel1.start should be(None)
      panel1.end should be(None)
      panel1.terms should be(Seq(t1, t2, t3))
      
      //Should blow up on an Or that doesn't contain only Terms
      intercept[IllegalArgumentException] {
        toPanels(Or(t1, Not(t2), t3))
      }

      //empty Or gives no panels
      toPanels(Or()) should be(Nil)
      
      //normalized?
      val Seq(panel2) = toPanels(Or(t1, Or(t2, t3), Or()))
      
      panel2.number should be(1)
      panel2.inverted should be(false)
      panel2.minOccurrences should be(1)
      panel2.start should be(None)
      panel2.end should be(None)
      panel2.terms should be(Seq(t1, t2, t3))
    }
    
    //And
    {
      val Seq(panel1, panel2, panel3) = toPanels(And(t1, And(t2, t3)))
      
      panel1.number should be(1)
      panel1.inverted should be(false)
      panel1.minOccurrences should be(1)
      panel1.start should be(None)
      panel1.end should be(None)
      panel1.terms should be(Seq(t1))
      
      panel2.number should be(2)
      panel2.inverted should be(false)
      panel2.minOccurrences should be(1)
      panel2.start should be(None)
      panel2.end should be(None)
      panel2.terms should be(Seq(t2))
      
      panel3.number should be(3)
      panel3.inverted should be(false)
      panel3.minOccurrences should be(1)
      panel3.start should be(None)
      panel3.end should be(None)
      panel3.terms should be(Seq(t3))
    }
    
    val time = QueryDefinition.truncateDate(Utils.now)
    
    //DateBounded
    {
      val Seq(panel1) = toPanels(DateBounded(Some(time), None, t1))
      
      panel1.number should be(1)
      panel1.inverted should be(false)
      panel1.minOccurrences should be(1)
      panel1.start should be(Some(time))
      panel1.end should be(None)
      panel1.terms should be(Seq(t1))
    }
    
    {
      val Seq(panel1) = toPanels(DateBounded(None, Some(time), t1))
      
      panel1.number should be(1)
      panel1.inverted should be(false)
      panel1.minOccurrences should be(1)
      panel1.start should be(None)
      panel1.end should be(Some(time))
      panel1.terms should be(Seq(t1))
    }

    {
      val Seq(panel1) = toPanels(DateBounded(Some(time), Some(time), t1))
      
      panel1.number should be(1)
      panel1.inverted should be(false)
      panel1.minOccurrences should be(1)
      panel1.start should be(Some(time))
      panel1.end should be(Some(time))
      panel1.terms should be(Seq(t1))
    }
    
    //OccuranceLimited
    {
      val Seq(panel1) = toPanels(OccuranceLimited(99, t1))
      
      panel1.number should be(1)
      panel1.inverted should be(false)
      panel1.minOccurrences should be(99)
      panel1.start should be(None)
      panel1.end should be(None)
      panel1.terms should be(Seq(t1))
    }
    
    //Combo
    {
      val Seq(panel1, panel2, panel3, panel4) = toPanels(DateBounded(Some(time), Some(time), OccuranceLimited(99, And(t1, t2, t3, Not(Or(t4, t5, t6))))))
      
      panel1.number should be(1)
      panel1.inverted should be(false)
      panel1.minOccurrences should be(99)
      panel1.start should be(Some(time))
      panel1.end should be(Some(time))
      panel1.terms should be(Seq(t1))
      
      panel2.number should be(2)
      panel2.inverted should be(false)
      panel2.minOccurrences should be(99)
      panel2.start should be(Some(time))
      panel2.end should be(Some(time))
      panel2.terms should be(Seq(t2))
      
      panel3.number should be(3)
      panel3.inverted should be(false)
      panel3.minOccurrences should be(99)
      panel3.start should be(Some(time))
      panel3.end should be(Some(time))
      panel3.terms should be(Seq(t3))
      
      panel4.number should be(4)
      panel4.inverted should be(true)
      panel4.minOccurrences should be(99)
      panel4.start should be(Some(time))
      panel4.end should be(Some(time))
      panel4.terms should be(Seq(t4, t5, t6))
    }
  }
}