package net.shrine.webclient.client.widgets;

import com.google.gwt.user.client.ui.Image;

/**
 * 
 * @author clint
 * @date Mar 27, 2012
 */
public final class ShrineLogo extends Image {
	public ShrineLogo() {
		super("/images/logo.png");
		
		this.setAltText("Shrine");
		this.setWidth("187px");
		this.setHeight("72px");
	}
}
