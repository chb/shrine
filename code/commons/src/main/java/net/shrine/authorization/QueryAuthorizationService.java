package net.shrine.authorization;

import java.io.IOException;

import javax.xml.bind.JAXBException;

import net.shrine.protocol.ReadApprovedQueryTopicsRequest;
import net.shrine.protocol.ReadApprovedQueryTopicsResponse;
import net.shrine.protocol.RunQueryRequest;

import org.spin.message.serializer.SerializationException;
import org.spin.tools.config.ConfigException;

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

    ReadApprovedQueryTopicsResponse readApprovedEntries(ReadApprovedQueryTopicsRequest request) throws SerializationException, JAXBException, IOException, ConfigException;
}
