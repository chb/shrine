package net.shrine.webclient.client.widgets;

import java.util.Collection;
import java.util.Date;
import java.util.Iterator;

import net.shrine.webclient.client.Controllers;
import net.shrine.webclient.client.domain.Expression;
import net.shrine.webclient.client.domain.Or;
import net.shrine.webclient.client.domain.ReadOnlyQueryGroup;
import net.shrine.webclient.client.domain.Term;
import net.shrine.webclient.client.util.Observer;
import net.shrine.webclient.client.util.Util;

import com.allen_sauer.gwt.dnd.client.PickupDragController;
import com.allen_sauer.gwt.dnd.client.drop.SimpleDropController;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
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
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.user.datepicker.client.DateBox;

/**
 * 
 * @author clint
 * @date Mar 26, 2012
 */
public final class QueryRow extends Composite implements Observer, Iterable<Widget>, Disposable {

	private static final QueryRowUiBinder uiBinder = GWT.create(QueryRowUiBinder.class);

	interface QueryRowUiBinder extends UiBinder<Widget, QueryRow> { }

	private final Controllers controllers;

	private final ReadOnlyQueryGroup query;
	
	private final PickupDragController dragController;

	private final SimpleDropController dropController;
	
	@UiField
	Label nameLabel;

	@UiField
	HTMLPanel exprPanel;

	@UiField
	CloseButton clearButton;
	
	@UiField
	DateBox startDate;

	@UiField
	DateBox endDate;

	@UiField
	Spinner minOccursSpinner;

	@UiField
	CheckBox negationCheckbox;

	QueryRow(final Controllers controllers, final ReadOnlyQueryGroup query, final PickupDragController dragController) {
		super();

		Util.requireNotNull(controllers);
		Util.requireNotNull(query);
		Util.requireNotNull(query.getId());
		Util.requireNotNull(query.getExpression());
		Util.requireNotNull(dragController);

		initWidget(uiBinder.createAndBindUi(this));
		
		this.controllers = controllers;
		this.query = query;
		this.dragController = dragController;
		this.dropController = QueryRowDropController.from(this, this, controllers, query);
		
		this.dragController.registerDropController(dropController);
		
		this.query.observedBy(this);
		
		initClearButton();
		
		initNegationCheckbox();

		initStartDateBox();

		initEndDateBox();

		initMinOccursSpinner();

		inform();
	}
	
	@Override
	public Iterator<Widget> iterator() {
		return exprPanel.iterator();
	}

	private void initClearButton() {
		clearButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(final ClickEvent event) {
				controllers.queryBuilding.removeQueryGroup(query.getId());
			}
		});
	}

	private void initMinOccursSpinner() {
		this.minOccursSpinner.setMin(1);
		this.minOccursSpinner.setValue(query.getMinOccurances());
		this.minOccursSpinner.addSpinnerHandler(new Spinner.SpinnerHandler() {
			@Override
			public void onValueChange(final int value) {
				controllers.constraints.setMinOccurs(query.getId(), value);
			}
		});
	}

	public static final DateBox.Format dateFormat = new DateBox.DefaultFormat(DateTimeFormat.getFormat(PredefinedFormat.DATE_SHORT));

	private void initStartDateBox() {
		this.startDate.setFormat(dateFormat);

		startDate.setValue(query.getStart());

		this.startDate.addValueChangeHandler(new ValueChangeHandler<Date>() {
			@Override
			public void onValueChange(ValueChangeEvent<Date> event) {
				controllers.constraints.setStartDate(query.getId(), event.getValue());
			}
		});
	}

	private void initEndDateBox() {
		this.endDate.setFormat(dateFormat);

		endDate.setValue(query.getEnd());

		this.endDate.addValueChangeHandler(new ValueChangeHandler<Date>() {
			@Override
			public void onValueChange(ValueChangeEvent<Date> event) {
				controllers.constraints.setEndDate(query.getId(), event.getValue());
			}
		});
	}

	private void initNegationCheckbox() {
		negationCheckbox.setValue(query.isNegated());

		negationCheckbox.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
			@Override
			public void onValueChange(final ValueChangeEvent<Boolean> event) {
				controllers.constraints.setNegated(query.getId(), event.getValue());
			}
		});
	}

	@Override
	public void inform() {
		nameLabel.setText(query.getId().name);

		refreshExpressionPanel();

		negationCheckbox.setValue(query.isNegated(), false);

		startDate.setValue(query.getStart(), false);

		endDate.setValue(query.getEnd(), false);

		minOccursSpinner.setValue(query.getMinOccurances(), false);
	}

	private void refreshExpressionPanel() {
		exprPanel.clear();

		for (final QueryTerm queryTerm : makeQueryTermsFrom(query.getExpression())) {
			exprPanel.add(queryTerm);
			
			dragController.makeDraggable(queryTerm);
		}
	}

	// NB: exposed For tests
	Collection<QueryTerm> makeQueryTermsFrom(final Expression expr) {
		Util.require(expr instanceof Term || expr instanceof Or, "Only Or-expressions and single terms can be turned into lists of QueryTerms");

		final Collection<QueryTerm> result = Util.makeArrayList();

		for (final Term term : expr.getTerms()) {
			result.add(new QueryTerm(query.getId(), controllers.queryBuilding, term));
		}

		return result;
	}

	@Override
	public void dispose() {
		stopObserving();
		
		dragController.unregisterDropController(dropController);
	}
	
	@Override
	public void stopObserving() {
		this.query.noLongerObservedBy(this);
	}
}