package net.shrine.webclient.client.widgets;

import com.google.gwt.user.client.ui.Image;

/**
 * 
 * @author clint
 * @date Mar 26, 2012
 */
public final class LoadingSpinner extends Image {
	public LoadingSpinner() {
		super("/images/spinner3-greenie.gif");
		
		this.setAltText("loading");
		this.setWidth("20px");
		this.setHeight("20px");
	}
}
