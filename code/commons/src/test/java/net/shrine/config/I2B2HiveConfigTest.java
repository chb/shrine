package net.shrine.config;

import org.spin.tools.SPINUnitTest;
import org.spin.tools.config.ConfigTool;

/**
 * @author Andrew McMurry, MS
 * @date Jan 6, 2010 (REFACTORED)
 * @link http://cbmi.med.harvard.edu
 * @link http://chip.org
 *       <p/>
 *       NOTICE: This software comes with NO guarantees whatsoever and is
 *       licensed as Lgpl Open Source
 * @link http://www.gnu.org/licenses/lgpl.html
 */
public final class I2B2HiveConfigTest extends SPINUnitTest
{
    public void test()
    {
        final I2B2HiveConfig hive = getExampleI2B2HiveConfig();

        log.info("Testing that the hive configuraiton can be parsed correctly.");

        assertTrue(hive.hasCell(CellNames.PM));
        assertTrue(hive.hasCell(CellNames.ONT));
        assertTrue(hive.hasCell(CellNames.CRC));
        assertTrue(hive.hasCell(CellNames.AGGREGATOR));
        assertTrue(hive.hasCell(CellNames.SHERIFF));

        // TODO: What other tests should be performed here?
    }

    public static I2B2HiveConfig getExampleI2B2HiveConfig()
    {
        final I2B2HiveConfig hive = new I2B2HiveConfig();

        // i2b2 REST (xml over http) services

        // http://webservices.i2b2.org/i2b2/rest/QueryToolService/request
        hive.addCell(CellNames.CRC, "http://webservices.i2b2.org/i2b2/rest/QueryToolService/");

        // http://services.i2b2.org/i2b2/rest/OntologyService/{getSchemes,getCategories,getFoldersByUserId,...}
        hive.addCell(CellNames.ONT, "http://services.i2b2.org/i2b2/rest/OntologyService/");

        // http://services.i2b2.org/PM/rest/PMService/getServices
        hive.addCell(CellNames.PM, "http://services.i2b2.org/PM/rest/PMService/");
        //

        // SHRINE jaxws / WSDL provided services

        hive.addCell(CellNames.AGGREGATOR, "http://" + ConfigTool.getHostName() + "/shrine/soap");

        // http://localhost/sheriff/{webservice,logon}
        hive.addCell(CellNames.SHERIFF, "http://" + ConfigTool.getHostName() + "/sheriff/");

        return hive;
    }
}
