package net.shrine.broadcaster.dao


import net.shrine.broadcaster.dao.hibernate.AuditEntry
import org.junit.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.AbstractTransactionalDataSourceSpringContextTests
import java.util.Date
import org.scalatest.matchers.ShouldMatchers

/**
 * @author ??
 * @author Clint Gilbert
 * @date ??
 * Ported to Scala on Feb 28, 2012
 * 
 */
final class AuditDAOTest extends AbstractTransactionalDataSourceSpringContextTests with ShouldMatchers {

    @Autowired
    private var auditDao: AuditDAO = _

    override protected def getConfigLocations: Array[String] = Array("classpath:testApplicationContext.xml")

    @Test
    def testGetRecentEntries {
        val limit = 10
        val entries = auditDao.findRecentEntries(limit)
        entries should not be(null)
        entries.isEmpty should be(false)
        entries.size should equal(limit)
        assertDateInDescendingOrder(entries)
    }

    private def assertDateInDescendingOrder(entries: Seq[AuditEntry]) {
      for(Seq(left, right) <- entries.sliding(2)) {
        (left.getTime.getTime > right.getTime.getTime) should be(true)
      }
      
      entries should equal(entries.sortBy(_.getTime.getTime).reverse)
    }

    override protected def onSetUpInTransaction() {
      for(i <- 0 until 20) { 
    	auditDao.addAuditEntry(newAuditEntry(new Date(1000 * i))) 
      }
    }

    private def newAuditEntry(date: Date): AuditEntry = {
        val result = AuditEntry("project", "domain", "username", "query", "topic")
        
        result.time = date
        
        //Test AuditEntry.apply(), since it's convenient to do so here.
        result.project should equal("project")
        result.domain should equal("domain")
        result.username should equal("username")
        result.queryText should equal("query")
        result.queryTopic should equal("topic")
        result.time should equal(date)
        
        result
    }
}
