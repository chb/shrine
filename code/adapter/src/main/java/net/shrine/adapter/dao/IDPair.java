package net.shrine.adapter.dao;

/**
 * @author clint
 *         <p/>
 *         Sep 1, 2010
 *         <p/>
 *         Center for Biomedical Informatics (CBMI)
 * @link https://cbmi.med.harvard.edu/
 * <p/>
 * NB: Needs to be a mutable bean due to Ibatis. :(
 */
public final class IDPair
{
    private Long networkID;

    private String localID;

    private IDPair()
    {
        super();
    }

    private IDPair(final Long networkID, final String localID)
    {
        super();
        this.networkID = networkID;
        this.localID = localID;
    }

    public static IDPair of(final Long networkID, final String localID)
    {
        return new IDPair(networkID, localID);
    }

    public static IDPair networkOnly(final Long networkID)
    {
        return new IDPair(networkID, null);
    }

    public static IDPair localOnly(final String localID)
    {
        return new IDPair(null, localID);
    }

    public Long getNetworkID()
    {
        return networkID;
    }

    public void setNetworkID(final Long networkID)
    {
        this.networkID = networkID;
    }

    public String getLocalID()
    {
        return localID;
    }

    public void setLocalID(final String localID)
    {
        this.localID = localID;
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((localID == null) ? 0 : localID.hashCode());
        result = prime * result + ((networkID == null) ? 0 : networkID.hashCode());
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
        final IDPair other = (IDPair) obj;
        if(localID == null)
        {
            if(other.localID != null)
            {
                return false;
            }
        }
        else if(!localID.equals(other.localID))
        {
            return false;
        }
        if(networkID == null)
        {
            if(other.networkID != null)
            {
                return false;
            }
        }
        else if(!networkID.equals(other.networkID))
        {
            return false;
        }
        return true;
    }

    @Override
    public String toString()
    {
        return "IDPair [networkID=" + networkID + ", localID=" + localID + "]";
    }

    public static IDPair empty()
    {
        return new IDPair();
    }
}
