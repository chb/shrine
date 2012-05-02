package net.shrine.webclient.client.widgets;

import net.shrine.webclient.client.Controllers;
import net.shrine.webclient.client.domain.QueryGroup;
import net.shrine.webclient.client.util.Observer;
import net.shrine.webclient.client.util.ReadOnlyObservableList;
import net.shrine.webclient.client.util.Util;

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
public final class QueryColumn extends Composite implements Observer {

	private static final QueryColumnUiBinder uiBinder = GWT.create(QueryColumnUiBinder.class);

	interface QueryColumnUiBinder extends UiBinder<Widget, QueryColumn> { }

	@UiField
	CloseButton clearButton;
	
	@UiField
	FlowPanel delegate;

	private ReadOnlyObservableList<QueryGroup> queries;
	
	private Controllers controllers;
	
	public QueryColumn() {
		super();

		initWidget(uiBinder.createAndBindUi(this));
		
		//TODO: feels hackish
		this.delegate.getElement().setId("queryBuilder");
	}
	
	void wireUp(final Controllers controllers, final ReadOnlyObservableList<QueryGroup> queries) {
		
		Util.requireNotNull(controllers);
		Util.requireNotNull(queries);
		
		this.controllers = controllers;
		this.queries = queries;
		
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
		
		//Make sure queries are named A,B,C,... Z, in that order, with no gaps, always starting from 'A'
		controllers.queryBuilding.reNameQueries();
		
		for(final QueryGroup query : Util.sorted(queries)) {
			delegate.add(new QueryRow(controllers, query));
		}
		
		delegate.add(new EmptyRow());
	}

	private void clear() {
		for(final Widget w : delegate) {
			if(w instanceof QueryRow) {
				((QueryRow)w).stopObserving();
			}
		}
		
		delegate.clear();
	}

	public void stopObserving() {
		this.queries.noLongerObservedBy(this);
	}
}
