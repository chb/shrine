package net.shrine.adapter.dao;

/**
 * @author Bill Simons
 * @date Sep 14, 2010
 * @link http://cbmi.med.harvard.edu
 * @link http://chip.org
 * <p/>
 * NOTICE: This software comes with NO guarantees whatsoever and is
 * licensed as Lgpl Open Source
 * @link http://www.gnu.org/licenses/lgpl.html
 */
public class MasterTuple
{
    private IDPair idPair;
    private String queryDefinition;

    public MasterTuple()
    {
        idPair = IDPair.empty();
    }

    public MasterTuple(IDPair idPair, String queryDefinition)
    {
        this.idPair = idPair;
        this.queryDefinition = queryDefinition;
    }

    public IDPair getIdPair()
    {
        return idPair;
    }

    public void setIdPair(IDPair idPair)
    {
        this.idPair = idPair;
    }

    public String getQueryDefinition()
    {
        return queryDefinition;
    }

    public void setQueryDefinition(String queryDefinition)
    {
        this.queryDefinition = queryDefinition;
    }

    public String getLocalID()
    {
        return idPair.getLocalID();
    }

    public void setLocalID(String id)
    {
        idPair.setLocalID(id);
    }

    public Long getNetworkID()
    {
        return idPair.getNetworkID();
    }

    public void setNetworkID(Long id)
    {
        idPair.setNetworkID(id);
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

        MasterTuple that = (MasterTuple) o;

        if(idPair != null ? !idPair.equals(that.idPair) : that.idPair != null)
        {
            return false;
        }
        if(queryDefinition != null ? !queryDefinition.equals(that.queryDefinition) : that.queryDefinition != null)
        {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode()
    {
        int result = idPair != null ? idPair.hashCode() : 0;
        result = 31 * result + (queryDefinition != null ? queryDefinition.hashCode() : 0);
        return result;
    }
}
