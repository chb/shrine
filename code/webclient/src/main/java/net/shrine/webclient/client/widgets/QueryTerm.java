package net.shrine.webclient.client.widgets;

import net.shrine.webclient.client.controllers.QueryBuildingController;
import net.shrine.webclient.client.util.Util;
import net.shrine.webclient.shared.domain.Term;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.SpanElement;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasAllMouseHandlers;
import com.google.gwt.event.dom.client.MouseDownEvent;
import com.google.gwt.event.dom.client.MouseDownHandler;
import com.google.gwt.event.dom.client.MouseMoveEvent;
import com.google.gwt.event.dom.client.MouseMoveHandler;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOutHandler;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.event.dom.client.MouseOverHandler;
import com.google.gwt.event.dom.client.MouseUpEvent;
import com.google.gwt.event.dom.client.MouseUpHandler;
import com.google.gwt.event.dom.client.MouseWheelEvent;
import com.google.gwt.event.dom.client.MouseWheelHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;

/**
 * 
 * @author clint
 * @date Mar 26, 2012
 * 
 *       NB: Must implement HasAllMouseHandlers to be draggable
 */
public final class QueryTerm extends Composite implements HasAllMouseHandlers {

	private static final QueryTermUiBinder uiBinder = GWT.create(QueryTermUiBinder.class);

	interface QueryTermUiBinder extends UiBinder<Widget, QueryTerm> {
	}

	@UiField
	SpanElement termSpan;

	@UiField
	CloseButton closeButton;

	private final Term term;
	
	private final int queryId;

	public QueryTerm(final int queryId, final QueryBuildingController controller, final Term term) {
		Util.requireNotNull(controller);
		Util.requireNotNull(term);

		initWidget(uiBinder.createAndBindUi(this));

		this.term = term;
		this.queryId = queryId;

		termSpan.setInnerText(term.getSimpleName());

		setTitle(term.getPath());

		closeButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(final ClickEvent event) {
				controller.removeTerm(queryId, term);
			}
		});
	}

	Term getTerm() {
		return term;
	}

	int getQueryId() {
		return queryId;
	}

	@Override
	public String toString() {
		return "QueryTerm(" + term + ")";
	}

	// Begin stubs for HasAllMouseHandlers
	public HandlerRegistration addClickHandler(final ClickHandler handler) {
		return addDomHandler(handler, ClickEvent.getType());
	}

	@Override
	public HandlerRegistration addMouseDownHandler(final MouseDownHandler handler) {
		return addDomHandler(handler, MouseDownEvent.getType());
	}

	@Override
	public HandlerRegistration addMouseMoveHandler(final MouseMoveHandler handler) {
		return addDomHandler(handler, MouseMoveEvent.getType());
	}

	@Override
	public HandlerRegistration addMouseOutHandler(final MouseOutHandler handler) {
		return addDomHandler(handler, MouseOutEvent.getType());
	}

	@Override
	public HandlerRegistration addMouseOverHandler(final MouseOverHandler handler) {
		return addDomHandler(handler, MouseOverEvent.getType());
	}

	@Override
	public HandlerRegistration addMouseUpHandler(final MouseUpHandler handler) {
		return addDomHandler(handler, MouseUpEvent.getType());
	}

	@Override
	public HandlerRegistration addMouseWheelHandler(final MouseWheelHandler handler) {
		return addDomHandler(handler, MouseWheelEvent.getType());
	}
	// End stubs for HasAllMouseHandlers

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (term == null ? 0 : term.hashCode());
		return result;
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final QueryTerm other = (QueryTerm) obj;
		if (term == null) {
			if (other.term != null) {
				return false;
			}
		} else if (!term.equals(other.term)) {
			return false;
		}
		return true;
	}
}
