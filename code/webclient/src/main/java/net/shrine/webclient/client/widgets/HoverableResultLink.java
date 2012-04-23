package net.shrine.webclient.client.widgets;

import java.util.HashMap;

import net.shrine.webclient.client.domain.IntWrapper;
import net.shrine.webclient.client.util.Util;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOutHandler;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.event.dom.client.MouseOverHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DecoratedPopupPanel;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * 
 * @author clint
 * @date Apr 4, 2012
 */
public final class HoverableResultLink extends Composite {

	private static HoverableResultLinkUiBinder uiBinder = GWT.create(HoverableResultLinkUiBinder.class);

	interface HoverableResultLinkUiBinder extends UiBinder<Widget, HoverableResultLink> { }

	@UiField
	Anchor delegate;
	
	private PopupPanel resultTooltip;
	
	//NB: For testing
	final HashMap<String, IntWrapper> results;
	
	public HoverableResultLink(final HashMap<String, IntWrapper> results) {
		initWidget(uiBinder.createAndBindUi(this));
		
		Util.requireNotNull(results);
		
		this.results = results;
		
		//TODO: TOTAL HACK: use popup panel to make tooltip look passable for demo
		//delegate.setTitle((new ResultTooltip(results)).toString());
		delegate.setTitle(null);
		
		delegate.addMouseOverHandler(new MouseOverHandler() {
			@Override
			public void onMouseOver(MouseOverEvent event) {
				showPopup(event.getClientX(), event.getClientY());
			}
		});
		
		delegate.addMouseOutHandler(new MouseOutHandler() {
			@Override
			public void onMouseOut(MouseOutEvent event) {
				if(resultTooltip != null) {
					hidePopup();
				}
			}
		});
	}

	PopupPanel getResultTooltip() {
		return resultTooltip;
	}

	void showPopup(final int left, final int bottom) {
		resultTooltip = new DecoratedPopupPanel(false, false);
		
		resultTooltip.setStyleName("");
		
		resultTooltip.setWidget(new ResultTooltip(results));
		
		resultTooltip.setPopupPosition(left, bottom);
		
		resultTooltip.show();
	}

	private void hidePopup() {
		resultTooltip.hide();
		
		resultTooltip.clear();
	}
}
