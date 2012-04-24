package net.shrine.webclient.client.widgets;

import java.util.Arrays;

import net.shrine.webclient.client.Controllers;
import net.shrine.webclient.client.OntologySuggestOracle;
import net.shrine.webclient.client.domain.Term;
import net.shrine.webclient.client.domain.TermSuggestion;
import net.shrine.webclient.client.widgets.suggest.RichSuggestBox;
import net.shrine.webclient.client.widgets.suggest.RichSuggestionEvent;
import net.shrine.webclient.client.widgets.suggest.RichSuggestionEventHandler;
import net.shrine.webclient.client.widgets.suggest.WidgetMaker;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.ui.Widget;

/**
 * 
 * @author clint
 * @date Mar 23, 2012
 */
public final class OntologySearchBox extends RichSuggestBox<TermSuggestion> {

	public static final String ID = "ontSearchInput";

	public OntologySearchBox(final EventBus eventBus, final Controllers controllers) {
		super(new OntologySuggestOracle(), NullWidgetMaker);
		
		this.setText("Enter search terms");
		
		this.addSelectionHandler(new RichSuggestionEventHandler<TermSuggestion>() {
			@Override
			public void onSelectionMade(final RichSuggestionEvent<TermSuggestion> event) {
				final TermSuggestion suggestion = event.getSuggestion();
				
				final Term term = new Term(suggestion.getPath(), suggestion.getSimpleName());
				
				controllers.queryBuilding.addNewTerm(term);
				
				OntologySearchBox.this.setText("");
			}
		});
		
		//TODO: Very hackish
		//TODO: Are all these classes needed?
		for(final String styleName : Arrays.asList("searchInput", "ui-state-default", "autocomplete")) {
			this.addStyleName(styleName);
		}
		
		//TODO: Very hackish; means there can only be one OntologySearchBox. :(
		this.getElement().setId(ID);
	}
	
	public void wireUp(final EventBus eventBus) {
		this.setWidgetMaker(AutoSuggestRow.autoSuggestRowWidgetMaker(eventBus, this));
	}
	
	private static final WidgetMaker<TermSuggestion> NullWidgetMaker = new WidgetMaker<TermSuggestion>() {
		@Override
		public Widget makeWidget(final TermSuggestion suggestionInput) {
			throw new IllegalStateException("This WidgetMaker should never be invoked.  Did you call wireUp() on your OntologySearchBox?");
		}
	};
}
