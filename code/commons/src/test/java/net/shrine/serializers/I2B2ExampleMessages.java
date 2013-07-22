package net.shrine.serializers;

import edu.harvard.i2b2.crc.datavo.i2b2message.RequestMessageType;
import edu.harvard.i2b2.crc.datavo.i2b2message.ResponseMessageType;
import net.shrine.serializers.hive.HiveJaxbContext;
import org.apache.log4j.Logger;
import org.spin.tools.FileUtils;
import org.spin.tools.JAXBUtils;
import org.spin.tools.config.ConfigException;
import org.spin.tools.config.ConfigTool;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;

/**
 * REFACTORED
 *
 * @author Andrew McMurry, MS
 * @date Feb 22, 2010 (REFACTORED)
 * @link http://cbmi.med.harvard.edu
 * @link http://chip.org
 * <p/>
 * NOTICE: This software comes with NO guarantees whatsoever and is licensed as Lgpl Open Source
 * @link http://www.gnu.org/licenses/lgpl.html
 */
//TODO: http://jira.open.med.harvard.edu/browse/SHRINE-406
public enum I2B2ExampleMessages
{
    CRC_GET_QUERY_RESULT_INSTANCE_LIST_FROM_QUERY_INSTANCE_REQUEST,
    CRC_QRY_GET_REQUEST_XML_FROM_QUERY_MASTER_ID,
    CRC_GET_QUERY_RESULT_INSTANCE_LIST_FROM_QUERY_INSTANCE_RESPONSE,
    CRC_GET_QUEST_INSTANCE_LIST_FROM_QUERY_MASTER_REQUEST,
    CRC_GET_QUEST_INSTANCE_LIST_FROM_QUERY_MASTER_RESPONSE,
    CRC_PREVIOUS_QUERY_RESPONSE,
    CRC_QUERY_FROM_DEFINITION_DEMOGRAPHICS_REQUEST,
    CRC_QUERY_FROM_DEFINITION_DEMOGRAPHICS_RESPONSE,
    CRC_QUERY_FROM_DEFINITION_DEMOGRAPHICS_RESPONSE_v1dot2,
    CRC_QUERY_FROM_DEFINITION_FEMALE_REQUEST_WITH_PATIENTSET,
    CRC_QUERY_FROM_DEFINITION_FEMALE_REQUEST_WITH_QUERYTOPIC,
    CRC_QUERY_MULTIPLE_MAPPINGS,
    CRC_QUERY_MULTIPLE_QUERIES,
    CRC_CICTR_UCSF_RESPONSE,
    CRC_CICTR_UDAVIS_RESPONSE,
    CRC_CICTR_UWASH_RESPONSE,
    CRC_DAO_EXCEPTION_RESPONSE,
    ONT_GET_NAME_INFO_REQUEST,
    ONT_GET_NAME_INFO_RESPONSE,
    PMSERVICE_GET_SERVICES_REQUEST,
    PMSERVICE_GET_SERVICES_RESPONSE,
    PMSERVICE_GET_VERSION_REQUEST,
    PMSERVICE_GET_VERSION_RESPONSE,
    PM_INVALID_LOGON_REQUEST,
    PM_INVALID_LOGON_RESPONSE,
    PM_MINIMAL_REQUEST,
    PM_VALID_LOGON_REQUEST,
    PM_VALID_LOGON_RESPONSE,
    QUERYTOOLSERVICE_CRC_QRY_GET_QUERY_MASTER_LIST_FROM_USER_ID_REQUEST,
    QUERYTOOLSERVICE_CRC_QRY_GET_QUERY_MASTER_LIST_FROM_USER_ID_RESPONSE,
    QUERYTOOLSERVICE_CRC_QRY_GET_QUERY_RESULT_INSTANCE_LIST_FROM_QUERY_INSTANCE_ID_REQUEST,
    QUERYTOOLSERVICE_CRC_QRY_GET_QUERY_RESULT_INSTANCE_LIST_FROM_QUERY_INSTANCE_ID_RESPONSE,
    QUERYTOOLSERVICE_CRC_QRY_GET_QUREY_INSTANCE_LIST_FROM_QUERY_MASTER_ID_REQUEST,
    QUERYTOOLSERVICE_CRC_QRY_GET_QUREY_INSTANCE_LIST_FROM_QUERY_MASTER_ID_RESPONSE,
    QUERYTOOLSERVICE_GET_PDO_FROM_INPUT_LIST_REQUEST,
    QUERYTOOLSERVICE_GET_PDO_FROM_INPUT_LIST_RESPONSE,
    SHRINE_QUERY_FROM_DEFINITION_DEMOGRAPHICS_0_9_YEARSOLD_REQUEST,
    WORKPLACESERVICE_GET_FOLDERS_BY_USER_ID_REQUEST,
    WORKPLACESERVICE_GET_FOLDERS_BY_USER_ID_RESPONSE,
    ONOLOGYSERVICE_GET_SCHEMES_REQUEST,
    ONOLOGYSERVICE_GET_SCHEMES_RESPONSE,
    ONOLOGYSERVICE_GET_CATEGORIES_REQUEST,
    ONOLOGYSERVICE_GET_CATEGORIES_RESPONSE,
    ONOLOGYSERVICE_GET_CHILDREN_REQUEST,
    ONOLOGYSERVICE_GET_CHILDREN_RESPONSE,
    ONOLOGYSERVICE_GET_CODE_INFO_REQUEST,
    ONOLOGYSERVICE_GET_CODE_INFO_RESPONSE,
    ONOLOGYSERVICE_GET_NAME_INFO_REQUEST,
    ONOLOGYSERVICE_GET_NAME_INFO_RESPONSE,
    CRC_INCOMPLETE_QUERY_RESPONSE;

    String xml = null;

    static protected final Logger log = Logger.getLogger(I2B2ExampleMessages.class);

    public String getFilename()
    {
        return name() + ".xml";
    }

    public File getAbsolutePath() throws ConfigException
    {
        return ConfigTool.getConfigFileWithFailover(getFilename());
    }

    //TODO: @see http://jira.open.med.harvard.edu/browse/BASE-446

    public String getXML() throws ConfigException, IOException
    {
        if(xml == null)
        {
            String filename = getFilename();

            log.debug("Attempting to get xml contents for " + filename);

            log.debug("Absolute path is believed to be " + getAbsolutePath());

            log.debug("Loading input stream from ConfigTool....");

            xml = FileUtils.read(ConfigTool.getConfigFileStream(getFilename()));

            log.debug("Loaded input stream : " + xml);
        }

        return xml;
    }

    public RequestMessageType getRequest() throws JAXBException, IOException, ConfigException
    {
        final Object o = JAXBUtils.unmarshal(getXML(), HiveJaxbContext.getInstance().getPackageList());
        return ((JAXBElement<RequestMessageType>) o).getValue();
    }

    public ResponseMessageType getResponse() throws JAXBException, IOException, ConfigException
    {
        final Object o = JAXBUtils.unmarshal(new StringReader(getXML()), HiveJaxbContext.getInstance().getPackageList());
        return ((JAXBElement<ResponseMessageType>) o).getValue();
    }
}
