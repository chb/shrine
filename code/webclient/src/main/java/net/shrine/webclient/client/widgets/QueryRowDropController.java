package net.shrine.webclient.client.widgets;

import net.shrine.webclient.client.controllers.Controllers;
import net.shrine.webclient.client.state.QueryGroup;
import net.shrine.webclient.client.state.ReadOnlyQueryGroup;
import net.shrine.webclient.client.util.Util;
import net.shrine.webclient.shared.domain.Term;

import com.allen_sauer.gwt.dnd.client.DragContext;
import com.allen_sauer.gwt.dnd.client.VetoDragException;
import com.allen_sauer.gwt.dnd.client.drop.SimpleDropController;
import com.allen_sauer.gwt.log.client.Log;
import com.google.gwt.user.client.ui.Widget;

/**
 * 
 * @author clint
 * @date May 4, 2012
 */
public final class QueryRowDropController<T extends Widget & Iterable<Widget>> extends SimpleDropController {
	private final T enclosing;

	private final Controllers controllers;

	private final ReadOnlyQueryGroup query;

	QueryRowDropController(final T enclosing, final Widget dropTarget, final Controllers controllers, final ReadOnlyQueryGroup query) {
		super(dropTarget);
		
		this.enclosing = enclosing;
		this.controllers = controllers;
		this.query = query;
	}
	
	//
	//Type-inferring convenience method
	static <T extends Widget & Iterable<Widget>> QueryRowDropController<T> from(final T enclosing, final Widget dropTarget, final Controllers controllers, final ReadOnlyQueryGroup query) {
		return new QueryRowDropController<T>(enclosing, dropTarget, controllers, query);
	}

	@Override
	public void onDrop(final DragContext context) {
		Log.debug("Dropped a " + context.draggable + " on row " + getQueryGroupId());

		final QueryTerm draggedQueryTerm = (QueryTerm) context.draggable;

		final Term draggedTerm = draggedQueryTerm.getTerm();
		
		final int fromQueryId = draggedQueryTerm.getQueryId();
		
		final int toQueryId = shouldMoveTerm() ? query.getId() : QueryGroup.NullId;
		
		controllers.queryBuilding.moveTerm(draggedTerm, fromQueryId, toQueryId);
	}

	boolean shouldMoveTerm() {
		return query != null;
	}

	int getQueryGroupId() {
		return query == null ? QueryGroup.NullId : query.getId();
	}

	@Override
	public void onEnter(final DragContext context) {
		Log.debug("A " + context.draggable + " entered row " + getQueryGroupId());

		enclosing.addStyleName("queryRowDropTargetHighlighted");
	}

	@Override
	public void onLeave(final DragContext context) {
		Log.debug("A " + context.draggable + " left row " + getQueryGroupId());

		enclosing.removeStyleName("queryRowDropTargetHighlighted");
	}

	@Override
	public void onPreviewDrop(final DragContext context) throws VetoDragException {
		if (Util.toList(enclosing).contains(context.draggable)) {
			throw new VetoDragException();
		}
	}
}