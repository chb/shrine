package net.shrine.serializers.pm;

import edu.harvard.i2b2.crc.datavo.pm.UserType;
import org.apache.log4j.Logger;
import org.spin.query.message.identity.IdentityService;
import org.spin.query.message.identity.IdentityServiceException;
import org.spin.tools.config.ConfigException;
import org.spin.tools.crypto.signature.Identity;
import org.spin.tools.crypto.signature.XMLSignatureUtil;

/**
 * REFACTORED
 *
 * @author Andrew McMurry, MS
 * @date Mar 25, 2010 (REFACTORED)
 * @link http://cbmi.med.harvard.edu
 * @link http://chip.org
 * <p/>
 * NOTICE: This software comes with NO guarantees whatsoever and is licensed as Lgpl Open Source
 * @link http://www.gnu.org/licenses/lgpl.html
 */
public class PMIdentityService implements IdentityService
{
    public static final Logger log = Logger.getLogger(IdentityService.class);

    public static final boolean DEBUG = log.isDebugEnabled();
    public static final boolean INFO = log.isInfoEnabled();

    private final PMHttpClient pmClient;

    public PMIdentityService(final PMHttpClient pmClient) throws ConfigException
    {
        this.pmClient = pmClient;
    }

    public Identity certify(final String domain, final String username, final String password) throws IdentityServiceException
    {
        try
        {
            final UserType userType = pmClient.getUserConfiguration(domain, username, password);
            if(userType == null)
            {
                throw new IdentityServiceException(String.format("User %s does not exist", username));
            }
            String ecommonsUsername = PMSerializer.extractEcommonsUsername(userType);

            if(ecommonsUsername == null)
            {
                throw new IdentityServiceException(String.format("No ecommons id for user %s", username));
            }
            final Identity identity = new Identity(domain, ecommonsUsername);

            return XMLSignatureUtil.getDefaultInstance().sign(identity);
        }
        catch(final Exception e)
        {
            throw new IdentityServiceException("Failed to certify user", e);
        }
    }
}