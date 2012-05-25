package net.shrine.webclient.client.services;

import java.util.List;

import net.shrine.webclient.client.domain.OntNode;
import net.shrine.webclient.client.domain.TermSuggestion;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

/**
 * 
 * @author clint
 * @date Mar 23, 2012
 */
@RemoteServiceRelativePath("ontology")
public interface OntologySearchService extends RemoteService {
	List<TermSuggestion> getSuggestions(final String typedSoFar, final int limit);
	
	List<OntNode> getPathTo(final String term);
	
	List<OntNode> getChildrenFor(final String term);
}
