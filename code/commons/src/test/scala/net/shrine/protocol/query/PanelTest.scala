package net.shrine.protocol.query

import org.scalatest.matchers.ShouldMatchers
import org.scalatest.junit.AssertionsForJUnit
import junit.framework.TestCase
import scala.xml.NodeSeq
import scala.xml.Utility
import net.shrine.util.XmlUtil

final class PanelTest extends TestCase with AssertionsForJUnit with ShouldMatchers {
  private val t1 = Term("foo")
  
  private val term1 = Term("\\\\SHRINE\\SHRINE\\Diagnoses\\Congenital anomalies\\Cardiac and circulatory congenital anomalies\\Aortic valve stenosis\\Congenital stenosis of aortic valve\\")
  private val term2 = Term("\\\\SHRINE\\SHRINE\\Demographics\\Language\\Bosnian\\")
  private val term3 = Term("\\\\SHRINE\\SHRINE\\Demographics\\Age\18-34 years old\\30 years old\\")
  
  private val p1 = Panel(1, true, 1, None, None, Seq(t1))
    
  private val p2 = Panel(1, false, 1, None, None, Seq(t1))
  
  def testPanelGuards {
    intercept[IllegalArgumentException] {
      Panel(1, true, 1, None, None, Nil)
    }
    
    Panel(1, true, 1, None, None, Seq(t1))
  }
  
  def testInvert {
    p1.invert.inverted should be(false)
    p2.invert.inverted should be(true)
    
    p1.invert.invert should equal(p1)
  }
  
  def testComputeHLevel {
    import Panel.computeHLevel
    
    computeHLevel(term1) should be(5)
    computeHLevel(term2) should be(3)
    computeHLevel(term3) should be(3)
    
    computeHLevel(Term("foo")) should be(0)
    computeHLevel(Term("")) should be(0)
  }
  
  def testWithStart {
    val time = Utils.now
    
    val withStart = p1.withStart(Some(time))
    
    withStart should not be(p1)

    assert(withStart.start === Some(time))
    
    assert(withStart.end === p1.end)
    
    assert(withStart.number === p1.number)
    assert(withStart.inverted === p1.inverted)
    assert(withStart.minOccurrences === p1.minOccurrences)
    assert(withStart.terms === p1.terms)
  }

  def testWithEnd {
    val time = Utils.now
    
    val withEnd = p1.withEnd(Some(time))
    
    withEnd should not be(p1)

    assert(withEnd.start === p1.start)
    
    assert(withEnd.end === Some(time))
    
    assert(withEnd.number === p1.number)
    assert(withEnd.inverted === p1.inverted)
    assert(withEnd.minOccurrences === p1.minOccurrences)
    assert(withEnd.terms === p1.terms)
  }

  def testWithMinOccurrences {
    val min = 99
    
    val withMin = p1.withMinOccurrences(min)
    
    withMin should not be(p1)

    assert(withMin.minOccurrences === min)
    
    assert(withMin.start === p1.start)
    assert(withMin.end === p1.end)
    assert(withMin.number === p1.number)
    assert(withMin.inverted === p1.inverted)
    assert(withMin.terms === p1.terms)
  }
  
  def testToI2B2 {
    p1.toI2b2.head.label should equal("panel")
    
    //panel_number
    (p1.toI2b2 \ "panel_number").text should be("1")
    (p1.copy(number = 99).toI2b2 \ "panel_number").text should be("99")
    
    //panel date defaults
    (p1.toI2b2 \ "panel_start") should equal(NodeSeq.Empty)
    (p1.toI2b2 \ "panel_end") should equal(NodeSeq.Empty)
    
    //item date defaults
    (p1.toI2b2 \ "item" \ "constrain_by_date").toString should equal(XmlUtil.stripWhitespace(<constrain_by_date></constrain_by_date>).toString)
    (p1.toI2b2 \ "item" \ "constrain_by_date" \ "date_from") should equal(NodeSeq.Empty)
    (p1.toI2b2 \ "item" \ "constrain_by_date" \ "date_to") should equal(NodeSeq.Empty)
    
    //dates
    {
      val time = Utils.now
      
      val withStart = p1.withStart(Some(time)).toI2b2
      
      (withStart \ "panel_date_from").text should equal(time.toString)
      (withStart \ "item" \ "constrain_by_date" \ "date_from").text should equal(time.toString)
      (withStart \ "panel_date_to") should equal(NodeSeq.Empty)
      (withStart \ "item" \ "constrain_by_date" \ "date_to") should equal(NodeSeq.Empty)
      
      val withEnd = p1.withEnd(Some(time)).toI2b2
      
      (withEnd \ "panel_date_from") should equal(NodeSeq.Empty) 
      (withEnd \ "item" \ "constrain_by_date" \ "date_from") should equal(NodeSeq.Empty)
      (withEnd \ "panel_date_to").text should equal(time.toString)
      (withEnd \ "item" \ "constrain_by_date" \ "date_to").text should equal(time.toString)
      
      val withStartAndEnd = p1.withStart(Some(time)).withEnd(Some(time)).toI2b2
      
      (withStartAndEnd \ "panel_date_from").text should equal(time.toString)
      (withStartAndEnd \ "panel_date_to").text should equal(time.toString)
      
      (withStartAndEnd \ "item" \ "constrain_by_date" \ "date_from").text should equal(time.toString)
      (withStartAndEnd \ "item" \ "constrain_by_date" \ "date_to").text should equal(time.toString)
    }
    
    //invert
    (p1.toI2b2 \ "invert").text should equal("1") 
    (p1.invert.toI2b2 \ "invert").text should equal("0")
    
    //total_item_occurrences
    (p1.toI2b2 \ "total_item_occurrences").text should equal("1")
    (p1.withMinOccurrences(99).toI2b2 \ "total_item_occurrences").text should equal("99")
    
    //item defaults
    (p1.toI2b2 \ "item" \ "class").text should equal("ENC")
    (p1.toI2b2 \ "item" \ "item_icon").text should equal("LA")
    (p1.toI2b2 \ "item" \ "item_is_synonym").text should equal("false")
    
    //item/hlevel
    (p1.toI2b2 \ "item" \ "hlevel").text should equal("0")
    (p1.copy(terms = Seq(term1)).toI2b2 \ "item" \ "hlevel").text should equal("5")
    (p1.copy(terms = Seq(term2)).toI2b2 \ "item" \ "hlevel").text should equal("3")
    (p1.copy(terms = Seq(term3)).toI2b2 \ "item" \ "hlevel").text should equal("3")
    
    // item/item_name
    (p1.toI2b2 \ "item" \ "item_name").text should equal(t1.value)
    (p1.copy(terms = Seq(term1)).toI2b2 \ "item" \ "item_name").text should equal(term1.value)
    
    // item/item_key
    (p1.toI2b2 \ "item" \ "item_key").text should equal(t1.value)
    (p1.copy(terms = Seq(term1)).toI2b2 \ "item" \ "item_key").text should equal(term1.value)
    
    // item/tooltip
    (p1.toI2b2 \ "item" \ "tooltip").text should equal(t1.value)
    (p1.copy(terms = Seq(term1)).toI2b2 \ "item" \ "tooltip").text should equal(term1.value)
    
    //multiple items
    val with2Items = p1.copy(terms = Seq(term1, term2)).toI2b2
    
    (with2Items \\ "item").size should equal(2)
    
    val Seq(item1, item2) = (with2Items \\ "item")
    
    (item1 \ "item_name").text should equal(term1.value)
    (item1 \ "item_key").text should equal(term1.value)
    (item1 \ "tooltip").text should equal(term1.value)
    
    (item2 \ "item_name").text should equal(term2.value)
    (item2 \ "item_key").text should equal(term2.value)
    (item2 \ "tooltip").text should equal(term2.value)
  }
}