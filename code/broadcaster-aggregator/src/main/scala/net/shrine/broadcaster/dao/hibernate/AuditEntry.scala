package net.shrine.broadcaster.dao.hibernate

import java.util.Date
import scala.reflect.BeanProperty
import javax.persistence.Entity
import javax.persistence.Table
import javax.persistence.Id
import javax.persistence.GeneratedValue
import javax.persistence.Column
import javax.persistence.GenerationType
import javax.persistence.Temporal
import javax.persistence.TemporalType

/**
 * @author ??
 * @author Clint Gilbert
 * @date ??
 * ported to Scala on Feb 28, 2012
 * 
 * https://cbmi.med.harvard.edu/
 */
object AuditEntry {
  //Hibernate needs a no-arg constructor, and properties must be declared inline (see below) so we define
  //a factory method to enable a common instantiation case.
  def apply(project: String, domain: String, username: String, queryText: String, queryTopic: String) = {
    val result = new AuditEntry
    
    result.project = project
    result.domain = domain
    result.username = username
    result.queryText = queryText
    result.queryTopic = queryTopic
    
    result
  }
}

@Entity
@Table(name = "AUDIT_ENTRY")
class AuditEntry {
  //NB: properties must be declared inline for persistence annotations to "take" 
  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  @Column(name = "AUDIT_ENTRY_ID")
  @BeanProperty
  var auditEntryId: Long = _

  @Column(name = "USERNAME")
  @BeanProperty
  var username: String = _

  @Column(name = "DOMAIN_NAME")
  @BeanProperty
  var domain: String = _

  @Temporal(TemporalType.TIMESTAMP)
  @Column(name = "TIME")
  @BeanProperty
  var time: Date = new Date

  @Column(name = "QUERY_TEXT", columnDefinition = "TEXT")
  @BeanProperty
  var queryText: String = _

  @Column(name = "PROJECT")
  @BeanProperty
  var project: String = _

  @Column(name = "QUERY_TOPIC")
  @BeanProperty
  var queryTopic: String = _
  
  //NB: hashCode and equals implemented in terms of the @Id property, as Hibernate expects
  override def hashCode: Int = auditEntryId.hashCode
  
  override def equals(that: Any): Boolean = {
    val canEqual = that != null && that.isInstanceOf[AuditEntry]
    
    canEqual && that.asInstanceOf[AuditEntry].auditEntryId == this.auditEntryId
  }
}
