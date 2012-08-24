package net.shrine.util

import junit.framework.TestCase
import org.scalatest.junit.AssertionsForJUnit
import org.scalatest.matchers.ShouldMatchers
import org.junit.Test

/**
 * @author clint
 * @date Aug 24, 2012
 */
object SEnumTest {
  final class TestEnum private (val name: String) extends TestEnum.Value
  
  object TestEnum extends SEnum[TestEnum] {
    val Foo = new TestEnum("Foo")
    val Bar = new TestEnum("Bar")
    val Baz = new TestEnum("Baz")
  }
}

final class SEnumTest extends TestCase with AssertionsForJUnit with ShouldMatchers {
  import SEnumTest._
  
  @Test
  def testValues {
    import TestEnum._
    
    TestEnum.values should equal(Seq(Foo, Bar, Baz))
    
    Foo should not be(Bar)
    Bar should not be(Foo)
    
    Bar should not be(Baz)
    Baz should not be(Bar)
    
    Baz should not be(Foo)
    Foo should not be(Baz)
  }
  
  @Test
  def testValueOf {
    import TestEnum._
    
    valueOf("ajklshdkalshjals") should equal(None)
    
    valueOf("Foo").get should equal(Foo)
    valueOf("Bar").get should equal(Bar)
    valueOf("Baz").get should equal(Baz)
    
    valueOf("FoO").get should equal(Foo)
    valueOf("bAz").get should equal(Baz)
  }
  
  @Test
  def testNameAndToString {
    import TestEnum._

    Foo.name should equal("Foo")
    Foo.toString should equal("Foo")
    
    Bar.name should equal("Bar")
    Bar.toString should equal("Bar")
    
    Baz.name should equal("Baz")
    Baz.toString should equal("Baz")
  }
  
  @Test
  def testOrdinal {
    import TestEnum._

    Foo.ordinal should equal(0)
    
    Bar.ordinal should equal(1)
    
    Baz.ordinal should equal(2)
  }
  
  @Test
  def testCompareAndOrdering {
    import TestEnum.{Foo, Bar, Baz}

    Seq(Bar, Baz, Foo).sorted should equal(Seq(Foo, Bar, Baz))
  }
}