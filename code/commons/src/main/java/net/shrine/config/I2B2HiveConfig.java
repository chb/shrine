package net.shrine.config;

import org.apache.log4j.Logger;

import java.util.HashMap;

/**
 * Memory resident cached copy of the PM response, this class is NOT intended to live on disk (XML).
 *
 * @author Andrew McMurry, MS
 * @date Jan 7, 2010 (REFACTORED 1.6.6)
 * @link http://cbmi.med.harvard.edu
 * @link http://chip.org
 * <p/>
 * NOTICE: This software comes with NO guarantees whatsoever and is licensed as Lgpl Open Source
 * @link http://www.gnu.org/licenses/lgpl.html
 */
public class I2B2HiveConfig
{
    public static final Logger log = Logger.getLogger(I2B2HiveConfig.class);

    /**
     * The HashMap is populated from reading the PM response.
     */
    protected HashMap<String, String> cells = new HashMap<String, String>();

    public void addCell(CellNames cellName, String cellUrl)
    {
        addCell(cellName.name(), cellUrl);
    }

    public void addCell(String cellName, String cellUrl)
    {
        cells.put(cellName, cellUrl);
    }

    public void removeCell(CellNames cellName, String cellUrl)
    {
        cells.remove(cellName.name());
    }

    public void removeCell(String cellName)
    {
        cells.remove(cellName);
    }

    public String getSheriffURL()
    {
        return getUrl(CellNames.SHERIFF);
    }

    public String getPMURL()
    {
        return getUrl(CellNames.PM);
    }

    public String getONTURL()
    {
        return getUrl(CellNames.ONT);
    }

    public String getCRCURL()
    {
        return getUrl(CellNames.CRC);
    }

    public String getAggregatorURL()
    {
        return getUrl(CellNames.AGGREGATOR);
    }

    public String getUrl(CellNames cellName)
    {
        return getUrl(cellName.name());
    }

    public String getUrl(String cellName)
    {
        if (!hasCell(cellName))
        {
            log.warn("Attempting to get URL for a cell that does not exist. Expected behavior? cell name = " + cellName);

            return null;
        }
        else
        {
            return cells.get(cellName);
        }
    }

    public boolean hasSheriff()
    {
        return hasCell(CellNames.SHERIFF);
    }

    public boolean hasONT()
    {
        return hasCell(CellNames.ONT);
    }

    public boolean hasCRC()
    {
        return hasCell(CellNames.CRC);
    }

    public boolean hasPM()
    {
        return hasCell(CellNames.PM);
    }

    public boolean hasAggregator()
    {
        return hasCell(CellNames.AGGREGATOR);
    }

    public boolean hasCell(CellNames cellName)
    {
        return hasCell(cellName.name());
    }

    public boolean hasCell(String cellName)
    {
        return cells.containsKey(cellName);
    }

    /**
     * Notice: Required for JAXB
     *
     * @return
     */
    public HashMap<String, String> getCells()
    {
        return cells;
    }

    /**
     * Notice: Required for JAXB
     *
     * @param cells
     */
    public void setCells(HashMap<String, String> cells)
    {
        this.cells = cells;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (o == null || getClass() != o.getClass())
        {
            return false;
        }

        I2B2HiveConfig other = (I2B2HiveConfig) o;

        if (this.cells.keySet().size() != other.cells.keySet().size())
        {
            return false;
        }

        for (String cellName : cells.keySet())
        {
            if (!other.cells.containsKey(cellName))
            {
                return false;
            }

            if (!this.cells.get(cellName).equals(other.cells.get(cellName)))
            {
                return false;
            }
        }

        return true;
    }

    @Override
    public String toString()
    {
        return "I2B2HiveConfig{" +
                "cells=" + cells +
                '}';
    }
}
