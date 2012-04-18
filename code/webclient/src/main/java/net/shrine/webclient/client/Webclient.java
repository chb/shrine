package net.shrine.webclient.client;

import java.util.List;

import net.shrine.webclient.client.domain.OntNode;
import net.shrine.webclient.client.events.ShowBrowsePopupEvent;
import net.shrine.webclient.client.events.ShowBrowsePopupEventHandler;
import net.shrine.webclient.client.widgets.OntologySearchBox;
import net.shrine.webclient.client.widgets.OntologyTree;
import net.shrine.webclient.client.widgets.WebClientWrapper;

import com.allen_sauer.gwt.log.client.Log;
import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.event.shared.SimpleEventBus;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.RootPanel;

/**
 * Entry point classes define <code>onModuleLoad()</code>.
 */
public final class Webclient implements EntryPoint {

	public static final SimpleEventBus EventBus = new SimpleEventBus();
	
	private void doOnModuleLoad() {
		final State state = new State();

		final Controllers controllers = new Controllers(state);
		
		final WebClientWrapper wrapper = new WebClientWrapper();

		final OntologySearchBox ontSearchBox = new OntologySearchBox(EventBus, controllers);
		
		ontSearchBox.wireUp(EventBus);
		
		wrapper.wireUp(state, controllers, ontSearchBox);

		final PopupPanel popupPanel = new PopupPanel(true);
		
		EventBus.addHandler(ShowBrowsePopupEvent.getType(), new ShowBrowsePopupEventHandler() {
			@Override
			public void handle(final ShowBrowsePopupEvent event) {
				final OntologySearchServiceAsync ontologyService = GWT.create(OntologySearchService.class);

				//ontologyService.getTreeRootedAt("\\\\SHRINE\\SHRINE\\", new AsyncCallback<List<OntNode>>() {
				ontologyService.getTreeRootedAt(event.getStartingTerm().value, new AsyncCallback<List<OntNode>>() {
					@Override
					public void onSuccess(List<OntNode> result) {
						if (popupPanel.isShowing()) {
							popupPanel.hide();
						} else {
							popupPanel.setWidget(new OntologyTree(controllers, result.get(0)));

							// TODO: TOTAL HACK
							final Element searchBox = DOM.getElementById(OntologySearchBox.ID);

							popupPanel.setPopupPosition(searchBox.getAbsoluteLeft(), searchBox.getAbsoluteBottom());
							popupPanel.setWidth("929px");

							// TODO: END TOTAL HACK

							popupPanel.show();
						}
					}

					@Override
					public void onFailure(Throwable caught) {
						Log.error("Failed to browse ontology: " + caught.getMessage(), caught);
					}
				});
			}
		});
		
		RootPanel.get().add(wrapper);
		
		Log.info("Shrine Webclient loaded");
	}
	
	public void onModuleLoad() {

		// Install an UncaughtExceptionHandler which will produce FATAL log messages (useful when not in dev mode)
		Log.setUncaughtExceptionHandler();

		// use deferred command to catch initialization exceptions in doOnModuleLoad
	    Scheduler.get().scheduleDeferred(new ScheduledCommand() {
			@Override
			public void execute() {
				doOnModuleLoad();
			}
		});
	}
}
