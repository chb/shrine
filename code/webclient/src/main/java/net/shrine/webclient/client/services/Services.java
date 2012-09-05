package net.shrine.webclient.client.services;

import org.fusesource.restygwt.client.Resource;
import org.fusesource.restygwt.client.RestService;
import org.fusesource.restygwt.client.RestServiceProxy;

import com.google.gwt.core.client.GWT;

/**
 * @author clint
 * @date Aug 6, 2012
 */
public final class Services {
    private Services() {
        super();
    }

    public static QueryService makeQueryService() {
        final Resource resource = makeResource();

        final QueryService queryService = GWT.create(QueryService.class);

        setResource(resource, queryService);

        return queryService;
    }

    public static OntologyService makeOntologyService() {
        final Resource resource = makeResource();

        final OntologyService ontService = GWT.create(OntologyService.class);

        setResource(resource, ontService);

        return ontService;
    }

    public static BootstrapService makeBootstrapService() {
        final Resource resource = makeResource();

        final BootstrapService bootstrapService = GWT.create(BootstrapService.class);

        setResource(resource, bootstrapService);

        return bootstrapService;
    }

    private static void setResource(final Resource resource, final RestService service) {
        ((RestServiceProxy) service).setResource(resource);
    }

    private static Resource makeResource() {
        return new Resource(GWT.getModuleBaseURL());
    }
}
