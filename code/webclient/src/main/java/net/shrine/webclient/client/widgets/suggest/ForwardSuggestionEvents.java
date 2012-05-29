package net.shrine.webclient.client.widgets.suggest;

import com.allen_sauer.gwt.log.client.Log;
import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * 
 * @author clint
 * @date Apr 11, 2012
 */
public final class ForwardSuggestionEvents {
	private ForwardSuggestionEvents() {
		super();
	}
	
	public static <S extends IsSerializable> RichSuggestionEventHandler<S> to(final SuggestRowContainer<S> eventSink) {
		return new RichSuggestionEventHandler<S>() {
			@Override
			public void onSelectionMade(final RichSuggestionEvent<S> event) {
				Log.debug("Forwarding event");
					
				eventSink.fireSuggestionEvent(event);
			}
		};
	}
}
