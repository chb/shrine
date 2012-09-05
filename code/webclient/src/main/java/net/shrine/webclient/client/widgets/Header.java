package net.shrine.webclient.client.widgets;

import com.google.gwt.user.client.ui.SimplePanel;
import net.shrine.webclient.client.util.Util;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * 
 * @author clint
 * @date Apr 26, 2012
 */
public final class Header extends Composite {

    private static HeaderUiBinder uiBinder = GWT.create(HeaderUiBinder.class);

    interface HeaderUiBinder extends UiBinder<Widget, Header> { }

    @UiField
    Anchor queryHistoryLink;

    @UiField
    Anchor dataDictionaryLink;

    @UiField
    SimplePanel infoPlaceholder;

    private final PopupPanel queryHistoryPopup = new PopupPanel(true, false);

    private final PreviousQueriesPanel previousQueriesPanel = new PreviousQueriesPanel();

    private EventBus eventBus;

    public Header() {
        initWidget(uiBinder.createAndBindUi(this));

        queryHistoryPopup.hide();

        queryHistoryLink.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(final ClickEvent event) {
                if (queryHistoryPopup.isShowing()) {
                    queryHistoryPopup.hide();
                } else {
                    queryHistoryPopup.setWidget(previousQueriesPanel);

                    queryHistoryPopup.show();
                }
            }
        });
    }

    public void wireUp(final EventBus eventBus, String username) {
        Util.requireNotNull(eventBus);

        this.eventBus = eventBus;

        dataDictionaryLink.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(final ClickEvent event) {
                Header.this.eventBus.fireEvent(new ShowDataDictionaryPanelEvent(null));
            }
        });

        infoPlaceholder.setWidget(new LoggedInUserPanel(username));

    }
}
