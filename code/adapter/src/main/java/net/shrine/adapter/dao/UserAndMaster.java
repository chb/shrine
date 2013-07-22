package net.shrine.adapter.dao;

import java.util.Date;

import org.spin.tools.NetworkTime;

import edu.harvard.i2b2.crc.datavo.setfinder.query.QueryMasterType;

/**
 * 
 * @author clint
 * 
 * Sep 8, 2010
 *
 * Center for Biomedical Informatics (CBMI)
 * @link https://cbmi.med.harvard.edu/
 *
 */
public final class UserAndMaster
{
    private String domainName;
    
    private String userName;
    
    private Long networkMasterID;
    
    private String masterName;
    
    private Date masterCreateDate;

    //NB: For Ibatis
    private UserAndMaster()
    {
        this(null, null);
    }
    
    public UserAndMaster(final String domainName, final String userName)
    {
        this(domainName, userName, null, null, null);
    }
    
    public UserAndMaster(final String domainName, final String userName, final Long networkMasterID, final String masterName, final Date masterCreateDate)
    {
        super();
        
        //NB: Ibatis make empty beans and then sets their fields, so these must
        //all be nullable. :(
        
        this.domainName = domainName;
        this.userName = userName;
        this.networkMasterID = networkMasterID;
        this.masterName = masterName;
        this.masterCreateDate = masterCreateDate;
    }

    public QueryMasterType toQueryMasterType()
    {
        final QueryMasterType result = new QueryMasterType();
        
        result.setGroupId(domainName);
        result.setUserId(userName);
        result.setQueryMasterId(String.valueOf(networkMasterID));
        result.setName(masterName);
        result.setCreateDate(NetworkTime.makeXMLGregorianCalendar(masterCreateDate));
        
        return result;
    }
    
    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((domainName == null) ? 0 : domainName.hashCode());
        result = prime * result + ((masterCreateDate == null) ? 0 : masterCreateDate.hashCode());
        result = prime * result + ((masterName == null) ? 0 : masterName.hashCode());
        result = prime * result + ((networkMasterID == null) ? 0 : networkMasterID.hashCode());
        result = prime * result + ((userName == null) ? 0 : userName.hashCode());
        return result;
    }

    @Override
    public boolean equals(final Object obj)
    {
        if(this == obj)
        {
            return true;
        }
        if(obj == null)
        {
            return false;
        }
        if(getClass() != obj.getClass())
        {
            return false;
        }
        final UserAndMaster other = (UserAndMaster) obj;
        if(domainName == null)
        {
            if(other.domainName != null)
            {
                return false;
            }
        }
        else if(!domainName.equals(other.domainName))
        {
            return false;
        }
        if(masterCreateDate == null)
        {
            if(other.masterCreateDate != null)
            {
                return false;
            }
        }
        else if(!masterCreateDate.equals(other.masterCreateDate))
        {
            return false;
        }
        if(masterName == null)
        {
            if(other.masterName != null)
            {
                return false;
            }
        }
        else if(!masterName.equals(other.masterName))
        {
            return false;
        }
        if(networkMasterID == null)
        {
            if(other.networkMasterID != null)
            {
                return false;
            }
        }
        else if(!networkMasterID.equals(other.networkMasterID))
        {
            return false;
        }
        if(userName == null)
        {
            if(other.userName != null)
            {
                return false;
            }
        }
        else if(!userName.equals(other.userName))
        {
            return false;
        }
        return true;
    }

    @Override
    public String toString()
    {
        return "UserAndMaster [domainName=" + domainName + ", masterCreateDate=" + masterCreateDate + ", masterName=" + masterName + ", networkMasterID=" + networkMasterID + ", userName=" + userName + "]";
    }

    public String getDomainName()
    {
        return domainName;
    }

    public void setDomainName(final String domainName)
    {
        this.domainName = domainName;
    }

    public String getUserName()
    {
        return userName;
    }

    public void setUserName(final String userName)
    {
        this.userName = userName;
    }

    public Long getNetworkMasterID()
    {
        return networkMasterID;
    }

    public void setNetworkMasterID(final Long networkMasterID)
    {
        this.networkMasterID = networkMasterID;
    }

    public String getMasterName()
    {
        return masterName;
    }

    public Date getMasterCreateDate()
    {
        return masterCreateDate;
    }

    public void setMasterName(final String masterName)
    {
        this.masterName = masterName;
    }

    public void setMasterCreateDate(final Date masterCreateDate)
    {
        this.masterCreateDate = masterCreateDate;
    }
}
