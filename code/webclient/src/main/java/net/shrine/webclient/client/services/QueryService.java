package net.shrine.webclient.client.services;

import java.util.HashMap;

import com.google.gwt.user.client.rpc.RemoteService;

/**
 * 
 * @author clint
 * @date Mar 23, 2012
 */
//@RemoteServiceRelativePath("query")
public interface QueryService {// extends RemoteService {
	//NB: Must be a HashMap (and not a Map) for GWT serialization purposes.
	HashMap<String, Integer> queryForBreakdown(final String expr);
}

