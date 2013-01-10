package net.shrine.adapter.dao.scalaquery

import org.springframework.test.AbstractDependencyInjectionSpringContextTests
import net.shrine.adapter.AdapterDbTest
import org.scalatest.junit.ShouldMatchersForJUnit
import org.spin.tools.crypto.signature.Identity
import org.junit.Test
import net.shrine.adapter.dao.scalaquery.tables.PrivilegedUsers
import org.scalaquery.ql._
import org.scalaquery.session.Session
import net.shrine.protocol.Credential
import net.shrine.protocol.AuthenticationInfo
import net.shrine.protocol.query.Term
import net.shrine.protocol.RunQueryResponse
import net.shrine.protocol.RunQueryResponse
import net.shrine.util.Util
import net.shrine.protocol.query.QueryDefinition
import net.shrine.protocol.QueryResult
import net.shrine.protocol.ResultOutputType
import java.util.Calendar
import org.spin.tools.NetworkTime
import javax.xml.datatype.XMLGregorianCalendar
import net.shrine.adapter.dao.scalaquery.tables.DateHelpers
import net.shrine.protocol.RawCrcRunQueryResponse

/**
 * @author clint
 * @date Nov 19, 2012
 *
 * Ported
 */
final class PrivilegedUsersTest extends AbstractDependencyInjectionSpringContextTests with AdapterDbTest with ShouldMatchersForJUnit {

  private val testDomain = "testDomain"
  private val testUsername = "testUsername"
  private val testId = new Identity(testDomain, testUsername)
  private val testThreshold = 3
  private val defaultThreshold = 10

  @Test
  def testGetUserThreshold = afterInsertingTestUser {
    val threshold = database.withSession { implicit session: Session =>
      userThresholdQuery(testId.getUsername, testId.getDomain).first
    }

    threshold should equal(testThreshold)
  }

  @Test
  def testIsUserWithNoThresholdEntryLockedOut = afterInsertingTestUser {
    val username = "noEntry"
    val domain = "noEntryDomain"

    val noThresholdId1 = new Identity(testDomain, username)
    val noThresholdId2 = new Identity(domain, testUsername)

    for (noThresholdId <- Seq(noThresholdId1, noThresholdId2)) {
      dao.isUserLockedOut(noThresholdId, defaultThreshold) should be(false)

      lockoutUser(noThresholdId, 42)

      dao.isUserLockedOut(noThresholdId, defaultThreshold) should be(false)
    }
  }

  @Test
  def testIsUserLockedOut = afterInsertingTestUser {
    dao.isUserLockedOut(testId, defaultThreshold) should be(false)

    logCountQueryResult("masterId:0", 0, testId, 42)

    dao.isUserLockedOut(testId, defaultThreshold) should be(false)

    lockoutUser(testId, 42)

    dao.isUserLockedOut(testId, defaultThreshold) should be(true)

    // Make sure username + domain is how users are identified
    dao.isUserLockedOut(new Identity(testDomain, "some-other-username"), defaultThreshold) should be(false)
    dao.isUserLockedOut(new Identity("some-other-domain", testUsername), defaultThreshold) should be(false)
  }

  @Test
  def testIsUserLockedOutWithResultSetSizeOfZero = afterInsertingTestUser {
    dao.isUserLockedOut(testId, defaultThreshold) should be(false)

    logCountQueryResult("masterId:0", 0, testId, 0)

    dao.isUserLockedOut(testId, defaultThreshold) should be(false)

    lockoutUser(testId, 0)

    // user should not be locked out
    dao.isUserLockedOut(testId, defaultThreshold) should be(false)
  }

  @Test
  def testLockoutOverride = afterInsertingTestUser {
    dao.isUserLockedOut(testId, defaultThreshold) should be(false)

    lockoutUser(testId, 42)

    dao.isUserLockedOut(testId, defaultThreshold) should be(true)

    val tomorrow = DateHelpers.daysFromNow(1)

    setOverrideDate(testId, tomorrow)

    dao.isUserLockedOut(testId, defaultThreshold) should be(false)

    val thirtyOneDaysAgo = DateHelpers.daysFromNow(-31)

    setOverrideDate(testId, thirtyOneDaysAgo)
  }

  private def setOverrideDate(id: Identity, newOverrideDate: XMLGregorianCalendar) {
    val d = driver
    import d.Implicit._

    val updateQuery = for {
      user <- PrivilegedUsers
      if user.username === id.getUsername
      if user.domain === id.getDomain
    } yield user.overrideDate
    
    database.withSession { implicit session: Session => 
      updateQuery.update(newOverrideDate)
    }
  }
  
  private def lockoutUser(lockedOutId: Identity, resultSetSize: Int) {
    for (i <- 1 until testThreshold + 2) {
      logCountQueryResult("masterId:" + i, i, lockedOutId, resultSetSize)
    }
  }

  private def logCountQueryResult(masterId: String, networkQueryId: Int, lockedOutId: Identity, resultSetSize: Int) {
    val now = Util.now

    val expr = Term("blah")

    val queryDef = QueryDefinition("foo", expr)

    val authn = AuthenticationInfo(lockedOutId.getDomain, lockedOutId.getUsername, Credential("asjkhdad", false))

    val insertedQueryId = dao.insertQuery(masterId, networkQueryId, "foo", authn, expr)

    import ResultOutputType.PATIENT_COUNT_XML;

    val queryResult = QueryResult(999, 123, Option(PATIENT_COUNT_XML), resultSetSize, Option(now), Option(now), None, QueryResult.StatusType.Finished, None)

    val insertedResultIds = dao.insertQueryResults(insertedQueryId, Seq(queryResult))

    dao.insertCountResult(insertedResultIds(PATIENT_COUNT_XML).head, resultSetSize, resultSetSize + 1)
    
    dao.findResultsFor(networkQueryId).get.count.get.originalValue should equal(resultSetSize)
  }

  private def userThresholdQuery = {
    val d = driver
    import d.Implicit._

    for {
      username ~ domain <- Parameters[String, String]
      user <- PrivilegedUsers
      if user.username === username
      if user.domain === domain
    } yield user.threshold
  }

  private def insertTestUser() {
    val d = driver
    import d.Implicit._

    database.withSession { implicit session: Session =>
      //TODO: Fix once PrivilegedUsers.overrideDate is nullable; currently bogus override date equal to default is added. :(
      //PrivilegedUsers.withoutId.insert(testUsername, testDomain, testThreshold, None)
      PrivilegedUsers.withoutId.insert(testUsername, testDomain, testThreshold, DateHelpers.daysFromNow(-30))
    }
  }

  private def afterInsertingTestUser(body: => Any): Unit = afterCreatingTables {
    insertTestUser()

    body
  }
}