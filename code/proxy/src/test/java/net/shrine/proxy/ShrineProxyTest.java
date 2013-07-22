package net.shrine.proxy;

import org.apache.log4j.Logger;
import org.spin.extension.JDOMTool;
import org.spin.tools.SPINUnitTest;

/**
 * [ Author ]
 *
 * @author Ricardo Delima
 * @author Andrew McMurry
 *
 * Date: Apr 1, 2008
 * Harvard Medical School Center for BioMedical Informatics
 *
 * @link http://cbmi.med.harvard.edu
 * <p/>
 * [ In partnership with ]
 * @link http://chip.org
 * @link http://lcs.mgh.harvard.edu
 * @link http://www.brighamandwomens.org
 * @link http://bidmc.harvard.edu
 * @link http://dfhcc.harvard.edu
 * @link http://spin.nci.nih.gov/
 * <p/>
 * <p/>
 * ----------------------------------------------------------
 * [ All net.shrine.* code is available per the I2B2 license]
 * @link https://www.i2b2.org/software/i2b2_license.html
 * ----------------------------------------------------------
 */
public abstract class ShrineProxyTest extends SPINUnitTest
{
    
    private static final Logger log = Logger.getLogger(ShrineProxyTest.class) ;
    private String REDIRECT_URL       = "http://webservices.i2b2.org/i2b2/rest/QueryToolService/";
    private String REDIRECT_URL_HTTPS = "https://webservices.i2b2.org/i2b2/rest/QueryToolService/";    
    
    public static boolean DO_HTTPS_TEST = false; 
    
    public void testProxyDirectly() throws Exception
    {
        final ShrineProxy proxy = new ShrineProxy();

        log.info("Redirecting a CRC request message to: " + REDIRECT_URL);
        log.info("Response: " + proxy.redirect(new JDOMTool(getQuery(REDIRECT_URL))));

        if(DO_HTTPS_TEST)
        {
            log.info("Redirecting an HTTPS CRC request message to: " + REDIRECT_URL_HTTPS);
            log.info("Response: " + proxy.redirect(new JDOMTool(getQuery(REDIRECT_URL_HTTPS))));
        }
    }

    private String getQuery(String url) 
    {
		return	"<ns6:request xmlns:ns4=\"http://www.i2b2.org/xsd/cell/crc/psm/1.1/\" xmlns:ns7=\"http://www.i2b2.org/xsd/cell/ont/1.1/\" xmlns:ns3=\"http://www.i2b2.org/xsd/cell/crc/pdo/1.1/\" xmlns:ns5=\"http://www.i2b2.org/xsd/hive/plugin/\" xmlns:ns2=\"http://www.i2b2.org/xsd/hive/pdo/1.1/\" xmlns:ns6=\"http://www.i2b2.org/xsd/hive/msg/1.1/\" xmlns:ns8=\"http://www.i2b2.org/xsd/cell/crc/psm/querydefinition/1.1/\">\n" + 
				"    <message_header>\n" + 
				"    <proxy><redirect_url>" + url   + 
				"    </redirect_url></proxy>\n" +
		                "      <sending_application>\n" +
		                "          <application_name>i2b2_QueryTool</application_name>\n" +
		                "          <application_version>0.2</application_version>\n" +
		                "      </sending_application>\n" +
		                "      <sending_facility>\n" +
		                "          <facility_name>PHS</facility_name>\n" +
		                "      </sending_facility>\n" +
		                "      <receiving_application>\n" +
		                "          <application_name>i2b2_DataRepositoryCell</application_name>\n" +
		                "          <application_version>0.2</application_version>\n" +
		                "      </receiving_application>\n" +
		                "      <receiving_facility>\n" +
		                "          <facility_name>PHS</facility_name>\n" +
		                "      </receiving_facility>\n" +
		                "      <security>\n" +
		                "          <domain>Harvard Demo</domain>\n" +
		                "          <username>demo</username>\n" +
		                "          <password>demouser</password>\n" +
		                "      </security>\n" +
		                "      <message_type>\n" +
		                "          <message_code>Q04</message_code>\n" +
		                "          <event_type>EQQ</event_type>\n" +
		                "      </message_type>\n" +
		                "      <message_control_id>\n" +
		                "          <message_num>fXO4nxn7O2i9hGPrEgqW</message_num>\n" +
		                "          <instance_num>0</instance_num>\n" +
		                "      </message_control_id>\n" +
		                "      <processing_id>\n" +
		                "          <processing_id>P</processing_id>\n" +
		                "          <processing_mode>I</processing_mode>\n" +
		                "      </processing_id>\n" +
		                "      <accept_acknowledgement_type>messageId</accept_acknowledgement_type>\n" +
		                "      <project_id xsi:nil=\"true\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"/>\n" +
		                "  </message_header>\n" +
		                "  <request_header>\n" +
		                "      <result_waittime_ms>180000</result_waittime_ms>\n" +
		                "  </request_header>\n" +
		                "  <message_body>\n" +
		                "      <ns4:psmheader>\n" +
		                "          <user group=\"Asthma\" login=\"demo\">demo</user>\n" +
		                "          <patient_set_limit>0</patient_set_limit>\n" +
		                "          <estimated_time>0</estimated_time>\n" +
		                "          <request_type>CRC_QRY_runQueryInstance_fromQueryDefinition</request_type>\n" +
		                "      </ns4:psmheader>\n" +
		                "      <ns4:request xsi:type=\"ns4:query_definition_requestType\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">\n" +
		                "          <query_definition>\n" +
		                "              <query_name>0-9 years old@05:42:40</query_name>\n" +
		                "              <specificity_scale>0</specificity_scale>\n" +
		                "              <panel>\n" +
		                "                  <panel_number>1</panel_number>\n" +
		                "                  <invert>0</invert>\n" +
		                "                  <total_item_occurrences>1</total_item_occurrences>\n" +
		                "                  <item>\n" +
		                "                      <hlevel>3</hlevel>\n" +
		                "                      <item_name>0-9 years old</item_name>\n" +
		                "                      <item_key>\\\\i2b2\\i2b2\\Demographics\\Age\\0-9 years old</item_key>\n" +
		                "                      <tooltip>Demographic \\ Age \\ 0-9 years old</tooltip>\n" +
		                "                      <class>ENC</class>\n" +
		                "                      <constrain_by_date/>\n" +
		                "                  </item>\n" +
		                "              </panel>\n" +
		                "          </query_definition>\n" +
		                "      </ns4:request>\n" +
		                "  </message_body>\n" +
		                "</ns6:request>";
		
		
	}
 
    	

}
