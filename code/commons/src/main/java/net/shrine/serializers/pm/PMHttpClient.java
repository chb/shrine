package net.shrine.serializers.pm;

import edu.harvard.i2b2.crc.datavo.i2b2message.PasswordType;
import edu.harvard.i2b2.crc.datavo.i2b2message.SecurityType;
import edu.harvard.i2b2.crc.datavo.pm.UserType;
import edu.harvard.i2b2.crc.datavo.pm.UsersType;
import net.shrine.config.I2B2HiveConfig;
import net.shrine.serializers.HTTPClient;
import org.apache.log4j.Logger;
import org.spin.query.message.serializer.SerializationException;
import org.spin.tools.config.ConfigException;

import javax.xml.bind.JAXBException;
import java.io.IOException;

/**
 * REFACTORED
 *
 * @author Bill Simons, MS; Andy McMurry, MS
 * @date Aug 19, 2010
 * @link http://cbmi.med.harvard.edu
 * @link http://chip.org
 * <p/>
 * NOTICE: This software comes with NO guarantees whatsoever and is licensed as Lgpl Open Source
 * @link http://www.gnu.org/licenses/lgpl.html
 */

public class PMHttpClient
{
    private static final Logger log = Logger.getLogger(PMHttpClient.class);
    private static final boolean DEBUG = log.isDebugEnabled();
    private static final boolean INFO = log.isInfoEnabled();

    private String pmURL;

    public PMHttpClient(String url)
    {
        this.pmURL = url;
    }


    public I2B2HiveConfig getServices(SecurityType credentials) throws ConfigException, JAXBException, IOException, PMInvalidLogonException, SerializationException
    {
        return getServices(PMSerializer.getPMUserAuthRequestString(credentials));
    }


    public I2B2HiveConfig getServices(String i2b2XML) throws ConfigException, IOException, JAXBException, PMInvalidLogonException, SerializationException
    {
        if(INFO)
        {
            log.info("PM.getServices from " + pmURL);
        }

        String responseXML = HTTPClient.post(i2b2XML, pmURL);

        if(DEBUG)
        {
            log.debug("Reading PM response" + responseXML);
        }

        return PMSerializer.getHiveConfig(responseXML);
    }

    public UserType getUserConfiguration(String domain, String username, String password) throws ConfigException, JAXBException, IOException, PMInvalidLogonException, SerializationException
    {
        PasswordType passwordType = new PasswordType();
        passwordType.setIsToken(false);
        passwordType.setValue(password);
        return getUserConfiguration(new SecurityType(domain, username, passwordType));
    }

    public UserType getUserConfiguration(SecurityType credentials) throws ConfigException, IOException, JAXBException, PMInvalidLogonException, SerializationException
    {
        return getUserConfiguration(PMSerializer.getPMUserAuthRequestString(credentials));
    }


    public UserType getUserConfiguration(String i2b2XML) throws ConfigException, IOException, JAXBException, PMInvalidLogonException, SerializationException
    {
        if(DEBUG)
        {
            log.debug("PM.getUserParams from " + pmURL);
        }

        String responseXML = HTTPClient.post(i2b2XML, pmURL);

        if(DEBUG)
        {
            log.debug("Reading PM response" + responseXML);
        }

        return PMSerializer.getUserType(responseXML);
    }

    public UsersType getAllUserParams(SecurityType securityType) throws JAXBException, IOException, SerializationException
    {
        String request = PMSerializer.getPMGetAllUserParamsRequestString(securityType);
        String response = HTTPClient.post(request, pmURL);
        return PMSerializer.getUsersType(response);
    }
}
