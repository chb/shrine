package net.shrine.identity;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Base64;
import org.spin.query.message.identity.IdentityService;
import org.spin.query.message.identity.IdentityServiceException;
import org.spin.tools.JAXBUtils;
import org.spin.tools.crypto.signature.Identity;
import org.spin.tools.crypto.signature.XMLSignatureUtil;

import javax.xml.bind.JAXBException;
import java.io.UnsupportedEncodingException;


/**
 * @author Andrew McMurry, MS
 * @date Feb 22, 2010 (REFACTORED)
 * @link http://cbmi.med.harvard.edu
 * @link http://chip.org
 * <p/>
 * NOTICE: This software comes with NO guarantees whatsoever and is licensed as Lgpl Open Source
 * @link http://www.gnu.org/licenses/lgpl.html
 * <p/>
 * This class is for doing authentication the CarraNet way.
 */

public class CarraIdentityService implements IdentityService {


    /**
     * @param domain   doesn't matter
     * @param username the username
     * @param password the authString base64(signedIdentity)
     * @return Identity
     * @throws org.spin.query.message.identity.IdentityServiceException
     *
     */
    @Override
    public Identity certify(String domain, String username, String password) throws IdentityServiceException {

        if (username == null || password == null) {
            throw new IdentityServiceException("No username or password");
        }

        try {

            Identity id = convertAuthToIdentity(password);


            if (XMLSignatureUtil.getDefaultInstance().verifySignature(id) && username.equalsIgnoreCase(id.getUsername())) {
                //What we're doing is verifying that the ID came from someplace we trust
                //in this case most likely carra auth, and re-signing the Token
                //so that the end nodes in carra only need to trust the Broadcaster
                //cert and not the Auth Server's cert *AND* the broadcaster cert

                Identity resignedIdentity = new Identity(id.getDomain(), id.getUsername(), id.getAssertion());
                resignedIdentity = XMLSignatureUtil.getDefaultInstance().sign(resignedIdentity);
                return resignedIdentity;
            }
            throw new IdentityServiceException("Not authorized");

        } catch (Exception e) {
            throw new IdentityServiceException("Not authorized");
        }

    }

    private static Identity convertAuthToIdentity(String authHeaderString)
            throws JAXBException, UnsupportedEncodingException, DecoderException {
        Base64 base64 = new Base64();
        byte[] byteArray = (byte[]) base64.decode(authHeaderString);
        String xmlIdentity = new String(byteArray, "UTF-8");
        return JAXBUtils.unmarshal(xmlIdentity, Identity.class);
    }

}