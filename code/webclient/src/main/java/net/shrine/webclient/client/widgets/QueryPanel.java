package net.shrine.webclient.client.widgets;

import net.shrine.webclient.client.controllers.Controllers;
import net.shrine.webclient.client.state.QueryGroup;
import net.shrine.webclient.client.util.Observer;
import net.shrine.webclient.client.util.ReadOnlyObservableList;
import net.shrine.webclient.client.util.Util;

import com.allen_sauer.gwt.dnd.client.PickupDragController;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * 
 * @author clint
 * @date Mar 26, 2012
 */
public final class QueryPanel extends Composite implements Observer {

	private static final QueryColumnUiBinder uiBinder = GWT.create(QueryColumnUiBinder.class);

	interface QueryColumnUiBinder extends UiBinder<Widget, QueryPanel> { }

	@UiField
	CloseButton clearButton;
	
	@UiField
	FlowPanel delegate;

	private ReadOnlyObservableList<QueryGroup> queries;
	
	private Controllers controllers;
	
	private PickupDragController dragController;
	
	public QueryPanel() {
		super();

		initWidget(uiBinder.createAndBindUi(this));
		
		//TODO: feels hackish
		this.delegate.getElement().setId("queryBuilder");
	}
	
	void wireUp(final Controllers controllers, final ReadOnlyObservableList<QueryGroup> queries, final PickupDragController dragController) {
		
		Util.requireNotNull(controllers);
		Util.requireNotNull(queries);
		Util.requireNotNull(dragController);
		
		this.controllers = controllers;
		this.queries = queries;
		this.dragController = dragController;
		
		this.queries.observedBy(this);
		
		clearButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(final ClickEvent event) {
				controllers.queryBuilding.removeAllQueryGroups();
			}
		});
		
		this.inform();
	}
	
	public void inform() {
		clear();
		
		int rowNumber = 0;
		
		for(final QueryGroup query : Util.sorted(queries)) {
			final QueryRow row = new QueryRow(query, controllers, dragController);
			
			addRowStyle(row, rowNumber);
			
			delegate.add(row);
			
			++rowNumber;
		}
		
		delegate.add(new EmptyRow(controllers, dragController));
	}

	static void addRowStyle(final QueryRow row, final int rowNumber) {
		final String cssClass = Util.rowCssClasses.get(rowNumber % Util.rowCssClasses.size());
		
		row.addStyleName(cssClass);
	}

	private void clear() {
		for(final Widget w : delegate) {
			if(w instanceof Disposable) {
				((Disposable)w).dispose();
			}
		}
		
		delegate.clear();
	}

	public void stopObserving() {
		this.queries.noLongerObservedBy(this);
	}
}
