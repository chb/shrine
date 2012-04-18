package net.shrine.adapter.dao;

/**
 * @author Bill Simons
 * @date Sep 23, 2010
 * @link http://cbmi.med.harvard.edu
 * @link http://chip.org
 * <p/>
 * NOTICE: This software comes with NO guarantees whatsoever and is
 * licensed as Lgpl Open Source
 * @link http://www.gnu.org/licenses/lgpl.html
 */
public class ResultTuple
{
    private IDPair idPair;
    private Integer obfuscationAmount;

    public ResultTuple(IDPair idPair)
    {
        this.idPair = idPair;
    }

    public ResultTuple(IDPair idPair, Integer obfuscationAmount)
    {
        this.idPair = idPair;
        this.obfuscationAmount = obfuscationAmount;
    }

    public ResultTuple()
    {
    }

    public IDPair getIdPair()
    {
        return idPair;
    }

    public void setIdPair(IDPair idPair)
    {
        this.idPair = idPair;
    }

    public Integer getObfuscationAmount()
    {
        return obfuscationAmount;
    }

    public void setObfuscationAmount(Integer obfuscationAmount)
    {
        this.obfuscationAmount = obfuscationAmount;
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

        ResultTuple that = (ResultTuple) o;

        if(idPair != null ? !idPair.equals(that.idPair) : that.idPair != null)
        {
            return false;
        }
        if(obfuscationAmount != null ? !obfuscationAmount.equals(that.obfuscationAmount) : that.obfuscationAmount != null)
        {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode()
    {
        int result = idPair != null ? idPair.hashCode() : 0;
        result = 31 * result + (obfuscationAmount != null ? obfuscationAmount.hashCode() : 0);
        return result;
    }
}
