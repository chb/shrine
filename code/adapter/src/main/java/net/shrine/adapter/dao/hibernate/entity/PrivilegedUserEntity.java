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

    private String username;

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

    private int threshold;

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

        return true;
    }

    @Override
    public int hashCode()
    {
        int result = id;
        result = 31 * result + (username != null ? username.hashCode() : 0);
        result = 31 * result + threshold;
        return result;
    }
}
