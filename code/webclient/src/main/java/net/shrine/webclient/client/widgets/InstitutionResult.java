package net.shrine.webclient.client.widgets;

import com.allen_sauer.gwt.log.client.Log;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.SpanElement;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import net.shrine.webclient.client.state.InstitutionResultClickedEvent;
import net.shrine.webclient.client.util.Util;
import net.shrine.webclient.shared.domain.SingleInstitutionQueryResult;

/**
 * @author clint
 * @date Apr 25, 2012
 */
public final class InstitutionResult extends Composite {

    private static final long MinimumSetSizeToDisplay = 10;

    private static InstitutionResultUiBinder uiBinder = GWT.create(InstitutionResultUiBinder.class);

    @UiField
    SpanElement name;

    @UiField
    SpanElement quantity;
    
    @UiField
    SpanElement obfuscationAmount;

    private final SingleInstitutionQueryResult result;

    public HandlerRegistration addClickHandler(ClickHandler handler) {
        return addDomHandler(handler, ClickEvent.getType());
    }

    interface InstitutionResultUiBinder extends UiBinder<Widget, InstitutionResult> {
    }


    public InstitutionResult(final EventBus eventBus, final int index, final String instName, final SingleInstitutionQueryResult singleInstitutionResult) {
        super();

        Util.requireNotNull(instName);
        Util.requireNotNull(eventBus);
        this.result = singleInstitutionResult;

        initWidget(uiBinder.createAndBindUi(this));

        name.setInnerText(instName);
        
        setQuantityText(singleInstitutionResult);

        addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                Log.debug("Institution result " + instName + " clicked");
                
                eventBus.fireEvent(new InstitutionResultClickedEvent(index, singleInstitutionResult));
            }
        });

    }

    private void setQuantityText(final SingleInstitutionQueryResult singleInstitutionResult) {
        //TODO: Find better way to hide the "+/- N"
        if(areFewerPatientsThanMinimum(singleInstitutionResult.getCount())) {
            obfuscationAmount.setInnerText("");
        }
        
        quantity.setInnerText(asString(singleInstitutionResult.getCount()));
    }

    private boolean areFewerPatientsThanMinimum(final long resultSetSize) {
        return resultSetSize < MinimumSetSizeToDisplay;
    }

    String asString(final long resultSetSize) {
        if(areFewerPatientsThanMinimum(resultSetSize)) {
            return "< " + MinimumSetSizeToDisplay;
        }

        return String.valueOf(resultSetSize);
    }

    void setSelected(boolean isSelected) {
        if(isSelected) {
            addStyleName("resultInstitutionNameSelected");
        } else {
            removeStyleName("resultInstitutionNameSelected");
        }
    }

    public SingleInstitutionQueryResult getResult() {
        return result;
    }
}
