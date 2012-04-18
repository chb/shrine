package net.shrine.webclient.client;

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
	public List<TermSuggestion> getSuggestions(final String typedSoFar, final int limit) throws IllegalArgumentException;
	
	public List<OntNode> getTreeRootedAt(final String term) throws IllegalArgumentException;
	
	public List<OntNode> getChildrenFor(final String term) throws IllegalArgumentException;
	
	public List<OntNode> getParentOntTree(final String term) throws IllegalArgumentException;
}
