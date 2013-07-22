package net.shrine.adapter.dao.hibernate.entity;

import javax.persistence.*;
import java.sql.Timestamp;
import java.util.Date;

/**
 * Auto-generated Hibernate Entity
 */
@IdClass(RequestResponseDataEntityPK.class)
@Table(name = "REQUEST_RESPONSE_DATA")
@Entity
public class RequestResponseDataEntity {
    private String domainName;

    @Column(name = "DOMAIN_NAME")
    @Id
    public String getDomainName() {
        return domainName;
    }

    public void setDomainName(String domainName) {
        this.domainName = domainName;
    }

    private String username;

    @Column(name = "USERNAME")
    @Id
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    private long broadcastQueryMasterId;

    @Column(name = "BROADCAST_QUERY_MASTER_ID")
    @Id
    public long getBroadcastQueryMasterId() {
        return broadcastQueryMasterId;
    }

    public void setBroadcastQueryMasterId(long broadcastQueryMasterId) {
        this.broadcastQueryMasterId = broadcastQueryMasterId;
    }

    private long broadcastQueryInstanceId;

    @Column(name = "BROADCAST_QUERY_INSTANCE_ID")
    @Id
    public long getBroadcastQueryInstanceId() {
        return broadcastQueryInstanceId;
    }

    public void setBroadcastQueryInstanceId(long broadcastQueryInstanceId) {
        this.broadcastQueryInstanceId = broadcastQueryInstanceId;
    }

    private long broadcastResultInstanceId;

    @Column(name = "BROADCAST_RESULT_INSTANCE_ID")
    @Id
    public long getBroadcastResultInstanceId() {
        return broadcastResultInstanceId;
    }

    public void setBroadcastResultInstanceId(long broadcastResultInstanceId) {
        this.broadcastResultInstanceId = broadcastResultInstanceId;
    }

    private String resultStatus;

    @Column(name = "RESULT_STATUS")
    @Basic
    public String getResultStatus() {
        return resultStatus;
    }

    public void setResultStatus(String resultStatus) {
        this.resultStatus = resultStatus;
    }

    private int resultSetSize;

    @Column(name = "RESULT_SET_SIZE")
    @Basic
    public int getResultSetSize() {
        return resultSetSize;
    }

    public void setResultSetSize(int resultSetSize) {
        this.resultSetSize = resultSetSize;
    }

    private int timeElapsed;

    @Column(name = "TIME_ELAPSED")
    @Basic
    public int getTimeElapsed() {
        return timeElapsed;
    }

    public void setTimeElapsed(int timeElapsed) {
        this.timeElapsed = timeElapsed;
    }

    private String spinQueryId;

    @Column(name = "SPIN_QUERY_ID")
    @Basic
    public String getSpinQueryId() {
        return spinQueryId;
    }

    public void setSpinQueryId(String spinQueryId) {
        this.spinQueryId = spinQueryId;
    }

    private String resultXml;

    @Column(name = "RESULT_XML", length = 32000)
    @Basic
    public String getResultXml() {
        return resultXml;
    }

    public void setResultXml(String resultXml) {
        this.resultXml = resultXml;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        RequestResponseDataEntity that = (RequestResponseDataEntity) o;

        if (broadcastQueryInstanceId != that.broadcastQueryInstanceId) return false;
        if (broadcastQueryMasterId != that.broadcastQueryMasterId) return false;
        if (broadcastResultInstanceId != that.broadcastResultInstanceId) return false;
        if (resultSetSize != that.resultSetSize) return false;
        if (timeElapsed != that.timeElapsed) return false;
        if (domainName != null ? !domainName.equals(that.domainName) : that.domainName != null) return false;
        if (resultStatus != null ? !resultStatus.equals(that.resultStatus) : that.resultStatus != null) return false;
        if (resultXml != null ? !resultXml.equals(that.resultXml) : that.resultXml != null) return false;
        if (spinQueryId != null ? !spinQueryId.equals(that.spinQueryId) : that.spinQueryId != null) return false;
        if (username != null ? !username.equals(that.username) : that.username != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = domainName != null ? domainName.hashCode() : 0;
        result = 31 * result + (username != null ? username.hashCode() : 0);
        result = 31 * result + (int) (broadcastQueryMasterId ^ (broadcastQueryMasterId >>> 32));
        result = 31 * result + (int) (broadcastQueryInstanceId ^ (broadcastQueryInstanceId >>> 32));
        result = 31 * result + (int) (broadcastResultInstanceId ^ (broadcastResultInstanceId >>> 32));
        result = 31 * result + (resultStatus != null ? resultStatus.hashCode() : 0);
        result = 31 * result + resultSetSize;
        result = 31 * result + timeElapsed;
        result = 31 * result + (spinQueryId != null ? spinQueryId.hashCode() : 0);
        result = 31 * result + (resultXml != null ? resultXml.hashCode() : 0);
        return result;
    }

    private Date queryDatetime;

    @Column(name = "QUERY_DATETIME")
    @Temporal(TemporalType.TIMESTAMP)
    public Date getQueryDatetime() {
        return queryDatetime;
    }

    public void setQueryDatetime(Date queryDatetime) {
        this.queryDatetime = queryDatetime;
    }
}
