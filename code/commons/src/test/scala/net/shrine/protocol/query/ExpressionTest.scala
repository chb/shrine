package net.shrine.protocol.query

import junit.framework.TestCase
import org.scalatest.junit.AssertionsForJUnit
import org.scalatest.matchers.ShouldMatchers
import java.util.Date
import org.spin.tools.NetworkTime
import java.util.GregorianCalendar
import java.util.TimeZone
import javax.xml.datatype.XMLGregorianCalendar
import org.junit.Test
import scala.util.Try
import org.scalatest.junit.ShouldMatchersForJUnit
import net.shrine.util.Util
import net.shrine.protocol.XmlRoundTripper

/**
 *
 * @author Clint Gilbert
 * @date Jan 24, 2012
 *
 * @link http://cbmi.med.harvard.edu
 *
 * This software is licensed under the LGPL
 * @link http://www.gnu.org/licenses/lgpl.html
 *
 */
final class ExpressionTest extends TestCase with ShouldMatchersForJUnit {
  private[this] val t1 = Term("1")
  private[this] val t2 = Term("2")
  private[this] val t3 = Term("3")
  private[this] val t4 = Term("4")
  private[this] val t5 = Term("5")
  private[this] val t6 = Term("6")
  private[this] val t7 = Term("7")
  private[this] val t8 = Term("8")

  import Utils.now

  @Test
  def testOrToExectutionPlan {
    // 1 || 2
    //Plain old or, no need for sub-queries
    doToExecutionPlanTest(Or(t1, t2), SimplePlan(Or(t1, t2)))

    //(1 || 2) || (3 || 4)
    //nested Ors should be normalized first 
    doToExecutionPlanTest(
      Or(Or(t1, t2), Or(t3, t4)),
      SimplePlan(Or(t1, t2, t3, t4)))

    // (1 && 2) || (3 && 4)
    //Or of 2 Ands
    doToExecutionPlanTest(
      Or(And(t1, t2), And(t3, t4)),
      CompoundPlan.Or(SimplePlan(And(t1, t2)), SimplePlan(And(t3, t4))))

    // (1 && 2) || 3
    //And Ored with a Term 
    doToExecutionPlanTest(
      Or(And(t1, t2), t3),
      CompoundPlan.Or(SimplePlan(And(t1, t2)), SimplePlan(t3)))

    // (1 && 2) || (3 || 4)
    //And Ored with an Or
    doToExecutionPlanTest(
      Or(And(t1, t2), Or(t3, t4)),
      CompoundPlan.Or(SimplePlan(And(t1, t2)), SimplePlan(Or(t3, t4))))

    //(1 || 2) || (3 && 4) || (5 || 6)
    //Mix of Ors and Ands
    doToExecutionPlanTest(
      Or(Or(t1, t2), And(t3, t4), Or(t5, t6)),
      CompoundPlan.Or(SimplePlan(And(t3, t4)), SimplePlan(Or(t1, t2, t5, t6))))

    //(1 || 2) || 3 || (4 && 5) || 6 || (7 || 8)
    //Mix with raw terms too
    doToExecutionPlanTest(
      Or(Or(t1, t2), t3, And(t4, t5), t6, Or(t7, t8)),
      CompoundPlan.Or(SimplePlan(And(t4, t5)), SimplePlan(Or(t1, t2, t3, t6, t7, t8))))
  }

  @Test
  def testOrToExecutionPlanMoreNesting {
    // 1 || ((2 && 3) || (4 && 5)) 
    doToExecutionPlanTest(
      Or(t1, Or(And(t2, t3), And(t4, t5))),
      CompoundPlan.Or(SimplePlan(t1), SimplePlan(And(t2, t3)), SimplePlan(And(t4, t5))))

    // (1 && 2) || ((3 && 4) || 5) 
    doToExecutionPlanTest(
      Or(And(t1, t2), Or(And(t3, t4), t5)),
      CompoundPlan.Or(SimplePlan(And(t1, t2)), SimplePlan(And(t3, t4)), SimplePlan(t5)))

    // (1 || 2) || ((3 && 4) || (5 || 6))
    doToExecutionPlanTest(
      Or(Or(t1, t2), Or(And(t3, t4), Or(t5, t6))),
      CompoundPlan.Or(SimplePlan(Or(t1, t2)), SimplePlan(And(t3, t4)), SimplePlan(Or(t5, t6))))

    //1 || (2 || 3) || (4 && 5) || (6 || 7)
    doToExecutionPlanTest(
      Or(Or(t1, Or(t2, t3), And(t4, t5), Or(t6, t7))),
      CompoundPlan.Or(SimplePlan(And(t4, t5)), SimplePlan(Or(t1, t2, t3, t6, t7))))

    //(1 || 2) || (3 || 4) || (5 && 6) || (7 || 8)
    doToExecutionPlanTest(
      Or(Or(t1, t2), Or(t3, t4), And(t5, t6), Or(t7, t8)),
      CompoundPlan.Or(SimplePlan(And(t5, t6)), SimplePlan(Or(t1, t2, t3, t4, t7, t8))))

    //(1 && 2) || (3 || 4) || (5 && 6) || (7 || 8)
    doToExecutionPlanTest(
      Or(And(t1, t2), Or(t3, t4), And(t5, t6), Or(t7, t8)),
      CompoundPlan.Or(SimplePlan(And(t1, t2)), SimplePlan(And(t5, t6)), SimplePlan(Or(t3, t4, t7, t8))))

    //1 || ((2 || 3) || (4 && 5) || (6 || 7))
    doToExecutionPlanTest(
      Or(t1, Or(Or(t2, t3), And(t4, t5), Or(t6, t7))),
      CompoundPlan.Or(SimplePlan(And(t4, t5)), SimplePlan(Or(t1, t2, t3, t6, t7))))

    //(1 || 2) || ((3 || 4) || (5 && 6) || (7 || 8))
    doToExecutionPlanTest(
      Or(Or(t1, t2), Or(Or(t3, t4), And(t5, t6), Or(t7, t8))),
      CompoundPlan.Or(SimplePlan(Or(t1, t2, t3, t4, t7, t8)), SimplePlan(And(t5, t6))))

    //(1 && 2) || ((3 || 4) || (5 && 6) || (7 || 8))
    doToExecutionPlanTest(
      Or(And(t1, t2), Or(Or(t3, t4), And(t5, t6), Or(t7, t8))),
      CompoundPlan.Or(SimplePlan(And(t1, t2)), SimplePlan(And(t5, t6)), SimplePlan(Or(t3, t4, t7, t8))))
  }

  @Test
  def testAndToExecutionPlan {
    // 1 && 2
    //Plain old And, no need for sub-queries
    doToExecutionPlanTest(And(t1, t2), SimplePlan(And(t1, t2)))

    // (1 && 2) && (3 && 4)
    //nested Ands should be normalized first 
    doToExecutionPlanTest(
      And(And(t1, t2), And(t3, t4)),
      SimplePlan(And(t1, t2, t3, t4)))

    //(1 || 2) && (3 || 4)
    //Nested ors should be fine
    doToExecutionPlanTest(
      And(Or(t1, t2), Or(t3, t4)),
      SimplePlan(And(Or(t1, t2), Or(t3, t4))))

    // (1 || 2) && 3
    //And Ored with a Term 
    doToExecutionPlanTest(
      And(Or(t1, t2), t3),
      SimplePlan(And(Or(t1, t2), t3)))

    // (1 || 2) && (3 && 4)
    //And Ored with an Or
    doToExecutionPlanTest(
      And(Or(t1, t2), And(t3, t4)),
      SimplePlan(And(Or(t1, t2), t3, t4)))

    //(1 && 2) && (3 || 4) && (5 && 6)
    //Mix of Ors and Ands
    doToExecutionPlanTest(
      And(And(t1, t2), Or(t3, t4), And(t5, t6)),
      SimplePlan(And(t1, t2, Or(t3, t4), t5, t6)))

    //(1 && 2) && 3 && (4 || 5) && 6 && (7 && 8)
    //Mix with raw terms too
    doToExecutionPlanTest(
      And(And(t1, t2), t3, Or(t4, t5), t6, And(t7, t8)),
      SimplePlan(And(t1, t2, t3, Or(t4, t5), t6, t7, t8)))
  }

  @Test
  def testAndToExecutionPlanYieldsCompoundPlans {
    //((1 && 2) || (3 && 4)) && ((5 && 6) || (7 && 8))
    doToExecutionPlanTest(
      And(Or(And(t1, t2), And(t3, t4)), Or(And(t5, t6), And(t7, t8))),
      CompoundPlan.And(CompoundPlan.Or(SimplePlan(And(t1, t2)), SimplePlan(And(t3, t4))), CompoundPlan.Or(SimplePlan(And(t5, t6)), SimplePlan(And(t7, t8)))))

    //((1 && 2) || (3 && 4)) && ((5 || 6) && (7 || 8))
    doToExecutionPlanTest(
      And(Or(And(t1, t2), And(t3, t4)), Or(And(t5, t6), And(t7, t8))),
      CompoundPlan.And(CompoundPlan.Or(SimplePlan(And(t1, t2)), SimplePlan(And(t3, t4))), SimplePlan(And(Or(t5, t6), Or(t7, t8)))))
  }

  @Test
  def testAndToExecutionPlanMoreNesting {
    // 1 && ((2 || 3) && (4 || 5)) 
    doToExecutionPlanTest(
      And(t1, And(Or(t2, t3), Or(t4, t5))),
      SimplePlan(And(t1, Or(t2, t3), Or(t4, t5))))

    // (1 || 2) && ((3 || 4) && 5) 
    doToExecutionPlanTest(
      And(Or(t1, t2), And(Or(t3, t4), t5)),
      SimplePlan(And(Or(t1, t2), Or(t3, t4), t5)))

    // (1 && 2) && ((3 || 4) && (5 && 6))
    doToExecutionPlanTest(
      And(And(t1, t2), And(Or(t3, t4), And(t5, t6))),
      SimplePlan(And(t1, t2, Or(t3, t4), t5, t6)))

    //1 && (2 && 3) && (4 || 5) && (6 && 7)
    doToExecutionPlanTest(
      And(And(t1, And(t2, t3), Or(t4, t5), And(t6, t7))),
      SimplePlan(And(t1, t2, t3, Or(t4, t5), t6, t7)))

    //(1 && 2) && (3 && 4) && (5 || 6) && (7 && 8)
    doToExecutionPlanTest(
      And(And(t1, t2), And(t3, t4), Or(t5, t6), And(t7, t8)),
      SimplePlan(And(t1, t2, t3, t4, Or(t5, t6), t7, t8)))

    //(1 || 2) && (3 && 4) && (5 || 6) && (7 && 8)
    doToExecutionPlanTest(
      And(Or(t1, t2), And(t3, t4), And(t5, t6), And(t7, t8)),
      SimplePlan(And(Or(t1, t2), t3, t4, t5, t6, t7, t8)))

    //1 && ((2 && 3) && (4 || 5) && (6 && 7))
    doToExecutionPlanTest(
      And(t1, And(And(t2, t3), Or(t4, t5), And(t6, t7))),
      SimplePlan(And(t1, t2, t3, Or(t4, t5), t6, t7)))

    //(1 && 2) && ((3 && 4) && (5 || 6) && (7 && 8))
    doToExecutionPlanTest(
      And(And(t1, t2), And(And(t3, t4), Or(t5, t6), And(t7, t8))),
      SimplePlan(And(t1, t2, t3, t4, Or(t5, t6), t7, t8)))

    //(1 || 2) && ((3 && 4) && (5 || 6) && (7 && 8))
    doToExecutionPlanTest(
      And(Or(t1, t2), And(And(t3, t4), Or(t5, t6), And(t7, t8))),
      SimplePlan(And(Or(t1, t2), t3, t4, Or(t5, t6), t7, t8)))
  }

  private def doToExecutionPlanTest(expr: Expression, expected: ExecutionPlan) {
    val actual = expr.toExecutionPlan

    actual.getClass should equal(expected.getClass)

    actual match {
      case SimplePlan(e) => e should equal(expected.asInstanceOf[SimplePlan].expr)
      case expectedCompound @ CompoundPlan(conjunction, components @ _*) => {
        conjunction should equal(expectedCompound.conjunction)
        //NB: Use toSet to disregard order
        components.toSet should equal(expectedCompound.components.toSet)
      }
    }
  }

  private type HasWithExpr[T] = {
    val expr: Expression
    def withExpr(expr: Expression): T
  }

  private def doTestWithExpr[T <: HasWithExpr[T]](o: HasWithExpr[T]) {
    val withSameExpr = o.withExpr(o.expr)

    assert((withSameExpr eq o) === true)

    val term = Term("asjkdklas")

    val withNewExpr = o.withExpr(term)

    assert((withNewExpr eq o) === false)
    assert(withNewExpr.expr === term)
  }

  @Test
  def testNotWithExpr = doTestWithExpr(Not(t1))

  @Test
  def testDateBoundedWithExpr = doTestWithExpr(DateBounded(Some(now), Some(now), t1))

  @Test
  def testOccuranceLimitedWithExpr = doTestWithExpr(OccuranceLimited(1, t1))

  private def roundTrip[T](expr: Expression, serialize: Expression => T, deserialize: T => Try[Expression]) {
    assert(deserialize(serialize(expr)).get === expr)
  }

  private def xmlRoundTrip(expr: Expression) {
    roundTrip(expr, _.toXml, Expression.fromXml)
  }

  @Test
  def testExpressionFromXml {

    val expr = OccuranceLimited(99, And(Not(t1), Or(t2, t3, And(t4, t5), DateBounded(Some(now), Some(now), t6))))

    val db1 = DateBounded(None, None, Or(t1, t2, t3))
    val db2 = DateBounded(Some(now), None, Or(t1, t2, t3))
    val db3 = DateBounded(None, Some(now), Or(t1, t2, t3))
    val db4 = DateBounded(Some(now), Some(now), Or(t1, t2, t3))

    xmlRoundTrip(expr)
    xmlRoundTrip(db1)
    xmlRoundTrip(db2)
    xmlRoundTrip(db3)
    xmlRoundTrip(db4)
    xmlRoundTrip(Or())
    xmlRoundTrip(And())
    xmlRoundTrip(t1)
    xmlRoundTrip(Not(t1))
    xmlRoundTrip(OccuranceLimited(99, t1))
  }

  @Test
  def testNormalizeTerm {
    val t1 = Term("foo")

    t1 should be(t1.normalize)
  }

  @Test
  def testNormalizeNot {
    val simple = Not(t1)

    simple.normalize should be(simple)

    assert(Not(t1) === Not(And(And(t1))).normalize)

    assert(t1 === Not(Not(t1)).normalize)
  }

  @Test
  def testNormalizeAnd {
    doTestNormalizeComposeable(And)
  }

  @Test
  def testNormalizeOr {
    doTestNormalizeComposeable(Or)
  }

  @Test
  def testNormalizeMixedComposeable {
    val mixed1 = And(Or(t1, t2), Or(t3, t4))

    mixed1.normalize should be(mixed1)

    val mixed2 = Or(And(t1, t2), And(t3, t4))

    mixed2.normalize should be(mixed2)

    val mixed3 = Or(And(t1, t2), Or(t3, t4))

    assert(Or(And(t1, t2), t3, t4) === mixed3.normalize)

    val mixed4 = And(Or(t1, t2), And(t3, t4))

    assert(And(Or(t1, t2), t3, t4) === mixed4.normalize)
    
    val mixed5 = And(And(t1), And(t2, t3), Or(t4, t5), And(t6, t7), t8)
    
    assert(And(t1, t2, t3, Or(t4, t5), t6, t7, t8) === mixed5.normalize)
    
    val mixed6 = Or(Or(t1), Or(t2, t3), And(t4, t5), Or(t6, t7), t8)
    
    assert(Or(t1, t2, t3, And(t4, t5), t6, t7, t8) === mixed6.normalize)
    
    val mixed7 = And(t1, And(And(t2, t3), Or(t4, t5), And(t6, t7)))
      
    assert(And(t1, t2, t3, Or(t4, t5), t6, t7) === mixed7.normalize)

    val mixed8 = And(And(t1, t2), And(And(t3, t4), Or(t5, t6), And(t7, t8)))
    
    assert(And(t1, t2, t3, t4, Or(t5, t6), t7, t8) === mixed8.normalize)

    val mixed9 = And(Or(t1, t2), And(And(t3, t4), Or(t5, t6), And(t7, t8)))
    
    assert(And(Or(t1, t2), t3, t4, Or(t5, t6), t7, t8) === mixed9.normalize)
  }

  @Test
  def testDateBoundedTimeZonesAreNormalized {
    val db = DateBounded(Some(now), Some(now), And(And(t1))).normalize.asInstanceOf[DateBounded]

    def isUTC(date: Option[XMLGregorianCalendar]): Boolean = date.map(_.getTimeZone(0).getRawOffset == 0).getOrElse(false)

    assert(db.expr === t1)

    isUTC(db.start) should be(true)

    isUTC(db.end) should be(true)

    val db2 = DateBounded(None, Some(now), And(And(t1))).normalize.asInstanceOf[DateBounded]

    isUTC(db2.start) should be(false)

    isUTC(db2.end) should be(true)
  }

  @Test
  def testNormalizeDateBounded {

    assert(t1 === DateBounded(None, None, t1).normalize)

    val db1 = DateBounded(Some(now), None, t1)

    db1.normalize should be(db1)

    val db2 = DateBounded(None, Some(now), t1)

    db2.normalize should be(db2)

    val db3 = DateBounded(Some(now), Some(now), t1)

    db3.normalize should be(db3)

    assert(t1 === DateBounded(None, None, And(And(t1))).normalize)

    val db4 = DateBounded(Some(now), None, And(And(t1)))

    assert(DateBounded(db4.start, db4.end, t1) === db4.normalize)

    val db5 = DateBounded(None, Some(now), And(And(t1)))

    assert(DateBounded(db5.start, db5.end, t1) === db5.normalize)

    val db6 = DateBounded(Some(now), Some(now), And(And(t1)))

    assert(DateBounded(db6.start, db6.end, t1) === db6.normalize)
  }

  @Test
  def testOccuranceLimited {
    intercept[IllegalArgumentException] {
      OccuranceLimited(-1, t1)
    }
  }

  @Test
  def testNormalizeOccuranceLimited {
    assert(t1 === OccuranceLimited(1, t1).normalize)

    assert(t1 === OccuranceLimited(1, And(And(t1))).normalize)

    val o1 = OccuranceLimited(2, t1)

    o1.normalize should be(o1)

    val o2 = OccuranceLimited(99, And(And(t1)))

    assert(OccuranceLimited(99, t1) === o2.normalize)
  }

  @Test
  def testTermToXml {
    assert(<term>{ t1.value }</term>.toString === t1.toXmlString)
  }

  @Test
  def testNotToXml {
    assert(<not><term>{ t1.value }</term></not>.toString === Not(t1).toXmlString)
  }

  @Test
  def testAndToXml {
    assert(<and></and>.toString === And().toXmlString)
    assert(<and><term>{ t1.value }</term></and>.toString === And(t1).toXmlString)
    assert(<and><term>{ t1.value }</term><term>{ t2.value }</term></and>.toString === And(t1, t2).toXmlString)
  }

  @Test
  def testOrToXml {
    assert(<or></or>.toString === Or().toXmlString)
    assert(<or><term>{ t1.value }</term></or>.toString === Or(t1).toXmlString)
    assert(<or><term>{ t1.value }</term><term>{ t2.value }</term></or>.toString === Or(t1, t2).toXmlString)
  }

  @Test
  def testDateBoundedToXml {
    assert(<dateBounded><start/><end/><term>{ t1.value }</term></dateBounded>.toString === DateBounded(None, None, t1).toXmlString)

    val time = now

    assert(<dateBounded><start>{ time.toString }</start><end/><term>{ t1.value }</term></dateBounded>.toString === DateBounded(Some(time), None, t1).toXmlString)
    assert(<dateBounded><start/><end>{ time.toString }</end><term>{ t1.value }</term></dateBounded>.toString === DateBounded(None, Some(time), t1).toXmlString)
    assert(<dateBounded><start>{ time.toString }</start><end>{ time.toString }</end><term>{ t1.value }</term></dateBounded>.toString === DateBounded(Some(time), Some(time), t1).toXmlString)
  }

  @Test
  def testOccuranceLimitedToXml {
    assert(<occurs><min>99</min><term>{ t1.value }</term></occurs>.toString === OccuranceLimited(99, t1).toXmlString)
  }

  private def doTestNormalizeComposeable[T <: Expression](Op: (Expression*) => T) {
    val empty = Op()

    empty should be(empty.normalize)

    val one = Op(t1)

    assert(t1 === one.normalize)

    val two = Op(t1, t2)

    assert(two === two.normalize)

    val nested = Op(Op(t1))

    assert(t1 === nested.normalize)

    val nested2 = Op(Op(t1, t2))

    assert(Op(t1, t2) === nested2.normalize)

    val nested3 = Op(Op(t1, t2), Op(t3, t4))

    assert(Op(t1, t2, t3, t4) === nested3.normalize)

    val nested4 = Op(Op(t1, t2), t3, t4)

    assert(Op(t1, t2, t3, t4) === nested4.normalize)

    val nested5 = Op(t1, t2, Op(t3, t4))

    assert(Op(t1, t2, t3, t4) === nested5.normalize)

    val nested6 = Op(t1, Op(t2, t3), t4)

    assert(Op(t1, t2, t3, t4) === nested6.normalize)

    val nested7 = Op(Op(t1), Op(t2), Op(t3), Op(t4))

    assert(Op(t1, t2, t3, t4) === nested7.normalize)

    val deeplyNested1 = Op(Op(Op(Op(Op(t1)))))

    assert(t1 === deeplyNested1.normalize)

    val deeplyNested2 = Op(Op(Op(Op(Op(t1, t2)))))

    assert(Op(t1, t2) === deeplyNested2.normalize)

    val mixed1 = Op(t1, Op(t2, t3))

    assert(Op(t1, t2, t3) === mixed1.normalize)

    val mixed2 = Op(Op(t2, t3), t4)

    assert(Op(t2, t3, t4) === mixed2.normalize)

    val mixed3 = Op(t1, Op(t2, t3), t4)

    assert(Op(t1, t2, t3, t4) === mixed3.normalize)
  }

  private def jsonRoundTrip(expr: Expression) {
    roundTrip(expr, _.toJson, Expression.fromJson)
  }

  @Test
  def testNotFromJson {
    jsonRoundTrip(Not(Term("hello expression")))
  }

  @Test
  def testAndFromJson {
    jsonRoundTrip(And(Term("a"), Term("hello expression")))
  }

  @Test
  def testOrFromJson {
    jsonRoundTrip(Or(Term("a"), Term("hello expression")))
  }

  @Test
  def testDateBoundedFromJson {
    jsonRoundTrip(DateBounded(None, Some(Util.now), Term("hello")))
  }

  @Test
  def testOccurenceLimitedFromJson {
    jsonRoundTrip(OccuranceLimited(2, Term("hello")))
  }

  @Test
  def testTermComputeHLevelTerm {
    val term1 = Term("\\\\SHRINE\\SHRINE\\Diagnoses\\Congenital anomalies\\Cardiac and circulatory congenital anomalies\\Aortic valve stenosis\\Congenital stenosis of aortic valve\\")
    val term2 = Term("\\\\SHRINE\\SHRINE\\Demographics\\Language\\Bosnian\\")
    val term3 = Term("\\\\SHRINE\\SHRINE\\Demographics\\Age\18-34 years old\\30 years old\\")

    term1.computeHLevel.get should be(5)
    term2.computeHLevel.get should be(3)
    term3.computeHLevel.get should be(3)

    Term("foo").computeHLevel.get should be(0)
    Term("").computeHLevel.get should be(0)
  }

  @Test
  def testQueryComputeHLevelTerm {
    val term1 = Query("\\\\SHRINE\\SHRINE\\Diagnoses\\Congenital anomalies\\Cardiac and circulatory congenital anomalies\\Aortic valve stenosis\\Congenital stenosis of aortic valve\\")
    val term2 = Query("1234567")

    term1.computeHLevel.get should be(0)
    term2.computeHLevel.get should be(0)

    Query("foo").computeHLevel.get should be(0)
    Query("").computeHLevel.get should be(0)
  }

  @Test
  def testQueryToAndFromXml {
    Query("123456").toXmlString should equal(<query>123456</query>.toString)

    xmlRoundTrip(Query("98765"))
  }

  @Test
  def testQueryToAndFromJson {
    jsonRoundTrip(Query("123456"))
  }

  @Test
  def testQueryValue {
    Query("123456").value should equal("masterid:123456")
  }
  
  @Test
  def testComposeableExpressionToIterable {
    val emptyAnd = And()
    val emptyOr = Or()
    
    val q1 = Query("8394723")
    
    for(expr <- Seq(t1, q1, Not(q1), DateBounded(Some(Util.now), Some(Util.now), t1), OccuranceLimited(42, q1))) {
      emptyAnd.toIterable(expr) should equal(Seq(expr))
      emptyOr.toIterable(expr) should equal(Seq(expr))
    }
    
    emptyAnd.toIterable(And(t1, q1)) should equal(Seq(t1, q1))
    emptyAnd.toIterable(Or(t1, q1)) should equal(Seq(Or(t1, q1)))
    
    emptyAnd.toIterable(And(Or(t1, q1))) should equal(Seq(Or(t1, q1)))
    emptyAnd.toIterable(And(t1, q1, Or(t1, q1))) should equal(Seq(t1, q1, Or(t1, q1)))
    emptyAnd.toIterable(And(t1, q1, And(t1, q1))) should equal(Seq(t1, q1, And(t1, q1)))
    
    emptyOr.toIterable(Or(t1, q1)) should equal(Seq(t1, q1))
    emptyOr.toIterable(And(t1, q1)) should equal(Seq(And(t1, q1)))
    
    emptyOr.toIterable(Or(And(t1, q1))) should equal(Seq(And(t1, q1)))
    emptyOr.toIterable(Or(t1, q1, And(t1, q1))) should equal(Seq(t1, q1, And(t1, q1)))
    emptyOr.toIterable(Or(t1, q1, Or(t1, q1))) should equal(Seq(t1, q1, Or(t1, q1)))
  }
  
  @Test
  def testComposeableExpressionEmpty {
    (And()).empty.getClass should equal(classOf[And])
    (And()).empty should equal(And())
    (And()).empty.exprs.isEmpty should be(true)
    
    (Or()).empty.getClass should equal(classOf[Or])
    (Or()).empty should equal(Or())
    (Or()).empty.exprs.isEmpty should be(true)
  }
  
  
  @Test
  def testComposeableExpressionMerge {
    val q1 = Query("8394723")
    
    And() merge And() should equal(And())
    And(t1) merge And(q1) should equal(And(t1, q1))
    And() merge And(t1, q1) should equal(And(t1, q1))
    And(t1, q1) merge And() should equal(And(t1, q1))
    
    Or() merge Or() should equal(Or())
    Or(t1) merge Or(q1) should equal(Or(t1, q1))
    Or() merge Or(t1, q1) should equal(Or(t1, q1))
    Or(t1, q1) merge Or() should equal(Or(t1, q1))
  }
  
  @Test
  def testComposeableExpressionPlusPlus {
    val q1 = Query("8394723")
    
    And() ++ Nil should equal(And())
    And(t1) ++ Seq(q1) should equal(And(t1, q1))
    And() ++ Seq(t1, q1) should equal(And(t1, q1))
    And(t1, q1) ++ Nil should equal(And(t1, q1))
    
    Or() ++ Nil should equal(Or())
    Or(t1) ++ Seq(q1) should equal(Or(t1, q1))
    Or() ++ Seq(t1, q1) should equal(Or(t1, q1))
    Or(t1, q1) ++ Nil should equal(Or(t1, q1))
  }
  
  @Test
  def testComposeableExpressionContainsA {
    val expr = And(t1, t2, Or(t3, Query("12345")))
    
    expr.containsA[And] should be(false)
    expr.containsA[Query] should be(false)
    expr.containsA[Term] should be(true)
    expr.containsA[Or] should be(true)
  }
}