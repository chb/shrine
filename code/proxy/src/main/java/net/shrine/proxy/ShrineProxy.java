package net.shrine.proxy;

import net.shrine.serializers.HTTPClient;
import org.apache.log4j.Logger;
import org.spin.extension.JDOMTool;
import org.spin.tools.config.ConfigException;
import org.spin.tools.config.ConfigTool;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * [ Author ]
 *
 * @author Ricardo Delima
 * @author Andrew McMurry
 * @author Britt Fitch
 *         <p/>
 *         Date: Apr 1, 2008
 *         Harvard Medical School Center for BioMedical Informatics
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
public class ShrineProxy
{
    private static final Logger log = Logger.getLogger(ShrineProxy.class);
    private static final boolean DEBUG = log.isDebugEnabled();

    private List<String> whiteList = new ArrayList<String>();
    private List<String> blackList = new ArrayList<String>();

    public ShrineProxy() throws ConfigException
    {
        log.info("ProxyAdapter init: ");
        getLists();
    }

    private void getLists() throws ConfigException
    {
        try
        {
            File proxyconf = ConfigTool.getConfigFileWithFailover("shrine-proxy-acl.xml");

            JDOMTool jdom = new JDOMTool(new BufferedReader(new FileReader(proxyconf)));

            whiteList = jdom.getElementValues("//lists/whitelist/host");
            blackList = jdom.getElementValues("//lists/blacklist/host");
        }
        catch (Exception e)
        {
            log.error("ShrineProxy encountered a problem while checking ACL permissions: " + e);
            throw new ConfigException(e.getStackTrace().toString());
        }

        log.info("Loaded access control list.");

        for (String white : whiteList)
        {
            log.info("Whitelist entry:" + white);
        }

        for (String black : blackList)
        {
            log.info("Blacklist entry:" + black);
        }
    }

    private boolean isAllowableDomain(String redirectURL)
    {
        boolean returnValue = false;

        //check white list
        for (String s : whiteList)
        {
            if (redirectURL.startsWith(s))
            {
                returnValue = true;
                break;
            }
        }

        //check black list
        for (String s : blackList)
        {
            if (redirectURL.startsWith(s))
            {
                returnValue = false;
                break;
            }
        }

        return returnValue;
    }

    /**
     * Redirect to a URL embedded within the I2B2 message
     *
     * @param request
     * @return
     * @throws ShrineMessageFormatException bad input XML
     * @throws ConfigException              wrong keystore configuration for SSL
     * @throws IOException                  error in HTTP/TCP
     */
    public String redirect(JDOMTool request) throws ShrineMessageFormatException, ConfigException, IOException
    {

        String redirectURL;

        try
        {
            redirectURL = request.getElementValue("//redirect_url");
        }
        catch (Exception e)
        {
            log.error("Error parsing redirect_url tag: " + e.getStackTrace());
            throw new ShrineMessageFormatException(e);
        }

        if (redirectURL == null || redirectURL.length() == 0)
        {
            log.error("ShrineAdapter detected missing redirect_url tag");
            throw new ShrineMessageFormatException("ShrineAdapter detected missing redirect_url tag");
        }

        //if redirectURL is not in the white list or is in the black list, do not proceed.
        if (!isAllowableDomain(redirectURL))
        {
            throw new ShrineMessageFormatException("redirectURL not in white list or is in black list: " + redirectURL);
        }

        if (DEBUG)
        {
            log.debug("Proxy redirecting to " + redirectURL);
        }

        return HTTPClient.post(request.toString(), redirectURL);
    }
}
