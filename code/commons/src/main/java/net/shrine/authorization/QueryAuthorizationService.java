package net.shrine.authorization;


import net.shrine.protocol.*;
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
