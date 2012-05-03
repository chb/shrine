package net.shrine.webclient.client.widgets;

import java.util.List;

import net.shrine.webclient.client.Controllers;
import net.shrine.webclient.client.OntologySearchService;
import net.shrine.webclient.client.OntologySearchServiceAsync;
import net.shrine.webclient.client.domain.OntNode;
import net.shrine.webclient.client.domain.Term;
import net.shrine.webclient.client.events.CollapseDataDictionaryPanelEvent;
import net.shrine.webclient.client.events.CollapseDataDictionaryPanelEventHandler;
import net.shrine.webclient.client.events.ShowDataDictionaryPanelEvent;
import net.shrine.webclient.client.events.ShowDataDictionaryPanelEventHandler;

import com.allen_sauer.gwt.log.client.Log;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * 
 * @author clint
 * @date Apr 25, 2012
 */
public final class DataDictionaryRow extends Composite {

	private static DataDictionaryRowUiBinder uiBinder = GWT.create(DataDictionaryRowUiBinder.class);

	interface DataDictionaryRowUiBinder extends UiBinder<Widget, DataDictionaryRow> {
	}

	@UiField
	CloseButton closeButton;

	@UiField
	SimplePanel dataDictionaryDataHolder;

	private EventBus eventBus;

	private Controllers controllers;

	private static final Term shrineRoot = new Term("\\\\SHRINE\\SHRINE\\", "SHRINE Ontology");

	private Term rootTerm = new Term("\\\\SHRINE\\SHRINE\\", "SHRINE Ontology");

	public DataDictionaryRow() {
		initWidget(uiBinder.createAndBindUi(this));

		closeButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(final ClickEvent event) {
				hide();
			}
		});
		
		hide();
	}

	public void wireUp(final EventBus eventBus, final Controllers controllers) {
		this.eventBus = eventBus;
		this.controllers = controllers;

		eventBus.addHandler(ShowDataDictionaryPanelEvent.getType(), new ShowDataDictionaryPanelEventHandler() {
			@Override
			public void handle(final ShowDataDictionaryPanelEvent event) {
				if (dataDictionaryIsShowing()) {
					hideDataDictionaryTree();

					hide();
				} else {
					rootTerm = event.getStartingTerm() == null ? shrineRoot : event.getStartingTerm();

					loadOntTree(controllers, rootTerm);
					
					show();
				}
			}
		});

		eventBus.addHandler(CollapseDataDictionaryPanelEvent.getType(), new CollapseDataDictionaryPanelEventHandler() {
			@Override
			public void handle(final CollapseDataDictionaryPanelEvent event) {
				hide();
			}
		});
	}

	OntologyTree makeTree(final OntNode rootOntNode) {
		return new OntologyTree(eventBus, controllers, rootOntNode);
	}

	void toggleDataDictionaryDisplay() {
		if (!dataDictionaryIsShowing()) {
			if (isWiredUp()) {
				loadOntTree(controllers, rootTerm);
			}
		} else {
			hideDataDictionaryTree();
		}
	}

	boolean isWiredUp() {
		return controllers != null && eventBus != null;
	}

	boolean dataDictionaryIsShowing() {
		return dataDictionaryDataHolder.getWidget() != null;
	}

	void showDataDictionaryTree(final Controllers controllers, final OntNode ontTree) {
		dataDictionaryDataHolder.setWidget(new DataDictionaryPanel(makeTree(ontTree)));
	}

	void hideDataDictionaryTree() {
		dataDictionaryDataHolder.clear();
	}

	void loadOntTree(final Controllers controllers, final Term startingTerm) {
		final OntologySearchServiceAsync ontologyService = GWT.create(OntologySearchService.class);

		ontologyService.getTreeRootedAt(startingTerm.value, new AsyncCallback<List<OntNode>>() {
			@Override
			public void onSuccess(final List<OntNode> result) {
				if (!result.isEmpty()) {
					showDataDictionaryTree(controllers, result.get(0));
				} else {
					Log.error("No results after attempting to load ontology tree rooted at term '" + startingTerm.value + "'");
				}
			}

			@Override
			public void onFailure(final Throwable caught) {
				Log.error("Failed to browse ontology: " + caught.getMessage(), caught);

				hideDataDictionaryTree();
			}
		});
	}

	void hide() {
		hideDataDictionaryTree();
		
		setVisible(false);
	}
	
	void show() {
		setVisible(true);
	}
}
