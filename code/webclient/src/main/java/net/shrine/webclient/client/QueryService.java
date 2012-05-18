package net.shrine.webclient.client;

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
	public HashMap<String, IntWrapper> queryForBreakdown(final String expr) throws IllegalArgumentException;
}

