package net.shrine.webclient.client.widgets;

import net.shrine.webclient.client.util.ReadOnlyObservable;

import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

/**
 * 
 * @author clint
 * @date Mar 27, 2012
 */
public final class WidgetUtil {
	private WidgetUtil() {
		super();
	}
	
	public static String textFor(final ReadOnlyObservable<Integer> handle) {
		final Integer value = handle.get();
		
		if(value < 0) {
			return "< 10";
		}
		
		return String.valueOf(value);
	}
	
	public static Label labelFor(final ReadOnlyObservable<Integer> handle) {
		return new Label(textFor(handle));
	}
	
	public static Widget toLabelOrSpinner(final ReadOnlyObservable<Integer> handle) {
		if(handle.isDefined()) {
			return labelFor(handle); 
		}
		
		return new LoadingSpinner();
	}
}
