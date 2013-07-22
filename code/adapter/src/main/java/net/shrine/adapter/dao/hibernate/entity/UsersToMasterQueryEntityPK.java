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
public class UsersToMasterQueryEntityPK implements Serializable {
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        UsersToMasterQueryEntityPK that = (UsersToMasterQueryEntityPK) o;

        if (broadcastQueryMasterId != that.broadcastQueryMasterId) return false;
        if (domainName != null ? !domainName.equals(that.domainName) : that.domainName != null) return false;
        if (username != null ? !username.equals(that.username) : that.username != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = domainName != null ? domainName.hashCode() : 0;
        result = 31 * result + (username != null ? username.hashCode() : 0);
        result = 31 * result + (int) (broadcastQueryMasterId ^ (broadcastQueryMasterId >>> 32));
        return result;
    }
}
