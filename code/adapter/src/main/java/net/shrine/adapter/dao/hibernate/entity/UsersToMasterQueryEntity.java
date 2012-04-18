package net.shrine.adapter.dao.hibernate.entity;

import net.shrine.adapter.dao.MasterTuple;

import javax.persistence.*;
import java.sql.Timestamp;

/**
 * Created by IntelliJ IDEA.
 * User: davidortiz
 * Date: 12/13/10
 * Time: 9:19 AM
 * To change this template use File | Settings | File Templates.
 */
@IdClass(UsersToMasterQueryEntityPK.class)
@Table(name = "USERS_TO_MASTER_QUERY")
@Entity
public class UsersToMasterQueryEntity {
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

    private String masterName;

    @Column(name = "MASTER_NAME")
    @Basic
    public String getMasterName() {
        return masterName;
    }

    public void setMasterName(String masterName) {
        this.masterName = masterName;
    }

    private Timestamp masterCreateDate;

    @Column(name = "MASTER_CREATE_DATE")
    @Basic
    public Timestamp getMasterCreateDate() {
        return masterCreateDate;
    }

    public void setMasterCreateDate(Timestamp masterCreateDate) {
        this.masterCreateDate = masterCreateDate;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        UsersToMasterQueryEntity that = (UsersToMasterQueryEntity) o;

        if (broadcastQueryMasterId != that.broadcastQueryMasterId) return false;
        if (domainName != null ? !domainName.equals(that.domainName) : that.domainName != null) return false;
        if (masterCreateDate != null ? !masterCreateDate.equals(that.masterCreateDate) : that.masterCreateDate != null)
            return false;
        if (masterName != null ? !masterName.equals(that.masterName) : that.masterName != null) return false;
        if (username != null ? !username.equals(that.username) : that.username != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = domainName != null ? domainName.hashCode() : 0;
        result = 31 * result + (username != null ? username.hashCode() : 0);
        result = 31 * result + (int) (broadcastQueryMasterId ^ (broadcastQueryMasterId >>> 32));
        result = 31 * result + (masterName != null ? masterName.hashCode() : 0);
        result = 31 * result + (masterCreateDate != null ? masterCreateDate.hashCode() : 0);
        return result;
    }
}
