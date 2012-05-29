package net.shrine.webclient.client.widgets;

import static java.util.Arrays.asList;
import net.shrine.webclient.client.domain.TermSuggestion;
import net.shrine.webclient.client.util.Util;
import net.shrine.webclient.client.widgets.suggest.RichSuggestionEvent;
import net.shrine.webclient.client.widgets.suggest.SuggestRowContainer;
import net.shrine.webclient.client.widgets.suggest.WidgetMaker;

import com.allen_sauer.gwt.log.client.Log;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.ImageElement;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.regexp.shared.RegExp;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

/**
 * 
 * @author clint
 * @date Mar 30, 2012
 * 
 *       NB: many methods exposed with default access for tests
 */
public final class AutoSuggestRow extends Composite {

	private static AutoSuggestRowUiBinder uiBinder = GWT.create(AutoSuggestRowUiBinder.class);

	interface AutoSuggestRowUiBinder extends UiBinder<Widget, AutoSuggestRow> { }

	@UiField
	Anchor browseLink;
	
	@UiField
	ImageElement iconImage;

	@UiField
	Label simpleName;

	@UiField
	Label synonym;

	public AutoSuggestRow(final EventBus eventBus, final SuggestRowContainer<TermSuggestion> container, final TermSuggestion termSuggestion) {
		initWidget(uiBinder.createAndBindUi(this));

		Util.requireNotNull(container);
		Util.requireNotNull(termSuggestion);
		
		initIconImage(termSuggestion);

		final RegExp replaceHighlightRegex = RegExp.compile("(" + termSuggestion.getHighlight() + ")", "ig");

		initSimpleNameSpan(termSuggestion, replaceHighlightRegex);

		initSynonymSpan(termSuggestion, replaceHighlightRegex);

		// Make full path appear when mouse hovers
		this.setTitle(termSuggestion.getPath());

		initClickHandlers(container, termSuggestion);

		browseLink.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(final ClickEvent event) {
				container.hidePopup();
				
				container.clearTextBox();
				
				eventBus.fireEvent(new ShowDataDictionaryPanelEvent(termSuggestion.toTerm()));
			}
		});
	}

	void initIconImage(final TermSuggestion termSuggestion) {
		iconImage.setAlt("tree");
		iconImage.setSrc("images/" + (termSuggestion.isLeaf() ? "document.png" : "folder.gif"));
	}

	public static WidgetMaker<TermSuggestion> autoSuggestRowWidgetMaker(final EventBus eventBus, final SuggestRowContainer<TermSuggestion> suggestionEventSink) {
		return new WidgetMaker<TermSuggestion>() {
			@Override
			public Widget makeWidget(final TermSuggestion termSuggestion) {
				return new AutoSuggestRow(eventBus, suggestionEventSink, termSuggestion);
			}
		};
	}

	void initClickHandlers(final SuggestRowContainer<TermSuggestion> suggestionEventSink, final TermSuggestion termSuggestion) {
		final ClickHandler clickHandler = makeHandlerThatFiresSuggestionEvent(suggestionEventSink, termSuggestion);

		for (final Label clickable : asList(simpleName, synonym)) {
			clickable.addClickHandler(clickHandler);
		}
	}

	ClickHandler makeHandlerThatFiresSuggestionEvent(final SuggestRowContainer<TermSuggestion> eventSink, final TermSuggestion termSuggestion) {
		return new ClickHandler() {
			@Override
			public void onClick(final ClickEvent event) {
				Log.debug("Term Suggestion clicked: '" + termSuggestion.getPath() + "'");

				fireSuggestionEvent(eventSink, RichSuggestionEvent.from(termSuggestion));
			}
		};
	}

	void initSimpleNameSpan(final TermSuggestion termSuggestion, final RegExp replaceHighlightRegex) {
		simpleName.getElement().setInnerHTML(makeHighlightedText(termSuggestion.getSimpleName().trim(), replaceHighlightRegex));
	}

	void initSynonymSpan(final TermSuggestion termSuggestion, final RegExp replaceHighlightRegex) {
		final String synonymText = termSuggestion.getSynonym();

		final boolean shouldShowSynonym = synonymText != null && !synonymText.equalsIgnoreCase(termSuggestion.getSimpleName());

		this.synonym.setVisible(shouldShowSynonym);

		if (shouldShowSynonym) {
			final String wrapppedSynonymText = synonymText != null ? ("(synonym: " + synonymText + ")") : "";

			this.synonym.getElement().setInnerHTML(makeHighlightedText(wrapppedSynonymText, replaceHighlightRegex));
		}
	}

	String makeHighlightedText(final String original, final RegExp highlightRegex) {
		return highlightRegex.replace(original, "<strong>$1</strong>");
	}

	void fireSuggestionEvent(final SuggestRowContainer<TermSuggestion> eventSink, final RichSuggestionEvent<TermSuggestion> suggestionEvent) {
		Log.debug("Firing suggestion event: " + suggestionEvent);

		eventSink.fireSuggestionEvent(suggestionEvent);
	}
}
