package net.shrine.broadcaster.dao.hibernate;

import javax.persistence.*;
import java.util.Date;


@Entity
@Table(name = "AUDIT_ENTRY")
public class AuditEntry {


    private long auditEntryId;

    private String username;
    private String domain;
    private Date time;
    private String queryText;
    private String project;
    private String queryTopic;


    public AuditEntry() {
    }

    public AuditEntry(String project, String domain, String username, String queryText, String queryTopic) {
        this.username = username;
        this.domain = domain;
        this.queryText = queryText;
        this.project = project;
        this.queryTopic = queryTopic;
        this.time = new Date();
    }

    @Id
    @GeneratedValue(strategy= GenerationType.AUTO)
    @Column(name = "AUDIT_ENTRY_ID")
    public long getAuditEntryId() {
        return auditEntryId;
    }

    protected void setAuditEntryId(long auditEntryId){
        this.auditEntryId = auditEntryId;
    }

    @Column(name = "QUERY_TOPIC")
    public String getQueryTopic() {
        return queryTopic;
    }

    public void setQueryTopic(String queryTopic) {
        this.queryTopic = queryTopic;
    }

    @Column(name = "PROJECT")
    public String getProject() {
        return project;
    }

    public void setProject(String project) {
        this.project = project;
    }

    @Column(name = "USERNAME")
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    @Column(name = "DOMAIN_NAME")
    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "TIME")
    public Date getTime() {
        return time;
    }

    public void setTime(Date time) {
        this.time = time;
    }

    @Column(name = "QUERY_TEXT", columnDefinition = "TEXT")
    public String getQueryText() {
        return queryText;
    }

    public void setQueryText(String queryText) {
        this.queryText = queryText;
    }
}
