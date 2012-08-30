package net.shrine.webclient.client.widgets.suggest;

import net.shrine.webclient.client.util.Observable;
import net.shrine.webclient.client.util.Observer;
import net.shrine.webclient.client.util.ReadOnlyObservable;
import net.shrine.webclient.client.util.Util;

import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * 
 * @author clint
 * @date Apr 17, 2012
 */
public final class SuggestionsPanel extends FlowPanel implements Observer {
	private Observable<Integer> highlightedRow;

	SuggestionsPanel(final Observable<Integer> highlightedRow) {
		super();
		
		Util.requireNotNull(highlightedRow);
		
		this.highlightedRow = highlightedRow;
		
		this.highlightedRow.observedBy(this);
		
		//TODO: HACK ALERT
		this.addStyleName("autosuggest");
		
		this.inform();
	}

	@Override
	public void inform() {
		for(final Widget w : this) {
			((RichSuggestionRow)w).unHighlight();
		}
		
		if(highlightedRow.isDefined()) {
			final int index = highlightedRow.get();
			
			if(index >= 0 && index < Util.count(this)) {
				((RichSuggestionRow)this.getWidget(index)).highlight();
			}
		}
	}

	ReadOnlyObservable<Integer> getHighlightedRow() {
		return highlightedRow;
	}

	@Override
	public void stopObserving() {
		this.highlightedRow.noLongerObservedBy(this);
		
		this.highlightedRow = null;
	}
}
