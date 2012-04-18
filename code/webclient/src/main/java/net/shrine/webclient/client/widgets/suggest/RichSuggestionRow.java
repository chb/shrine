package net.shrine.webclient.client.widgets.suggest;

import net.shrine.webclient.client.util.Util;

import com.allen_sauer.gwt.log.client.Log;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.dom.client.HasMouseOutHandlers;
import com.google.gwt.event.dom.client.HasMouseOverHandlers;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOutHandler;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.event.dom.client.MouseOverHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * 
 * @author clint
 * @date Apr 10, 2012
 */
public final class RichSuggestionRow extends SimplePanel implements HasMouseOutHandlers, HasMouseOverHandlers, HasClickHandlers {

	/**
	 * 
	 * @author clint
	 * @date Apr 11, 2012
	 */
	public static enum StyleNames {
		Highlighted, NotHighlighted;
		
		public final String toStyleName() {
			return "richSuggestionRow-" + name();
		}
	}
	
	private final HasHideablePopup container;
	private final Runnable onSelect;
	
	public RichSuggestionRow(final HasHideablePopup container, final Widget wrapped, final Runnable onSelect) {
		super(wrapped);
		
		Util.requireNotNull(container);
		Util.requireNotNull(wrapped);
		Util.requireNotNull(onSelect);
		
		this.container = container;
		this.onSelect = onSelect;
		
		unHighlight();
		
		this.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(final ClickEvent event) {
				Log.debug("RichSuggestionRow clicked: " + getWidget());
				
				container.hidePopup();
			}
		});
	}
	
	@Override
	public HandlerRegistration addClickHandler(ClickHandler handler) {
		return this.addDomHandler(handler, ClickEvent.getType());
	}

	@Override
	public HandlerRegistration addMouseOverHandler(final MouseOverHandler handler) {
		return this.addDomHandler(handler, MouseOverEvent.getType());
	}

	@Override
	public HandlerRegistration addMouseOutHandler(MouseOutHandler handler) {
		return this.addDomHandler(handler, MouseOutEvent.getType());
	}

	void highlight() {
		this.setStyleName(StyleNames.Highlighted.toStyleName());
	}

	void unHighlight() {
		this.setStyleName(StyleNames.NotHighlighted.toStyleName());
	}

	void select() {
		onSelect.run();
		
		container.hidePopup();
	}
}
