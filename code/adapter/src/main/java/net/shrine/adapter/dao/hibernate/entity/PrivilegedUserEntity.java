package net.shrine.adapter.dao.hibernate.entity;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * Created by IntelliJ IDEA.
 * User: davidortiz
 * Date: 12/13/10
 * Time: 9:19 AM
 * To change this template use File | Settings | File Templates.
 */
@Table(name = "PRIVILEGED_USER")
@Entity
public class PrivilegedUserEntity
{
    private int id;

    private String username;
    
    private String domain;

    private int threshold;
    
    @Column(name = "ID")
    @Id
    public int getId()
    {
        return id;
    }

    public void setId(int id)
    {
        this.id = id;
    }
    
    @Column(name = "USERNAME")
    @Basic
    public String getUsername()
    {
        return username;
    }

    public void setUsername(String username)
    {
        this.username = username;
    }

    @Column(name = "DOMAIN")
    @Basic
    public String getDomain()
    {
        return domain;
    }

    public void setDomain(String domain)
    {
        this.domain = domain;
    }
    
    @Column(name = "THRESHOLD")
    @Basic
    public int getThreshold()
    {
        return threshold;
    }

    public void setThreshold(int threshold)
    {
        this.threshold = threshold;
    }


    @Override
    public boolean equals(Object o)
    {
        if(this == o)
        {
            return true;
        }
        if(o == null || getClass() != o.getClass())
        {
            return false;
        }

        PrivilegedUserEntity that = (PrivilegedUserEntity) o;

        if(id != that.id)
        {
            return false;
        }
        if(threshold != that.threshold)
        {
            return false;
        }
        if(username != null ? !username.equals(that.username) : that.username != null)
        {
            return false;
        }
        if(domain != null ? !domain.equals(that.domain) : that.domain != null)
        {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode()
    {
        int result = id;
        result = 31 * result + (username != null ? username.hashCode() : 0);
        result = 31 * result + (domain != null ? domain.hashCode() : 0);
        result = 31 * result + threshold;
        return result;
    }
}
