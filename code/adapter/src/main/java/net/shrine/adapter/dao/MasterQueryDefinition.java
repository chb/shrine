package net.shrine.adapter.dao;

import edu.harvard.i2b2.crc.datavo.setfinder.query.QueryDefinitionType;
import edu.harvard.i2b2.crc.datavo.setfinder.query.QueryMasterType;
import edu.harvard.i2b2.crc.datavo.setfinder.query.RequestXmlType;
import org.spin.tools.JAXBUtils;

import javax.xml.bind.JAXBException;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.util.Date;
import java.util.GregorianCalendar;

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

    public QueryMasterType toQueryMasterType()
    {
        QueryMasterType queryMaster = new QueryMasterType();
        queryMaster.setName(this.name);
        queryMaster.setCreateDate(dateToXmlGregorianCalendar());
        queryMaster.setQueryMasterId(this.queryMasterId);
        queryMaster.setUserId(this.userId);
        RequestXmlType requestXmlType = new RequestXmlType();
        requestXmlType.getContent().add(convertToQueryDefinitionType());
        queryMaster.setRequestXml(requestXmlType);
        return queryMaster;
    }

    private QueryDefinitionType convertToQueryDefinitionType()
    {
        QueryDefinitionType type = null;
        try
        {
            type = JAXBUtils.unmarshal(this.requestXml, QueryDefinitionType.class);
        }
        catch(JAXBException e)
        {
            return null;
        }
        return type;
    }

    private XMLGregorianCalendar dateToXmlGregorianCalendar()
    {
        GregorianCalendar c = new GregorianCalendar();
        c.setTime(this.createDate);
        XMLGregorianCalendar date = null;
        try
        {
            date = DatatypeFactory.newInstance().newXMLGregorianCalendar(c);
        }
        catch(DatatypeConfigurationException e)
        {
            return null;
        }
        return date;
    }
}
