package net.shrine.webclient.client;

import net.shrine.webclient.client.domain.TermSuggestion;
import net.shrine.webclient.client.widgets.AutoSuggestRow;

import com.google.gwt.user.client.ui.SuggestOracle.Suggestion;

/**
 * 
 * @author clint
 * @date Mar 30, 2012
 */
public final class OntologySuggestion implements Suggestion {
	private final TermSuggestion termSuggestion;

	public OntologySuggestion(final TermSuggestion termSuggestion) {
		this.termSuggestion = termSuggestion;
	}

	public TermSuggestion getTermSuggestion() {
		return termSuggestion;
	}

	@Override
	public String getReplacementString() {
		return termSuggestion.getSimpleName();
	}

	@Override
	public String getDisplayString() {
		// TODO: Highlight
		// We can't add actual widgets to the dropdown produced by a SuggestBox, but we can construct a widget
		// here and add call toString() on it, which returns the HTML for that widget.  A hack, but better than
		// reinventing SuggestBox, or inlining HTML here. :(
		return (new AutoSuggestRow(null, termSuggestion)).toString();
	}
}