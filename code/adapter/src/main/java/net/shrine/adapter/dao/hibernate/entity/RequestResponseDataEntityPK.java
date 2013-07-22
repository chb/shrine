package net.shrine.adapter.dao.hibernate.entity;

import javax.persistence.Column;
import javax.persistence.Id;
import java.io.Serializable;

/**
 * Created by IntelliJ IDEA.
 * User: davidortiz
 * Date: 12/13/10
 * Time: 9:19 AM
 * To change this template use File | Settings | File Templates.
 */
public class RequestResponseDataEntityPK implements Serializable {
    private String domainName;

    @Id
    @Column(name = "DOMAIN_NAME")
    public String getDomainName() {
        return domainName;
    }

    public void setDomainName(String domainName) {
        this.domainName = domainName;
    }

    private String username;

    @Id
    @Column(name = "USERNAME")
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    private long broadcastQueryMasterId;

    @Id
    @Column(name = "BROADCAST_QUERY_MASTER_ID")
    public long getBroadcastQueryMasterId() {
        return broadcastQueryMasterId;
    }

    public void setBroadcastQueryMasterId(long broadcastQueryMasterId) {
        this.broadcastQueryMasterId = broadcastQueryMasterId;
    }

    private long broadcastQueryInstanceId;

    @Id
    @Column(name = "BROADCAST_QUERY_INSTANCE_ID")
    public long getBroadcastQueryInstanceId() {
        return broadcastQueryInstanceId;
    }

    public void setBroadcastQueryInstanceId(long broadcastQueryInstanceId) {
        this.broadcastQueryInstanceId = broadcastQueryInstanceId;
    }

    private long broadcastResultInstanceId;

    @Id
    @Column(name = "BROADCAST_RESULT_INSTANCE_ID")
    public long getBroadcastResultInstanceId() {
        return broadcastResultInstanceId;
    }

    public void setBroadcastResultInstanceId(long broadcastResultInstanceId) {
        this.broadcastResultInstanceId = broadcastResultInstanceId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        RequestResponseDataEntityPK that = (RequestResponseDataEntityPK) o;

        if (broadcastQueryInstanceId != that.broadcastQueryInstanceId) return false;
        if (broadcastQueryMasterId != that.broadcastQueryMasterId) return false;
        if (broadcastResultInstanceId != that.broadcastResultInstanceId) return false;
        if (domainName != null ? !domainName.equals(that.domainName) : that.domainName != null) return false;
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
        return result;
    }
}
