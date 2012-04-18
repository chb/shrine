package net.shrine.broadcaster.dao.hibernate

import net.shrine.broadcaster.dao.AuditDAO
import org.hibernate.Session
import org.hibernate.SessionFactory
import org.hibernate.criterion.Order
import org.springframework.stereotype.Repository

import javax.annotation.Resource

import java.util.{ List => JList }

/**
 * @author ??
 * @author Clint Gilbert
 * @date ??
 * Ported to Scala on Feb 28, 2012
 * 
 * DAO that reads and writes audit entries
 */
@Repository
class HibernateAuditDAO extends AuditDAO {

  @Resource
  private var sessionFactory: SessionFactory = _

  override def addAuditEntry(auditEntry: AuditEntry) {
    withCurrentSession(_.saveOrUpdate(auditEntry))
  }

  override def findRecentEntries(limit: Int): Seq[AuditEntry] = {
    import scala.collection.JavaConverters._

    withCurrentSession { session =>
      session.createCriteria(classOf[AuditEntry]).addOrder(Order.desc("time")).setMaxResults(limit).list().asInstanceOf[JList[AuditEntry]].asScala
    }
  }

  private[this] def withCurrentSession[T](body: Session => T) = body(sessionFactory.getCurrentSession)
}
