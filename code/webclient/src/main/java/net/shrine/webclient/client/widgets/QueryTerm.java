package net.shrine.webclient.client.widgets;

import net.shrine.webclient.client.domain.Term;
import net.shrine.webclient.client.util.Util;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

/**
 * 
 * @author clint
 * @date Mar 26, 2012
 */
public final class QueryTerm extends Composite {

	private static final QueryTermUiBinder uiBinder = GWT.create(QueryTermUiBinder.class);

	interface QueryTermUiBinder extends UiBinder<Widget, QueryTerm> { }

	@UiField
	Label termLabel;
	
	//TODO: Get simplename from elsewhere (it comes down from the server with suggestions)
	public QueryTerm(final Term term) {
		Util.requireNotNull(term);
		
		initWidget(uiBinder.createAndBindUi(this));
		
		termLabel.setText(toSimpleName(term.value));
		termLabel.setTitle(term.value);
	}
	
	@Deprecated
	private final String toSimpleName(final String term) {
		final char forwardSlash = '\\';
		
		final String withoutTrailingSlash;
		
		if(term.endsWith(String.valueOf(forwardSlash))) {
			withoutTrailingSlash = term.substring(0, term.length() - 1);
		} else {
			withoutTrailingSlash = term;
		}
		
		final int locationOfLastSlash = withoutTrailingSlash.lastIndexOf(forwardSlash); 
		
		return withoutTrailingSlash.substring(locationOfLastSlash + 1);
	}
}
