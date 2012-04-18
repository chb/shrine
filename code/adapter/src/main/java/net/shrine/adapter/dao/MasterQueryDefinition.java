package net.shrine.adapter.dao;

import java.util.Date;

/**
 * @author Bill Simons
 * @date Sep 16, 2010
 * @link http://cbmi.med.harvard.edu
 * @link http://chip.org
 * <p/>
 * NOTICE: This software comes with NO guarantees whatsoever and is
 * licensed as Lgpl Open Source
 * @link http://www.gnu.org/licenses/lgpl.html
 */
public class MasterQueryDefinition
{

    protected String queryMasterId;
    protected String name;
    protected String userId;
    protected String groupId;
    protected Date createDate;
    protected String requestXml;

    public String getQueryMasterId()
    {
        return queryMasterId;
    }

    public void setQueryMasterId(String queryMasterId)
    {
        this.queryMasterId = queryMasterId;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getUserId()
    {
        return userId;
    }

    public void setUserId(String userId)
    {
        this.userId = userId;
    }

    public String getGroupId()
    {
        return groupId;
    }

    public void setGroupId(String groupId)
    {
        this.groupId = groupId;
    }

    public Date getCreateDate()
    {
        return createDate;
    }

    public void setCreateDate(Date createDate)
    {
        this.createDate = createDate;
    }

    public String getRequestXml()
    {
        return requestXml;
    }

    public void setRequestXml(String requestXml)
    {
        this.requestXml = requestXml;
    }
}
