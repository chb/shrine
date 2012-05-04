package net.shrine.webclient.client.widgets;

import com.allen_sauer.gwt.dnd.client.PickupDragController;
import com.google.gwt.user.client.ui.AbsolutePanel;

/**
 * 
 * @author clint
 * @date May 4, 2012
 */
public final class QueryTermDragController extends PickupDragController {
	public QueryTermDragController(AbsolutePanel boundaryPanel, boolean allowDroppingOnBoundaryPanel) {
		super(boundaryPanel, allowDroppingOnBoundaryPanel);
	}

	@Override
	protected void restoreSelectedWidgetsLocation() {

	}

	@Override
	protected void restoreSelectedWidgetsStyle() {
		
	}

	@Override
	protected void saveSelectedWidgetsLocationAndStyle() {
		
	}
}