package net.shrine.Identity;

import net.shrine.identity.CarraIdentityService;
import org.apache.commons.codec.binary.Base64;
import org.spin.tools.JAXBUtils;
import org.spin.tools.SPINUnitTest;
import org.spin.tools.crypto.signature.Identity;
import org.spin.tools.crypto.signature.XMLSignatureUtil;


/**
 * Testing the carranet version of the identity service
 */
public class TestCarraIdentityService extends SPINUnitTest {


    public void testCarraIdentityService() throws Exception {
        Base64 base64 = new Base64();

        Identity id1 = new Identity("domain_no_matter", "kenny");
        id1 = XMLSignatureUtil.getDefaultInstance().sign(id1);

        Identity id2 = new Identity("domain_no_matter", "stan");
        id2 = XMLSignatureUtil.getDefaultInstance().sign(id2);

        Identity id3 = new Identity("domain_no_matter", "cartman");
        id3 = XMLSignatureUtil.getDefaultInstance().sign(id3);


        CarraIdentityService service = new CarraIdentityService();
        id1 = service.certify("domain_no_matter", "kenny", new String(base64.encode(JAXBUtils.marshalToString(id1).getBytes())));
        id2 = service.certify("domain_no_matter", "stan", new String(base64.encode(JAXBUtils.marshalToString(id2).getBytes())));
        id3 = service.certify("domain_no_matter", "cartman", new String(base64.encode(JAXBUtils.marshalToString(id3).getBytes())));

        assertEquals(true, XMLSignatureUtil.getDefaultInstance().verifySignature(id1));
        assertEquals(true, XMLSignatureUtil.getDefaultInstance().verifySignature(id2));
        assertEquals(true, XMLSignatureUtil.getDefaultInstance().verifySignature(id3));

    }


}
