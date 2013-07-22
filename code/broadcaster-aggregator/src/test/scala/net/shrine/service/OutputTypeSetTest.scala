package net.shrine.service
import junit.framework.TestCase
import org.scalatest.junit.AssertionsForJUnit
import org.scalatest.junit.ShouldMatchersForJUnit
import net.shrine.protocol.ResultOutputType
import javax.ws.rs.WebApplicationException
import javax.ws.rs.core.Response.Status

/**
 *
 * @author Clint Gilbert
 * @date Sep 20, 2011
 *
 * @link http://cbmi.med.harvard.edu
 *
 * This software is licensed under the LGPL
 * @link http://www.gnu.org/licenses/lgpl.html
 *
 */
final class OutputTypeSetTest extends TestCase with AssertionsForJUnit with ShouldMatchersForJUnit {

  import scala.collection.JavaConversions._

  private val possibleOutputTypeSets = Seq(ResultOutputType.values.toSet,
    Set(ResultOutputType.PATIENT_COUNT_XML),
    Set(ResultOutputType.PATIENTSET),
    Set.empty[ResultOutputType])

  def testConstructorAndToSet {
    possibleOutputTypeSets.foreach { outputTypes =>

      val outputTypeSet = new OutputTypeSet(outputTypes)

      outputTypeSet.toSet should equal(outputTypes)
    }

    intercept[IllegalArgumentException] {
      val nullSet: Set[ResultOutputType] = null

      new OutputTypeSet(nullSet)
    }
  }

  def testStringConstructorAndSerialized {
    possibleOutputTypeSets.foreach { outputTypes =>
      val outputTypeSet = new OutputTypeSet(outputTypes)

      val serialized = outputTypeSet.serialized

      serialized should not be (null)

      val roundTripped = new OutputTypeSet(serialized)

      roundTripped should equal(outputTypeSet)
    }

    def shouldGiveBadRequest(f: => Any) {
      try {
        f

        fail("Should have thrown")
      } catch {
        case e: WebApplicationException => e.getResponse.getStatus should equal(Status.BAD_REQUEST.getStatusCode)
      }
    }

    shouldGiveBadRequest {
      val nullString: String = null

      new OutputTypeSet(nullString)
    }

    shouldGiveBadRequest {
      new OutputTypeSet("jkasdhkjashdjks")
    }
  }

  def testSerialized {
    import ResultOutputType._

    new OutputTypeSet(Set.empty[ResultOutputType]).serialized should equal("")

    new OutputTypeSet(Set(PATIENT_COUNT_XML, PATIENTSET)).serialized should equal("PATIENT_COUNT_XML%2CPATIENTSET")

    ResultOutputType.values.foreach { outputType =>
      new OutputTypeSet(Set(outputType)).serialized should equal(outputType.name)
    }
  }

  def testDeserialize {
    import OutputTypeSet.deserialize
    import ResultOutputType._

    intercept[WebApplicationException] {
      deserialize(null)
    }

    deserialize("") should equal(Set.empty[ResultOutputType])

    ResultOutputType.values.foreach { outputType =>
      deserialize(outputType.name) should equal(Set(outputType))
    }

    val allOutputTypes = ResultOutputType.values.toSet

    deserialize("PATIENT_COUNT_XML%2CPATIENTSET") should equal(allOutputTypes)
    deserialize("PATIENTSET%2CPATIENT_COUNT_XML") should equal(allOutputTypes)
  }
}