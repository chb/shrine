package net.shrine.webclient.client.widgets;

import net.shrine.webclient.client.util.Util;

import com.allen_sauer.gwt.dnd.client.PickupDragController;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.user.client.ui.Widget;

/**
 * 
 * @author clint
 * @date May 4, 2012
 * 
 * NB: non-final for mockability 
 */
public class QueryTermDragController extends PickupDragController {
	public QueryTermDragController(final AbsolutePanel boundaryPanel, boolean allowDroppingOnBoundaryPanel) {
		super(boundaryPanel, allowDroppingOnBoundaryPanel);
	}

	private HasWidgets draggedWidgetContainer = null;
	
	@Override
	protected void restoreSelectedWidgetsLocation() {
		final Widget dragged = context.selectedWidgets.get(0);

		Util.requireNotNull(draggedWidgetContainer);
		
		draggedWidgetContainer.remove(dragged);
		
		draggedWidgetContainer.add(dragged);
	}

	@Override
	protected void restoreSelectedWidgetsStyle() {
		//NOOP, no style to reset
	}

	@Override
	protected void saveSelectedWidgetsLocationAndStyle() {
		final Widget dragged = context.selectedWidgets.get(0);
		
		draggedWidgetContainer = (HasWidgets)dragged.getParent();
	}
}