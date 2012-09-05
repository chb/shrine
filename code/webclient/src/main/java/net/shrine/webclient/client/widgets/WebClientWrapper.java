package net.shrine.webclient.client.widgets;

import com.allen_sauer.gwt.log.client.Log;
import net.shrine.webclient.client.controllers.Controllers;
import net.shrine.webclient.client.services.BootstrapService;
import net.shrine.webclient.client.state.State;
import net.shrine.webclient.shared.domain.BootstrapInfo;

import com.allen_sauer.gwt.dnd.client.PickupDragController;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import org.fusesource.restygwt.client.Method;
import org.fusesource.restygwt.client.MethodCallback;

/**
 * 
 * @author clint
 * @date Mar 27, 2012
 */
public final class WebClientWrapper extends Composite {

    private static final WebClientWrapperUiBinder uiBinder = GWT.create(WebClientWrapperUiBinder.class);

    interface WebClientWrapperUiBinder extends UiBinder<Widget, WebClientWrapper> {
    }

    @UiField
    WebClientContent content;

    @UiField
    Header header;

    @UiField
    VersionsPanel versions;

    public WebClientWrapper() {
        super();

        initWidget(uiBinder.createAndBindUi(this));
    }

    public void wireUp(final EventBus eventBus, final State state, final Controllers controllers, final OntologySearchBox ontSearchBox, final PickupDragController dragController, BootstrapService bootstrapService) {
        this.content.wireUp(eventBus, state, controllers, ontSearchBox, dragController);

        final Header myHeader = header;
        bootstrapService.getBootstrapinfo(new MethodCallback<BootstrapInfo>() {
            @Override
            public void onFailure(Method method, Throwable throwable) {
                Log.error("Failed to retrieve bootstrap info");
                myHeader.wireUp(eventBus, "User");
            }

            @Override
            public void onSuccess(Method method, BootstrapInfo bootstrapInfo) {
                myHeader.wireUp(eventBus, bootstrapInfo.getLoggedInUsername());
            }
        });
    }
}
