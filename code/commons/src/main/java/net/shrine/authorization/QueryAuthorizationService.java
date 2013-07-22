package net.shrine.authorization;

import edu.harvard.i2b2.crc.datavo.i2b2message.RequestMessageType;
import edu.harvard.i2b2.crc.datavo.i2b2message.ResponseMessageType;
import net.shrine.protocol.ReadApprovedQueryTopicsRequest;
import net.shrine.protocol.ReadApprovedQueryTopicsResponse;
import net.shrine.protocol.RunQueryRequest;
import net.shrine.protocol.ShrineRequest;
import net.shrine.protocol.ShrineResponse;
import net.shrine.serializers.pm.PMInvalidLogonException;
import org.spin.query.message.serializer.SerializationException;
import org.spin.tools.config.ConfigException;

import javax.xml.bind.JAXBException;
import java.io.IOException;

/**
 * @author Bill Simons
 * @date Aug 24, 2010
 * @link http://cbmi.med.harvard.edu
 * @link http://chip.org
 * <p/>
 * NOTICE: This software comes with NO guarantees whatsoever and is
 * licensed as Lgpl Open Source
 * @link http://www.gnu.org/licenses/lgpl.html
 */
public interface QueryAuthorizationService
{
    void authorizeRunQueryRequest(RunQueryRequest request) throws AuthorizationException;

    ReadApprovedQueryTopicsResponse readApprovedEntries(ReadApprovedQueryTopicsRequest request) throws SerializationException, JAXBException, IOException, ConfigException, PMInvalidLogonException;

}
