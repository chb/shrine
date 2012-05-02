package net.shrine.webclient.client;

import net.shrine.webclient.client.widgets.OntologySearchBox;
import net.shrine.webclient.client.widgets.WebClientWrapper;

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
		
		final State state = new State();

		final Controllers controllers = new Controllers(state, GWT.<QueryServiceAsync>create(QueryService.class));
		
		final WebClientWrapper wrapper = new WebClientWrapper();

		final OntologySearchBox ontSearchBox = new OntologySearchBox(eventBus, controllers);
		
		wrapper.wireUp(eventBus, state, controllers, ontSearchBox);

		RootPanel.get().add(wrapper);
		
		Log.info("Shrine Webclient loaded");
		
		Log.debug("Base URL: " + GWT.getModuleBaseURL());
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
