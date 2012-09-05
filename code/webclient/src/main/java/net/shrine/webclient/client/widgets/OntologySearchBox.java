package net.shrine.webclient.client.widgets;

import net.shrine.webclient.client.controllers.Controllers;
import net.shrine.webclient.client.suggest.OntologySuggestOracle;
import net.shrine.webclient.client.widgets.suggest.RichSuggestBox;
import net.shrine.webclient.client.widgets.suggest.RichSuggestionEvent;
import net.shrine.webclient.client.widgets.suggest.RichSuggestionEventHandler;
import net.shrine.webclient.client.widgets.suggest.WidgetMaker;
import net.shrine.webclient.shared.domain.Term;
import net.shrine.webclient.shared.domain.TermSuggestion;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.ui.Widget;

/**
 * 
 * @author clint
 * @date Mar 23, 2012
 */
public final class OntologySearchBox extends RichSuggestBox<TermSuggestion> {

    public static final String ID = "searchbox";

    public OntologySearchBox(final EventBus eventBus, final Controllers controllers) {
        super(new OntologySuggestOracle(), NullWidgetMaker);

        this.setText("Enter search terms");

        this.addSelectionHandler(new RichSuggestionEventHandler<TermSuggestion>() {
            @Override
            public void onSelectionMade(final RichSuggestionEvent<TermSuggestion> event) {
                final Term term = event.getSuggestion().toTerm();

                controllers.queryBuilding.addNewTerm(term);

                OntologySearchBox.this.setText("");
            }
        });

        // TODO: Very hackish
        this.addStyleName("searchInput");

        this.setWidgetMaker(AutoSuggestRow.autoSuggestRowWidgetMaker(eventBus, this));

        // TODO: Very hackish; means there can only be one OntologySearchBox. :(
        this.getElement().setId(ID);
    }

    private static final WidgetMaker<TermSuggestion> NullWidgetMaker = new WidgetMaker<TermSuggestion>() {
        @Override
        public Widget makeWidget(final TermSuggestion suggestionInput) {
            throw new IllegalStateException("This WidgetMaker should never be invoked.  Did you call wireUp() on your OntologySearchBox?");
        }
    };
}
