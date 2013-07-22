package net.shrine.serializers.pm;

import edu.harvard.i2b2.crc.datavo.i2b2message.ResponseMessageType;
import edu.harvard.i2b2.crc.datavo.pm.ConfigureType;
import edu.harvard.i2b2.crc.datavo.pm.PasswordType;
import edu.harvard.i2b2.crc.datavo.pm.UserType;
import net.shrine.config.I2B2HiveConfig;
import net.shrine.serializers.I2B2ExampleMessages;
import net.shrine.serializers.SerializerUnitTest;

/**
 * REFACTORED 1.6.6
 *
 * @author Andrew McMurry, MS
 * @date Jan 6, 2010 (REFACTORED)
 * @link http://cbmi.med.harvard.edu
 * @link http://chip.org
 * <p/>
 * NOTICE: This software comes with NO guarantees whatsoever and is licensed as Lgpl Open Source
 * @link http://www.gnu.org/licenses/lgpl.html
 */
public class PMSerializerTest extends SerializerUnitTest
{
    public void testRoundTripCannedMessages() throws Exception
    {
        roundTripRequest(I2B2ExampleMessages.PM_INVALID_LOGON_REQUEST);
        roundTripRequest(I2B2ExampleMessages.PM_VALID_LOGON_REQUEST);

        roundTripResponse(I2B2ExampleMessages.PM_INVALID_LOGON_RESPONSE);
        roundTripResponse(I2B2ExampleMessages.PM_VALID_LOGON_RESPONSE);
    }

    public void testGetHiveConfig() throws Exception
    {
        ResponseMessageType response = I2B2ExampleMessages.PM_VALID_LOGON_RESPONSE.getResponse();
        I2B2HiveConfig hiveConfig = PMSerializer.getHiveConfig(response);

        assertTrue(hiveConfig.hasCRC());
        assertTrue(hiveConfig.hasONT());

        assertEquals(hiveConfig.getCRCURL(), "http://cbmi-lab.med.harvard.edu:8443/shrine-cell/QueryToolService/");
        assertEquals(hiveConfig.getONTURL(), "http://cbmi-i2b2-dev:9090/i2b2/rest/OntologyService/");

        try
        {
            I2B2HiveConfig shouldBeNull = PMSerializer.getHiveConfig(I2B2ExampleMessages.PM_INVALID_LOGON_RESPONSE.getResponse());
            fail("Expected PMInvalidLogonException getting I2B2HiveConfig from PM_INVALID_LOGON_RESPONSE, instead got " + String.valueOf(shouldBeNull));
        }
        catch(PMInvalidLogonException pmile)
        {
            // expected
        }

    }

    public void testGetUserType() throws Exception
    {
        UserType userType = PMSerializer.getUserType(I2B2ExampleMessages.PM_VALID_LOGON_RESPONSE.getResponse());

        assertNotNull("UserType should not be null!", userType);
        assertEquals(userType.getFullName(), "Demo User");
        assertEquals(userType.getUserName(), "demo");
        PasswordType pass = new PasswordType();
        pass.setValue("demouser");
        assertEquals(userType.getPassword(), pass);
        assertEquals(userType.getDomain(), "demo");
        assertEquals(userType.getProject().size(), 2);

        try
        {
            UserType shouldBeNull = PMSerializer.getUserType(I2B2ExampleMessages.PM_INVALID_LOGON_RESPONSE.getResponse());
            fail("Expected PMInvalidLogonException getting usertype from PM_INVALID_LOGON_RESPONSE, instead got " + String.valueOf(shouldBeNull));
        }
        catch(PMInvalidLogonException pmile)
        {
            // expected
        }
    }

    public void testGetBodyType() throws Exception
    {
        ConfigureType configureType = PMSerializer.getBodyType(I2B2ExampleMessages.PM_VALID_LOGON_RESPONSE.getResponse());
        assertEquals("DEVELOPMENT", configureType.getEnvironment());
        assertEquals("http://www.i2b2.org", configureType.getHelpURL());
        assertEquals(6, configureType.getCellDatas().getCellData().size());

        try
        {
            ConfigureType shouldBeNull = PMSerializer.getBodyType(I2B2ExampleMessages.PM_INVALID_LOGON_RESPONSE.getResponse());
            fail("Expected PMInvalidLogonException getting configureType from PM_INVALID_LOGON_RESPONSE, instead got " + String.valueOf(shouldBeNull));
        }
        catch(PMInvalidLogonException pmile)
        {
            // expected
        }
    }
}