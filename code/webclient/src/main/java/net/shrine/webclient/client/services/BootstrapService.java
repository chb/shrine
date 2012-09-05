package net.shrine.webclient.client.services;

import net.shrine.webclient.shared.domain.BootstrapInfo;

import org.fusesource.restygwt.client.MethodCallback;
import org.fusesource.restygwt.client.RestService;

import javax.ws.rs.GET;
import javax.ws.rs.Path;

/**
 * @author Bill Simons
 * @date 9/5/12
 * @link http://cbmi.med.harvard.edu
 * @link http://chip.org
 * <p/>
 * NOTICE: This software comes with NO guarantees whatsoever and is
 * licensed as Lgpl Open Source
 * @link http://www.gnu.org/licenses/lgpl.html
 */
public interface BootstrapService extends RestService {

    @GET
    @Path("rest/api/bootstrap")
    void getBootstrapinfo(final MethodCallback<BootstrapInfo> callback);
}
