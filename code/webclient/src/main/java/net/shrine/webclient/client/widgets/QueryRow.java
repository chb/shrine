package net.shrine.webclient.client.widgets;

import java.util.Collection;
import java.util.Date;
import java.util.HashMap;

import net.shrine.webclient.client.Controllers;
import net.shrine.webclient.client.domain.Expression;
import net.shrine.webclient.client.domain.IntWrapper;
import net.shrine.webclient.client.domain.Or;
import net.shrine.webclient.client.domain.ReadOnlyQueryGroup;
import net.shrine.webclient.client.domain.Term;
import net.shrine.webclient.client.util.Observer;
import net.shrine.webclient.client.util.ReadOnlyObservable;
import net.shrine.webclient.client.util.Util;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.DateTimeFormat.PredefinedFormat;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.user.datepicker.client.DateBox;

/**
 * 
 * @author clint
 * @date Mar 26, 2012
 */
public final class QueryRow extends Composite implements Observer {

	private static final QueryRowUiBinder uiBinder = GWT.create(QueryRowUiBinder.class);

	interface QueryRowUiBinder extends UiBinder<Widget, QueryRow> { }

	private final String queryName;

	private final Expression expr;

	private final ReadOnlyObservable<HashMap<String, IntWrapper>> result;

	private final Controllers controllers;
	
	private final ReadOnlyQueryGroup query;
	
	@UiField
	Label nameLabel;

	@UiField
	HTMLPanel exprPanel;

	@UiField
	SimplePanel resultPanel;
	
	@UiField
	DateBox startDate;
	
	@UiField
	DateBox endDate;
	
	@UiField
	Spinner minOccursSpinner;
	
	@UiField
	CheckBox negationCheckbox;
	
	QueryRow(final Controllers controllers, final String queryName, final ReadOnlyQueryGroup query) {
		super();

		Util.requireNotNull(controllers);
		Util.requireNotNull(queryName);
		Util.requireNotNull(query);
		Util.requireNotNull(query.getExpression());
		Util.requireNotNull(query.getResult());

		this.controllers = controllers;
		this.queryName = queryName;
		this.query = query;
		this.expr = query.getExpression();
		this.result = query.getResult();

		initWidget(uiBinder.createAndBindUi(this));
		
		this.result.observedBy(this);
		
		initNegationCheckbox();
		
		initStartDateBox();
		
		initEndDateBox();
		
		initMinOccursSpinner();
		
		inform();
		
		resultPanel.clear();
	}

	private void initMinOccursSpinner() {
		this.minOccursSpinner.setMin(1);
		this.minOccursSpinner.setValue(1);
		this.minOccursSpinner.addSpinnerHandler(new Spinner.SpinnerHandler() {
			@Override
			public void onValueChange(final int value) {
				controllers.constraints.setMinOccurs(queryName, value);
			}
		});
	}

	public static final DateBox.Format dateFormat = new DateBox.DefaultFormat(DateTimeFormat.getFormat(PredefinedFormat.DATE_SHORT));
	
	private void initStartDateBox() {
		this.startDate.setFormat(dateFormat);
		
		this.startDate.addValueChangeHandler(new ValueChangeHandler<Date>() {
			@Override
			public void onValueChange(ValueChangeEvent<Date> event) {
				controllers.constraints.setStartDate(queryName, event.getValue());
			}
		});
	}

	private void initEndDateBox() {
		this.endDate.setFormat(dateFormat);
		
		this.endDate.addValueChangeHandler(new ValueChangeHandler<Date>() {
			@Override
			public void onValueChange(ValueChangeEvent<Date> event) {
				controllers.constraints.setEndDate(queryName, event.getValue());
			}
		});
	}

	private void initNegationCheckbox() {
		negationCheckbox.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
			@Override
			public void onValueChange(final ValueChangeEvent<Boolean> event) {
				controllers.constraints.setNegated(queryName, event.getValue());
			}
		});
	}
	
	@Override
	public void inform() {
		nameLabel.setText(queryName);
		
		refreshExpressionPanel();
		
		refreshResultPanel();
		
		negationCheckbox.setValue(query.isNegated(), false);
		
		startDate.setValue(query.getStart(), false);
		
		endDate.setValue(query.getEnd(), false);
		
		minOccursSpinner.setValue(query.getMinOccurances(), false);
	}

	private void refreshResultPanel() {
		final Widget resultWidget;
		
		if(result.isDefined()) {
			resultWidget = new HoverableResultLink(result.get());
		} else {
			resultWidget = new LoadingSpinner();
		}
		
		resultPanel.setWidget(resultWidget);
	}

	private void refreshExpressionPanel() {
		exprPanel.clear();
		
		for(final QueryTerm queryTerm : makeQueryTermsFrom(expr)) {
			exprPanel.add(queryTerm);
		}
	}

	@Override
	public void stopObserving() {
		result.noLongerObservedBy(this);
	}
	
	//NB: exposed For tests
	static Collection<QueryTerm> makeQueryTermsFrom(final Expression expr) {
		if(expr instanceof Term || expr instanceof Or) {
			final Collection<QueryTerm> result = Util.makeArrayList();
			
			for(final Term term : expr.getTerms()) {
				result.add(new QueryTerm(term));
			}
			
			return result;
		} else {
			throw new IllegalArgumentException("Only Or-expressions and single terms can be turned into lists of QueryTerms");
		}
	}
}