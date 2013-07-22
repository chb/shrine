package net.shrine.serializers.hive;

import static net.shrine.serializers.hive.HiveCommonSerializer.getTemplateResponseMessageTypeError;

import org.apache.log4j.Logger;

import edu.harvard.i2b2.crc.datavo.i2b2message.ResponseMessageType;
import net.shrine.serializers.I2B2ExampleMessages;
import net.shrine.serializers.SerializerUnitTest;

/**
 * @author Andrew McMurry, MS
 *         <p/>
 *         With primary support from Children's Hospital Informatics Program @
 *         Harvard-MIT Health Sciences and Technology and
 *         <p/>
 *         Secondary support from the Harvard Medical School Center for
 *         BioMedical Informatics
 *         <p/>
 *         PHD candidate, Boston University Bioinformatics Member, I2b2 National
 *         Center for Biomedical Computing
 *         <p/>
 *         All works licensed under LGPL
 *         <p/>
 *         User: andy Date: Apr 6, 2010 Time: 2:30:37 PM
 */
public final class HiveCommonSerializerTest extends SerializerUnitTest
{
    private static final Logger log = Logger.getLogger(HiveCommonSerializer.class);
    
    private static final boolean DEBUG = log.isDebugEnabled();
    
    public void testGetTemplateResponseMessageTypeError() throws Exception
    {
        // TODO http://jira.open.med.harvard.edu/browse/SHRINE-234
        final ResponseMessageType response = getTemplateResponseMessageTypeError(I2B2ExampleMessages.CRC_GET_QUERY_RESULT_INSTANCE_LIST_FROM_QUERY_INSTANCE_REQUEST.getRequest(), 
                                                                                 "Could not get query instance list");
        if(DEBUG)
        {
            log.debug(HiveCommonSerializer.toXMLString(response));
        }
    }
    
    public void testAddResponseHeaderWithDoneStatus() throws Exception
    {
        final ResponseMessageType response = new ResponseMessageType();
        
        HiveCommonSerializer.addResponseHeaderWithDoneStatus(response);
        
        assertNotNull(response.getResponseHeader());
        assertNotNull(response.getResponseHeader().getResultStatus());
        assertNotNull(response.getResponseHeader().getResultStatus().getStatus());
        assertEquals("DONE", response.getResponseHeader().getResultStatus().getStatus().getValue());
        assertEquals("DONE", response.getResponseHeader().getResultStatus().getStatus().getType());
    }
}
