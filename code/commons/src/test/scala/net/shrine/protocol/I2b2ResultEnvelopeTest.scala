package net.shrine.protocol

import junit.framework.TestCase
import org.scalatest.junit.AssertionsForJUnit
import org.scalatest.matchers.ShouldMatchers
import org.junit.Test
import net.shrine.protocol.I2b2ResultEnvelope.Column

/**
 * @author clint
 * @date Aug 15, 2012
 */
final class I2b2ResultEnvelopeTest extends TestCase with AssertionsForJUnit with ShouldMatchers {
  private val tuples = Seq("x" -> 1L, "y" -> 2L, "z" -> 3L)
  
  @Test
  def testPlusPlus {
    val resultType = ResultOutputType.PATIENT_RACE_COUNT_XML
    
    val env = new I2b2ResultEnvelope(resultType)
    
    val envWithData = env ++ tuples
    
    (env eq envWithData) should not be(true)
    
    env.toMap should equal(Map.empty)
    
    envWithData.toMap should equal(Map(tuples: _*))
    
    val anotherEnvWithData = env ++ Map(tuples: _*)
    
    env.toMap should equal(Map.empty)
    
    anotherEnvWithData.toMap should equal(Map(tuples: _*))
  }
  
  @Test
  def testConstructorTupleVarargs {
    val resultType = ResultOutputType.PATIENT_GENDER_COUNT_XML
    
    val env = new I2b2ResultEnvelope(resultType, tuples: _*)
    
    env.resultType should equal(resultType)
    
    env.toMap should equal(Map(tuples: _*))
  }
  
  @Test
  def testFromI2b2 {
    val xml = I2b2Workarounds.unescape("""&lt;?xml version="1.0" encoding="UTF-8" standalone="yes"?>
&lt;ns10:i2b2_result_envelope xmlns:ns2="http://www.i2b2.org/xsd/hive/pdo/1.1/" xmlns:ns4="http://www.i2b2.org/xsd/cell/crc/psm/1.1/" xmlns:ns3="http://www.i2b2.org/xsd/cell/crc/pdo/1.1/" xmlns:ns9="http://www.i2b2.org/xsd/cell/ont/1.1/" xmlns:ns5="http://www.i2b2.org/xsd/hive/msg/1.1/" xmlns:ns6="http://www.i2b2.org/xsd/cell/crc/psm/querydefinition/1.1/" xmlns:ns10="http://www.i2b2.org/xsd/hive/msg/result/1.1/" xmlns:ns7="http://www.i2b2.org/xsd/cell/crc/psm/analysisdefinition/1.1/" xmlns:ns8="http://www.i2b2.org/xsd/cell/pm/1.1/">
    &lt;body>
        &lt;ns10:result name="PATIENT_AGE_COUNT_XML">
            &lt;data type="int" column="  0-9 years old">0&lt;/data>
            &lt;data type="int" column="  10-17 years old">11&lt;/data>
            &lt;data type="int" column="  18-34 years old">26&lt;/data>
            &lt;data type="int" column="  35-44 years old">26&lt;/data>
            &lt;data type="int" column="  45-54 years old">8&lt;/data>
            &lt;data type="int" column="  55-64 years old">6&lt;/data>
            &lt;data type="int" column="  65-74 years old">5&lt;/data>
            &lt;data type="int" column="  75-84 years old">0&lt;/data>
            &lt;data type="int" column="&amp;gt;= 65 years old">5&lt;/data>
            &lt;data type="int" column="&amp;gt;= 85 years old">0&lt;/data>
            &lt;data type="int" column="Not recorded">0&lt;/data>
        &lt;/ns10:result>
    &lt;/body>
&lt;/ns10:i2b2_result_envelope>""")

    val env = I2b2ResultEnvelope.fromI2b2(xml).get
    
    env.resultType should be(ResultOutputType.PATIENT_AGE_COUNT_XML)
    
    val expected = Map("  0-9 years old" -> 0,
                       "  10-17 years old" -> 11,
                       "  18-34 years old" -> 26,
                       "  35-44 years old" -> 26,
                       "  45-54 years old" -> 8,
                       "  55-64 years old" -> 6,
                       "  65-74 years old" -> 5,
                       "  75-84 years old" -> 0,
                       ">= 65 years old" -> 5,
                       ">= 85 years old" -> 0,
                       "Not recorded" -> 0)
                       
    env.data should equal(expected)
    
    val badXml1 = I2b2Workarounds.unescape("""&lt;?xml version="1.0" encoding="UTF-8" standalone="yes"?>
&lt;ns10:i2b2_result_envelope xmlns:ns2="http://www.i2b2.org/xsd/hive/pdo/1.1/" xmlns:ns4="http://www.i2b2.org/xsd/cell/crc/psm/1.1/" xmlns:ns3="http://www.i2b2.org/xsd/cell/crc/pdo/1.1/" xmlns:ns9="http://www.i2b2.org/xsd/cell/ont/1.1/" xmlns:ns5="http://www.i2b2.org/xsd/hive/msg/1.1/" xmlns:ns6="http://www.i2b2.org/xsd/cell/crc/psm/querydefinition/1.1/" xmlns:ns10="http://www.i2b2.org/xsd/hive/msg/result/1.1/" xmlns:ns7="http://www.i2b2.org/xsd/cell/crc/psm/analysisdefinition/1.1/" xmlns:ns8="http://www.i2b2.org/xsd/cell/pm/1.1/">
    &lt;body>
        &lt;ns10:result name="PATIENT_AGE_COUNT_XML_JKALSHFKAJSFHKASHFJKSAHFKHJH">
            &lt;data type="int" column="  0-9 years old">0&lt;/data>
            &lt;data type="int" column="  10-17 years old">11&lt;/data>
            &lt;data type="int" column="  18-34 years old">26&lt;/data>
            &lt;data type="int" column="  35-44 years old">26&lt;/data>
            &lt;data type="int" column="  45-54 years old">8&lt;/data>
            &lt;data type="int" column="  55-64 years old">6&lt;/data>
            &lt;data type="int" column="  65-74 years old">5&lt;/data>
            &lt;data type="int" column="  75-84 years old">0&lt;/data>
            &lt;data type="int" column="&amp;gt;= 65 years old">5&lt;/data>
            &lt;data type="int" column="&amp;gt;= 85 years old">0&lt;/data>
            &lt;data type="int" column="Not recorded">0&lt;/data>
        &lt;/ns10:result>
    &lt;/body>
&lt;/ns10:i2b2_result_envelope>""")
      
      I2b2ResultEnvelope.fromI2b2(badXml1) should be(None)
    
     val badXml2 = I2b2Workarounds.unescape("""&lt;?xml version="1.0" encoding="UTF-8" standalone="yes"?>
&lt;ns10:i2b2_result_envelope xmlns:ns2="http://www.i2b2.org/xsd/hive/pdo/1.1/" xmlns:ns4="http://www.i2b2.org/xsd/cell/crc/psm/1.1/" xmlns:ns3="http://www.i2b2.org/xsd/cell/crc/pdo/1.1/" xmlns:ns9="http://www.i2b2.org/xsd/cell/ont/1.1/" xmlns:ns5="http://www.i2b2.org/xsd/hive/msg/1.1/" xmlns:ns6="http://www.i2b2.org/xsd/cell/crc/psm/querydefinition/1.1/" xmlns:ns10="http://www.i2b2.org/xsd/hive/msg/result/1.1/" xmlns:ns7="http://www.i2b2.org/xsd/cell/crc/psm/analysisdefinition/1.1/" xmlns:ns8="http://www.i2b2.org/xsd/cell/pm/1.1/">
    &lt;body>
        &lt;ns10:result name="PATIENT_AGE_COUNT_XML">
            &lt;foo type="int" column="  0-9 years old">0&lt;/foo>
        &lt;/ns10:result>
    &lt;/body>
&lt;/ns10:i2b2_result_envelope>""")
       
     I2b2ResultEnvelope.fromI2b2(badXml2).get.columns should equal(Nil)
  }

  @Test
  def testPlusColumn {
    val resultType = ResultOutputType.PATIENT_COUNT_XML

    val empty = I2b2ResultEnvelope.empty(resultType)

    val column1 = Column("foo", 123)

    val env1 = empty + column1

    (env1 eq empty) should be(false)

    env1.resultType should be(resultType)

    env1.columns should equal(Seq(column1))

    val column2 = Column("nuh", 123)

    val env2 = env1 + column2

    (env2 eq env1) should be(false)

    env2.resultType should be(resultType)
    env2.columns should equal(Seq(column1, column2))
  }

  @Test
  def testPlusTuple {
    val resultType = ResultOutputType.PATIENT_COUNT_XML

    val empty = I2b2ResultEnvelope.empty(resultType)

    val env = empty + ("foo" -> 123L)

    (env eq empty) should be(false)

    env.resultType should be(resultType)

    env.columns should equal(Seq(Column("foo", 123)))
  }

  @Test
  def testEmpty {
    for (resultType <- ResultOutputType.values) {
      val env1 = I2b2ResultEnvelope.empty(resultType)

      env1 should not be (null)
      env1.resultType should be(resultType)
      env1.columns.isEmpty should be(true)

      val env2 = I2b2ResultEnvelope.empty(resultType)

      (env1 eq env2) should be(false)
    }
  }
  
  @Test
  def testToMap {
    val resultType = ResultOutputType.PATIENT_COUNT_XML
    
    val env = new I2b2ResultEnvelope(resultType, Seq(Column("foo", 123L), Column("bar", 99L)))
    
    env.toMap should equal(Map("foo" -> 123L, "bar" -> 99L))
  }
}