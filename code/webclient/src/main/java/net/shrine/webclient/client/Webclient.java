package net.shrine.webclient.client;

import net.shrine.webclient.client.widgets.OntologySearchBox;
import net.shrine.webclient.client.widgets.QueryTermDragController;
import net.shrine.webclient.client.widgets.WebClientWrapper;

import com.allen_sauer.gwt.dnd.client.PickupDragController;
import com.allen_sauer.gwt.log.client.Log;
import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.event.shared.SimpleEventBus;
import com.google.gwt.user.client.ui.RootPanel;

/**
 * Entry point classes define <code>onModuleLoad()</code>.
 */
public final class Webclient implements EntryPoint {

	private void doOnModuleLoad() {
		final SimpleEventBus eventBus = new SimpleEventBus();
		
		final State state = new State(eventBus);

		final Controllers controllers = new Controllers(state, GWT.<QueryServiceAsync>create(QueryService.class));
		
		final WebClientWrapper wrapper = new WebClientWrapper();

		final OntologySearchBox ontSearchBox = new OntologySearchBox(eventBus, controllers);
		
		final PickupDragController dragController = new QueryTermDragController(RootPanel.get(), false);
		
		configureDragController(dragController);
		
		wrapper.wireUp(eventBus, state, controllers, ontSearchBox, dragController);

		RootPanel.get().add(wrapper);
		
		Log.info("Shrine Webclient loaded");
		
		Log.debug("Base URL: " + GWT.getModuleBaseURL());
	}

	static void configureDragController(final PickupDragController dragController) {
		dragController.setBehaviorConstrainedToBoundaryPanel(true);
	    dragController.setBehaviorMultipleSelection(false);
	    //NB: widgets must be "dragged" at least 5 pixels before dragging is considered to have begun.
	    //This allows widgets to receive click events (for close buttons, etc) without triggering DnD.
	    dragController.setBehaviorDragStartSensitivity(5);
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
