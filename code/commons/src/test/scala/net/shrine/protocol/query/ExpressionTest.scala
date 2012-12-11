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
import net.shrine.util.Try
import org.scalatest.junit.ShouldMatchersForJUnit

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
  private[this] val t1 = Term("foo")
  private[this] val t2 = Term("bar")
  private[this] val t3 = Term("baz")
  private[this] val t4 = Term("blarg")
  private[this] val t5 = Term("nuh")
  private[this] val t6 = Term("zuh")

  import Utils.now

  private def doToExecutionPlanTest(expr: Expression, expected: ExecutionPlan) {
    expr.toExecutionPlan should equal(expected)
  }
  
  @Test
  def testOrToExectutionPlan {
    //Plain old or, no need for sub-queries
    {
      val expr = Or(t1, t2)

      val expected = SimpleQuery(Or(t1, t2))
      
      expr.toExecutionPlan should equal(expected)
    }
    
    //Or of 2 Ands
    {
      val expr = Or(And(t1, t2), And(t3, t4))

      val expected = CompoundQuery(Conjunction.Or, SimpleQuery(And(t1, t2)), SimpleQuery(And(t3, t4)))

      expr.toExecutionPlan should equal(expected)
    }
    
    //And Ored with a Term 
    {
      val expr = Or(And(t1, t2), t3)

      val expected = CompoundQuery(Conjunction.Or, SimpleQuery(And(t1, t2)), SimpleQuery(t3))
      
      expr.toExecutionPlan should equal(expected)
    }
    
    //And Ored with an Or
    {
      val expr = Or(And(t1, t2), Or(t3, t4))

      val expected = CompoundQuery(Conjunction.Or, SimpleQuery(And(t1, t2)), SimpleQuery(Or(t3, t4)))
      
      expr.toExecutionPlan should equal(expected)
    }
    
    //nested Ors should be normalized first 
    {
      val expr = Or(Or(t1, t2), Or(t3, t4))
      
      val expected = SimpleQuery(Or(t1, t2, t3, t4))
      
      expr.toExecutionPlan should equal(expected)
    }
    
    {
      val expr = Or(Or(t1, t2), And(t3, t4), Or(t5, t6))
      
      val expected = CompoundQuery(Conjunction.Or, SimpleQuery(And(t3, t4)), SimpleQuery(Or(t1, t2, t5, t6)))
      
      expr.toExecutionPlan should equal(expected)
    }
  }
  
  @Test
  def testAndToExecutionPlan {
    //Plain old And, no need for sub-queries
    {
      val expr = And(t1, t2)

      val expected = SimpleQuery(And(t1, t2))
      
      expr.toExecutionPlan should equal(expected)
    }
    
    //nested Ands should be normalized first 
    {
      val expr = And(And(t1, t2), And(t3, t4))
      
      val expected = SimpleQuery(And(t1, t2, t3, t4))
      
      expr.toExecutionPlan should equal(expected)
    }
  }

  private type HasWithExpr[T] = {
    val expr: Expression
    def withExpr(expr: Expression): T
  }

  private def doTestWithExpr[T <: AnyRef with HasWithExpr[T]](o: HasWithExpr[T]) {
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

  @Test
  def testExpressionFromXml {
    def xmlRoundTrip(expr: Expression) {
      roundTrip(expr, _.toXml, Expression.fromXml)
    }

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
    assert(<term>{ t1.value }</term>.toString === t1.toXml.toString)
  }

  @Test
  def testNotToXml {
    assert(<not><term>{ t1.value }</term></not>.toString === Not(t1).toXml.toString)
  }

  @Test
  def testAndToXml {
    assert(<and/>.toString === And().toXml.toString)
    assert(<and><term>{ t1.value }</term></and>.toString === And(t1).toXml.toString)
    assert(<and><term>{ t1.value }</term><term>{ t2.value }</term></and>.toString === And(t1, t2).toXml.toString)
  }

  @Test
  def testOrToXml {
    assert(<or/>.toString === Or().toXml.toString)
    assert(<or><term>{ t1.value }</term></or>.toString === Or(t1).toXml.toString)
    assert(<or><term>{ t1.value }</term><term>{ t2.value }</term></or>.toString === Or(t1, t2).toXml.toString)
  }

  @Test
  def testDateBoundedToXml {
    assert(<dateBounded><start/><end/><term>{ t1.value }</term></dateBounded>.toString === DateBounded(None, None, t1).toXml.toString)

    val time = now

    assert(<dateBounded><start>{ time.toString }</start><end/><term>{ t1.value }</term></dateBounded>.toString === DateBounded(Some(time), None, t1).toXml.toString)
    assert(<dateBounded><start/><end>{ time.toString }</end><term>{ t1.value }</term></dateBounded>.toString === DateBounded(None, Some(time), t1).toXml.toString)
    assert(<dateBounded><start>{ time.toString }</start><end>{ time.toString }</end><term>{ t1.value }</term></dateBounded>.toString === DateBounded(Some(time), Some(time), t1).toXml.toString)
  }

  @Test
  def testOccuranceLimitedToXml {
    assert(<occurs><min>99</min><term>{ t1.value }</term></occurs>.toString === OccuranceLimited(99, t1).toXml.toString)
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
    jsonRoundTrip(DateBounded(None, Some(new NetworkTime().getXMLGregorianCalendar), Term("hello")))
  }

  @Test
  def testOccurenceLimitedFromJson {
    jsonRoundTrip(OccuranceLimited(2, Term("hello")))
  }
}