package net.shrine.webclient.client.services;

import java.util.HashMap;

import net.shrine.webclient.client.domain.IntWrapper;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

/**
 * 
 * @author clint
 * @date Mar 23, 2012
 */
@RemoteServiceRelativePath("query")
public interface QueryService extends RemoteService {
	//NB: Must be a HashMap (and not a Map) for GWT serialization purposes.
	HashMap<String, IntWrapper> queryForBreakdown(final String expr);
}

