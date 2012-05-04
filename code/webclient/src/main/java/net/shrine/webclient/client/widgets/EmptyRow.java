package net.shrine.webclient.client.widgets;

import java.util.Collections;
import java.util.Iterator;

import net.shrine.webclient.client.Controllers;
import net.shrine.webclient.client.util.Util;

import com.allen_sauer.gwt.dnd.client.PickupDragController;
import com.allen_sauer.gwt.dnd.client.drop.SimpleDropController;
import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * 
 * @author clint
 * @date Mar 30, 2012
 */
public final class EmptyRow extends Composite implements Iterable<Widget>, Disposable {

	private static EmptyRowUiBinder uiBinder = GWT.create(EmptyRowUiBinder.class);
    
	interface EmptyRowUiBinder extends UiBinder<Widget, EmptyRow> { }

	@UiField
	HTMLPanel delegate;
	
	private final PickupDragController dragController;
	
	private final SimpleDropController dropController;
	
	public EmptyRow(final Controllers controllers, final PickupDragController dragController) {
		initWidget(uiBinder.createAndBindUi(this));
		
		Util.requireNotNull(controllers);
		Util.requireNotNull(dragController);
		
		this.dragController = dragController;
		
		this.dropController = new QueryRowDropController<EmptyRow>(this, this, controllers, null);
		
		dragController.registerDropController(dropController);
	}

	@Override
	public void dispose() {
		dragController.unregisterDropController(dropController);
	}
	
	@Override
	public Iterator<Widget> iterator() {
		return Collections.<Widget>emptyList().iterator();
	}
}
