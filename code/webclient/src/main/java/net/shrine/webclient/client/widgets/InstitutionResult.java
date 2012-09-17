package net.shrine.webclient.client.widgets;

import com.allen_sauer.gwt.log.client.Log;
import com.google.gwt.dom.client.SpanElement;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.shared.HandlerRegistration;
import net.shrine.webclient.client.util.Util;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;

/**
 * 
 * @author clint
 * @date Apr 25, 2012
 */
public final class InstitutionResult extends Composite {

	private static final int MinimumSetSizeToDisplay = 10;
	
	private static InstitutionResultUiBinder uiBinder = GWT.create(InstitutionResultUiBinder.class);

    @UiField
    SpanElement name;

    @UiField
    SpanElement quantity;

    public HandlerRegistration addClickHandler(ClickHandler handler) {
        return addDomHandler(handler, ClickEvent.getType());
    }

    interface InstitutionResultUiBinder extends UiBinder<Widget, InstitutionResult> { }


	public InstitutionResult(final String instName, final int resultSetSize) {
		super();
		
		Util.requireNotNull(instName);
		
		initWidget(uiBinder.createAndBindUi(this));

        name.setInnerText(instName);
        quantity.setInnerText(asString(resultSetSize));

        addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                Log.debug("Institution result " + instName + " clicked");
                //send InstitutionResultClickedEvent
            }
        });

	}

	String asString(final int resultSetSize) {
		if(resultSetSize < MinimumSetSizeToDisplay) {
			return "< " + MinimumSetSizeToDisplay;
		}
		
		return String.valueOf(resultSetSize);
	}
}
