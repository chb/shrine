package net.shrine.adapter.dao.hibernate.entity;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import java.util.Date;

/**
 * Created by IntelliJ IDEA.
 * User: davidortiz
 * Date: 12/13/10
 * Time: 9:19 AM
 * To change this template use File | Settings | File Templates.
 */
@Table(name = "PRIVILEGED_USER")
@Cache(usage= CacheConcurrencyStrategy.NONE)
@Entity
public class PrivilegedUserEntity {
    private int id;

    private String username;

    private String domain;

    private int threshold;

    private Date overrideDate;

    @Column(name = "ID")
    @Id
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @Column(name = "USERNAME")
    @Basic
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    @Column(name = "DOMAIN")
    @Basic
    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    @Column(name = "THRESHOLD")
    @Basic
    public int getThreshold() {
        return threshold;
    }

    public void setThreshold(int threshold) {
        this.threshold = threshold;
    }

    @Column(name = "OVERRIDE_DATE")
    @Temporal(TemporalType.TIMESTAMP)
    public Date getOverrideDate() {
        return overrideDate;
    }

    public void setOverrideDate(Date overrideDate) {
        this.overrideDate = overrideDate;
    }


    @Override
    public boolean equals(Object o) {
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;

        PrivilegedUserEntity that = (PrivilegedUserEntity) o;

        if(id != that.id) return false;
        if(threshold != that.threshold) return false;
        if(!domain.equals(that.domain)) return false;
        if(overrideDate != null ? !overrideDate.equals(that.overrideDate) : that.overrideDate != null) return false;
        if(!username.equals(that.username)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = id;
        result = 31 * result + username.hashCode();
        result = 31 * result + domain.hashCode();
        result = 31 * result + threshold;
        result = 31 * result + (overrideDate != null ? overrideDate.hashCode() : 0);
        return result;
    }
}
