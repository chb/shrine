package net.shrine.serializers.pm;

import edu.harvard.i2b2.crc.datavo.i2b2message.ResponseHeaderType;
import edu.harvard.i2b2.crc.datavo.i2b2message.ResponseMessageType;
import edu.harvard.i2b2.crc.datavo.pm.CellDataType;
import edu.harvard.i2b2.crc.datavo.pm.ConfigureType;
import edu.harvard.i2b2.crc.datavo.pm.ParamType;
import edu.harvard.i2b2.crc.datavo.pm.ProjectType;
import edu.harvard.i2b2.crc.datavo.pm.UserType;
import edu.harvard.i2b2.crc.datavo.pm.UsersType;
import net.shrine.config.CellNames;
import net.shrine.config.I2B2HiveConfig;
import net.shrine.serializers.hive.HiveCommonSerializer;
import org.apache.log4j.Logger;
import org.spin.query.message.serializer.SerializationException;

import javax.xml.bind.JAXBException;

/**
 * REFACTORED
 *
 * @author Andrew McMurry, MS
 * @date Jan 6, 2010 (REFACTORED)
 * @link http://cbmi.med.harvard.edu
 * @link http://chip.org
 * <p/>
 * NOTICE: This software comes with NO guarantees whatsoever and is licensed as Lgpl Open Source
 * @link http://www.gnu.org/licenses/lgpl.html
 */
public class PMSerializer extends HiveCommonSerializer
{
    public static final Logger log = Logger.getLogger(PMSerializer.class);

    public final boolean DEBUG = log.isDebugEnabled();
    public final boolean INFO = log.isInfoEnabled();

    //--- REQUEST MESSAGES

    //@SEE HiveCommonSerializer


    //--- RESPONSE MESSAGES

    public static ConfigureType getBodyType(ResponseMessageType response) throws SerializationException, PMInvalidLogonException
    {
        assertValidLogin(response);

        return getBodyNode(response, 0, ConfigureType.class);
    }

    public static I2B2HiveConfig getHiveConfig(String responseXML) throws JAXBException, PMInvalidLogonException, SerializationException
    {
        return getHiveConfig(getResponse(responseXML));
    }

    public static I2B2HiveConfig getHiveConfig(ResponseMessageType response) throws SerializationException, PMInvalidLogonException
    {
        assertValidLogin(response);

        ConfigureType config = getBodyType(response);

        I2B2HiveConfig hive = new I2B2HiveConfig();

        for(CellDataType cellData : config.getCellDatas().getCellData())
        {
            hive.addCell(cellData.getId(), cellData.getUrl());
        }

        if(hive.hasCRC())
        {
            // trim off the resource
            String aggregatorURL = hive.getCRCURL() + "aggregate";

            hive.addCell(CellNames.AGGREGATOR, aggregatorURL);
        }
        else
        {
            log.warn("No CRC found, invalid hive?");
        }

        return hive;
    }

    public static UserType getUserType(String responseXML) throws JAXBException, PMInvalidLogonException, SerializationException
    {
        return getUserType(getResponse(responseXML));
    }

    public static UserType getUserType(ResponseMessageType response) throws JAXBException, PMInvalidLogonException, SerializationException
    {
        assertValidLogin(response);

        //TODO: http://jira.open.med.harvard.edu/browse/SHRINE-447
        UserType userType = getBodyType(response).getUser();
        return userType;
    }

    private static void assertValidLogin(ResponseMessageType response) throws PMInvalidLogonException
    {
        ResponseHeaderType header = response.getResponseHeader();
        if(header.getResultStatus().getStatus().getType().equalsIgnoreCase(ERROR))
        {
            throw new PMInvalidLogonException(header.getResultStatus().getStatus().getValue());
        }
    }

    public static UsersType getUsersType(String responseXml) throws JAXBException, SerializationException
    {
        ResponseMessageType response = getResponse(responseXml);
        return getBodyNode(response, 0, UsersType.class);
    }

    public static String extractEcommonsUsername(UserType user)
    {
        for(ParamType param : user.getParam())
        {
            if(param.getName().equals("ecommons_username"))
            {
                return param.getValue();
            }
        }

        //TODO - Remove me once we drop i2b2 1.3 support
        for(ProjectType project : user.getProject())
        {
            for(ParamType param : project.getParam())
            {
                if(param.getName().equals("ecommons_username"))
                {
                    return param.getValue();
                }
            }
        }

        return null;
    }
}