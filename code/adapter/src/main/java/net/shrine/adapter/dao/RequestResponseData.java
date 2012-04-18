package net.shrine.adapter.dao;

/**
 * The java object representation of the data we wish to store about every
 * request that comes through the adapter. There are various consumers of this
 * gigantic data blob, and they should probably be separated out in the future.
 *
 * @author Justin Quan
 * @date: Apr 12, 2010
 * @link http://cbmi.med.harvard.edu
 * @link http://chip.org
 * <p/>
 * NOTICE: This software comes with NO guarantees whatsoever and is
 * licensed as Lgpl Open Source
 * @link http://www.gnu.org/licenses/lgpl.html
 * <p/>
 * TODO: It would be nice to make this immutable, but that will require
 * MyBatis 3.0
 */
public final class RequestResponseData
{
    private String domainName;
    private String username;

    private long broadcastQueryMasterId;
    private long broadcastQueryInstanceId;
    private long broadcastResultInstanceId;

    private String resultStatus;
    private String resultXml;
    private int resultSetSize;
    private long timeElapsedMillis;
    // TODO: fix type
    private String spinQueryId;

    /**
     * This default constructor is required by ibatis in order to serialize its
     * query results into this object
     */
    public RequestResponseData()
    {
    }

    public RequestResponseData(final String domainName, final String username, final long broadcastQueryMasterId, final long broadcastQueryInstanceId, final long broadcastResultInstanceId, final String resultStatus, final int resultSetSize, final long timeElapsedMillis, final String spinQueryId, String resultXml)
    {
        this.domainName = domainName;
        this.username = username;
        this.broadcastQueryMasterId = broadcastQueryMasterId;
        this.broadcastQueryInstanceId = broadcastQueryInstanceId;
        this.broadcastResultInstanceId = broadcastResultInstanceId;
        this.resultStatus = resultStatus;
        this.resultSetSize = resultSetSize;
        this.timeElapsedMillis = timeElapsedMillis;
        this.spinQueryId = spinQueryId;
        this.resultXml = resultXml;
    }

    public String getDomainName()
    {
        return domainName;
    }

    public String getUsername()
    {
        return username;
    }

    public long getBroadcastQueryMasterId()
    {
        return broadcastQueryMasterId;
    }

    public long getBroadcastQueryInstanceId()
    {
        return broadcastQueryInstanceId;
    }

    public long getBroadcastResultInstanceId()
    {
        return broadcastResultInstanceId;
    }

    public String getResultStatus()
    {
        return resultStatus;
    }

    public int getResultSetSize()
    {
        return resultSetSize;
    }

    public long getTimeElapsedMillis()
    {
        return timeElapsedMillis;
    }

    public String getSpinQueryId()
    {
        return spinQueryId;
    }

    public void setDomainName(final String domainName)
    {
        this.domainName = domainName;
    }

    public void setUsername(final String username)
    {
        this.username = username;
    }

    public void setBroadcastQueryMasterId(final long broadcastQueryMasterId)
    {
        this.broadcastQueryMasterId = broadcastQueryMasterId;
    }

    public void setBroadcastQueryInstanceId(final long broadcastQueryInstanceId)
    {
        this.broadcastQueryInstanceId = broadcastQueryInstanceId;
    }

    public void setBroadcastResultInstanceId(final long broadcastResultInstanceId)
    {
        this.broadcastResultInstanceId = broadcastResultInstanceId;
    }

    public void setResultStatus(final String resultStatus)
    {
        this.resultStatus = resultStatus;
    }

    public void setResultSetSize(final int resultSetSize)
    {
        this.resultSetSize = resultSetSize;
    }

    public void setTimeElapsedMillis(final long timeElapsedMillis)
    {
        this.timeElapsedMillis = timeElapsedMillis;
    }

    public void setSpinQueryId(final String spinQueryId)
    {
        this.spinQueryId = spinQueryId;
    }

    public String getResultXml()
    {
        return resultXml;
    }

    public void setResultXml(String resultXml)
    {
        this.resultXml = resultXml;
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

        RequestResponseData that = (RequestResponseData) o;

        if(broadcastQueryInstanceId != that.broadcastQueryInstanceId)
        {
            return false;
        }
        if(broadcastQueryMasterId != that.broadcastQueryMasterId)
        {
            return false;
        }
        if(broadcastResultInstanceId != that.broadcastResultInstanceId)
        {
            return false;
        }
        if(resultSetSize != that.resultSetSize)
        {
            return false;
        }
        if(timeElapsedMillis != that.timeElapsedMillis)
        {
            return false;
        }
        if(domainName != null ? !domainName.equals(that.domainName) : that.domainName != null)
        {
            return false;
        }
        if(resultStatus != null ? !resultStatus.equals(that.resultStatus) : that.resultStatus != null)
        {
            return false;
        }
        if(spinQueryId != null ? !spinQueryId.equals(that.spinQueryId) : that.spinQueryId != null)
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
        int result = domainName != null ? domainName.hashCode() : 0;
        result = 31 * result + (username != null ? username.hashCode() : 0);
        result = 31 * result + (int) (broadcastQueryMasterId ^ (broadcastQueryMasterId >>> 32));
        result = 31 * result + (int) (broadcastQueryInstanceId ^ (broadcastQueryInstanceId >>> 32));
        result = 31 * result + (int) (broadcastResultInstanceId ^ (broadcastResultInstanceId >>> 32));
        result = 31 * result + (resultStatus != null ? resultStatus.hashCode() : 0);
        result = 31 * result + resultSetSize;
        result = 31 * result + (int) (timeElapsedMillis ^ (timeElapsedMillis >>> 32));
        result = 31 * result + (spinQueryId != null ? spinQueryId.hashCode() : 0);
        return result;
    }
}
