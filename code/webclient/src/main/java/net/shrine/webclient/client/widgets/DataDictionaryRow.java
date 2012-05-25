package net.shrine.webclient.client.widgets;

import java.util.List;

import net.shrine.webclient.client.controllers.Controllers;
import net.shrine.webclient.client.domain.OntNode;
import net.shrine.webclient.client.domain.Term;
import net.shrine.webclient.client.events.CollapseDataDictionaryPanelEvent;
import net.shrine.webclient.client.events.CollapseDataDictionaryPanelEventHandler;
import net.shrine.webclient.client.events.ShowDataDictionaryPanelEvent;
import net.shrine.webclient.client.events.ShowDataDictionaryPanelEventHandler;
import net.shrine.webclient.client.events.VerticalScrollRequestEvent;
import net.shrine.webclient.client.events.VerticalScrollRequestEventHandler;
import net.shrine.webclient.client.services.OntologySearchService;
import net.shrine.webclient.client.services.OntologySearchServiceAsync;

import com.allen_sauer.gwt.log.client.Log;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.ScrollEvent;
import com.google.gwt.event.dom.client.ScrollHandler;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.ScrollPanel;
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
	ScrollPanel dataDictionaryDataHolder;

	private EventBus eventBus;

	private Controllers controllers;

	public static final Term shrineRoot = new Term("\\\\SHRINE\\SHRINE\\", "SHRINE Ontology", "SHRINE Ontology");

	private Term shownTerm = shrineRoot;

	public DataDictionaryRow() {
		initWidget(uiBinder.createAndBindUi(this));

		closeButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(final ClickEvent event) {
				hide();
			}
		});
		
		dataDictionaryDataHolder.addScrollHandler(new ScrollHandler() {
			@Override
			public void onScroll(final ScrollEvent event) {
				Log.debug("Scroll event: " + event.toDebugString());
				Log.debug("Position: " + dataDictionaryDataHolder.getVerticalScrollPosition());
				
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
				if (shouldRedraw(event)) {
					shownTerm = event.getStartingTerm() == null ? shrineRoot : event.getStartingTerm();

					loadOntTree(controllers, shownTerm);

					show();
				}
			}

			boolean shouldRedraw(final ShowDataDictionaryPanelEvent event) {
				return !isSameTerm(event);
			}

			boolean isSameTerm(final ShowDataDictionaryPanelEvent event) {
				return shownTerm.equals(event.getStartingTerm());
			}
		});

		eventBus.addHandler(CollapseDataDictionaryPanelEvent.getType(), new CollapseDataDictionaryPanelEventHandler() {
			@Override
			public void handle(final CollapseDataDictionaryPanelEvent event) {
				hide();
			}
		});
		
		eventBus.addHandler(VerticalScrollRequestEvent.getType(), new VerticalScrollRequestEventHandler() {
			@Override
			public void handle(final VerticalScrollRequestEvent event) {
				Log.debug("Scrolling"); 
				
				dataDictionaryDataHolder.ensureVisible(event.getWidgetToScrollTo());
			}
		});
	}

	OntologyTree makeTree(final List<OntNode> pathFromRoot) {
		return new OntologyTree(eventBus, controllers, pathFromRoot);
	}

	void toggleDataDictionaryDisplay() {
		if (!dataDictionaryIsShowing()) {
			if (isWiredUp()) {
				loadOntTree(controllers, shownTerm);
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

	void showDataDictionaryTree(final Controllers controllers, final List<OntNode> ontNodes) {
		dataDictionaryDataHolder.setWidget(new DataDictionaryPanel(makeTree(ontNodes)));
	}

	void hideDataDictionaryTree() {
		dataDictionaryDataHolder.clear();
	}

	void loadOntTree(final Controllers controllers, final Term startingTerm) {
		final OntologySearchServiceAsync ontologyService = GWT.create(OntologySearchService.class);

		ontologyService.getPathTo(startingTerm.getPath(), new AsyncCallback<List<OntNode>>() {
			@Override
			public void onSuccess(final List<OntNode> pathFromRoot) {
				if (!pathFromRoot.isEmpty()) {
					showDataDictionaryTree(controllers, pathFromRoot);
				} else {
					Log.error("No results after attempting to load ontology tree rooted at term '" + startingTerm.getPath() + "'");
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
