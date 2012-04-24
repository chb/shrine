package net.shrine.webclient.client.widgets;

import com.google.gwt.user.client.ui.Image;

/**
 * 
 * @author clint
 * @date Apr 24, 2012
 */
public final class DeleteButton extends Image {
	public DeleteButton() {
		super("/images/delete.png");
		
		this.setAltText("Delete");
		
		this.setStyleName("loadDelete");
		
		this.setHeight("16px");
		this.setWidth("16px");
	}
}
