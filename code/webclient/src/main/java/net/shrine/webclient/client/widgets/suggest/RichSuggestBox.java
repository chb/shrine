package net.shrine.webclient.client.widgets.suggest;

import net.shrine.webclient.client.util.Observable;
import net.shrine.webclient.client.util.Util;

import com.allen_sauer.gwt.log.client.Log;
import com.google.gwt.event.dom.client.FocusEvent;
import com.google.gwt.event.dom.client.FocusHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOutHandler;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.event.dom.client.MouseOverHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.rpc.IsSerializable;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

/**
 * 
 * @author clint
 * @date Apr 5, 2012
 */
public class RichSuggestBox<S extends IsSerializable> extends Composite implements SuggestRowContainer<S>, HasText {
	private final TextBox textBox = new TextBox();

	private RichSuggestOracle<S> oracle;

	private WidgetMaker<S> widgetMaker;

	private final PopupPanel suggestionPopup = new PopupPanel(true, false);

	private SuggestionsPanel suggestionsPanel;

	private final Observable<Integer> highlightedPopupRow = Observable.empty();

	// TODO: make this a param
	private final int maxSuggestions;

	public RichSuggestBox(final RichSuggestOracle<S> oracle, final WidgetMaker<S> widgetMaker) {
		this(oracle, widgetMaker, 20);
	}

	public RichSuggestBox(final RichSuggestOracle<S> oracle, final WidgetMaker<S> widgetMaker, final int maxSuggestions) {
		super();

		Util.requireNotNull(oracle);
		Util.requireNotNull(widgetMaker);
		Util.require(maxSuggestions >= 0);

		this.oracle = oracle;
		this.widgetMaker = widgetMaker;
		this.maxSuggestions = maxSuggestions;

		initTextBox();

		initPopup();

		initWidget(textBox);
	}

	void clampHighlightedPopupRow() {
		if (highlightedPopupRow.isDefined()) {
			if (highlightedPopupRow.get() < 0) {
				highlightedPopupRow.set(0);
			} else if (highlightedPopupRow.get() >= suggestionsPanel.getWidgetCount()) {
				highlightedPopupRow.set(suggestionsPanel.getWidgetCount() - 1);
			}
		}
	}

	@Override
	public String getText() {
		return textBox.getText();
	}

	@Override
	public void setText(final String text) {
		textBox.setText(text);
	}

	@Override
	public void fireSuggestionEvent(final RichSuggestionEvent<S> event) {
		fireEvent(event);

		hidePopup();
	}

	public HandlerRegistration addSelectionHandler(final RichSuggestionEventHandler<S> handler) {
		Util.requireNotNull(handler);

		return this.addHandler(handler, RichSuggestionEvent.<S> getType());
	}

	private void initTextBox() {
		textBox.addFocusHandler(new FocusHandler() {
			@Override
			public void onFocus(final FocusEvent event) {
				clearTextBox();
			}
		});

		// NB: Use keyUp instead of keyPress so that suggestions are presented
		// after each keypress based on the entire contents of the TextBox
		textBox.addKeyUpHandler(new KeyUpHandler() {
			@Override
			public void onKeyUp(final KeyUpEvent event) {
				if (event.isUpArrow()) {
					decrementHighlightedRow();
				} else if (event.isDownArrow()) {
					incrementHighlightedRow();
				} else if (isEnter(event)) {
					if (highlightedPopupRow.isDefined()) {
						selectHighlightedRow();
					}
				} else if (isKeyThatTriggersSuggestRequest(event)) {
					if (getText().length() > 0) {
						oracle.requestSuggestions(new RichSuggestRequest(maxSuggestions, textBox.getText()), new RichSuggestCallback<S>() {
							@Override
							public void onSuggestionsReady(final RichSuggestRequest request, final RichSuggestResponse<S> response) {
								if (response.hasSuggestions()) {
									fillSuggestionPanel(response);

									positionAndShowPopup();
								} else {
									hidePopup();
								}
							}
						});
					} else {
						hidePopup();
					}
				}
			}

			boolean isKeyThatTriggersSuggestRequest(final KeyUpEvent event) {
				final int keyCode = event.getNativeKeyCode();

				return keyCode == ' ' || (keyCode >= 'A' && keyCode <= 'Z') || (keyCode >= '0' && keyCode <= '9') || keyCode == KeyCodes.KEY_BACKSPACE || keyCode == KeyCodes.KEY_DELETE;
			}

			boolean isEnter(final KeyUpEvent event) {
				return event.getNativeKeyCode() == KeyCodes.KEY_ENTER;
			}
		});
	}

	void setHighlightedRow(final int r) {
		highlightedPopupRow.set(r);

		clampHighlightedPopupRow();

		Log.trace("highlightedPopupRow: " + highlightedPopupRow.getOrElse(-1));
	}

	void zeroHighlightedRow() {
		highlightedPopupRow.set(0);

		Log.trace("highlightedPopupRow: " + highlightedPopupRow.getOrElse(-1));
	}

	void decrementHighlightedRow() {
		if (highlightedPopupRow.isDefined()) {
			highlightedPopupRow.set(highlightedPopupRow.get() - 1);

			clampHighlightedPopupRow();
		} else {
			zeroHighlightedRow();
		}

		Log.trace("highlightedPopupRow: " + highlightedPopupRow.getOrElse(-1));
	}

	void incrementHighlightedRow() {
		if (highlightedPopupRow.isDefined()) {
			highlightedPopupRow.set(highlightedPopupRow.get() + 1);

			clampHighlightedPopupRow();
		} else {
			zeroHighlightedRow();
		}

		Log.trace("highlightedPopupRow: " + highlightedPopupRow.getOrElse(-1));
	}

	public RichSuggestOracle<S> getOracle() {
		return oracle;
	}

	public void setOracle(final RichSuggestOracle<S> oracle) {
		Util.requireNotNull(oracle);

		this.oracle = oracle;
	}

	public WidgetMaker<S> getWidgetMaker() {
		return widgetMaker;
	}

	public void setWidgetMaker(final WidgetMaker<S> widgetMaker) {
		Util.requireNotNull(widgetMaker);

		this.widgetMaker = widgetMaker;
	}

	Observable<Integer> getHighlightedPopupRow() {
		return highlightedPopupRow;
	}

	int getMaxSuggestions() {
		return maxSuggestions;
	}

	TextBox getTextBox() {
		return textBox;
	}

	private void initPopup() {
		suggestionPopup.setAnimationEnabled(false);
	}

	private void positionPopup() {
		final Element textBoxElement = textBox.getElement();

		// TODO: HACK ALERT: 2 is completely arbitrary; used to make sure
		// suggest popup
		// lines up properly horizontally
		final int left = textBoxElement.getAbsoluteLeft();

		// TODO: HACK ALERT: "- textBoxElement.getClientHeight() - 7" is totally
		// arbitrary.
		// Needed to place suggest popup right under text box. :( :(
		final int bottom = textBoxElement.getAbsoluteBottom() - textBoxElement.getClientHeight() - 7;

		suggestionPopup.setPopupPosition(left, bottom);
	}

	// NB: Exposed for testing
	public void fillSuggestionPanel(final RichSuggestResponse<S> response) {
		refreshSuggestionPanel();

		int i = 0;

		for (final S suggestion : response.getSuggestions()) {
			final Widget widget = widgetMaker.makeWidget(suggestion);

			suggestionsPanel.add(rowFromWidget(i, suggestion, widget));

			++i;
		}
	}

	void refreshSuggestionPanel() {
		highlightedPopupRow.clear();

		if (suggestionsPanel != null) {
			suggestionsPanel.stopObserving();
			suggestionsPanel.clear();
		}

		suggestionsPanel = new SuggestionsPanel(highlightedPopupRow);

		suggestionPopup.setWidget(suggestionsPanel);
	}

	RichSuggestionRow rowFromWidget(final int index, final S suggestion, final Widget widget) {
		final RichSuggestionRow richSuggestionRow = new RichSuggestionRow(this, widget, new Runnable() {
			@Override
			public void run() {
				fireSuggestionEvent(RichSuggestionEvent.from(suggestion));
			}
		});

		richSuggestionRow.addMouseOverHandler(new MouseOverHandler() {
			@Override
			public void onMouseOver(final MouseOverEvent event) {
				setHighlightedRow(index);
			}
		});

		richSuggestionRow.addMouseOutHandler(new MouseOutHandler() {
			@Override
			public void onMouseOut(MouseOutEvent event) {
				highlightedPopupRow.clear();
			}
		});

		return richSuggestionRow;
	}

	private void positionAndShowPopup() {
		if (!suggestionPopup.isShowing()) {
			positionPopup();

			suggestionPopup.show();
		}
	}

	@Override
	public void hidePopup() {
		Log.debug("Hiding popup");

		suggestionPopup.hide();
	}

	// NB: For testing
	PopupPanel getSuggestionPopup() {
		return suggestionPopup;
	}

	// NB: For testing
	SuggestionsPanel getSuggestionsPanel() {
		return suggestionsPanel;
	}

	void selectHighlightedRow() {
		final RichSuggestionRow highlightedRow = (RichSuggestionRow) suggestionsPanel.getWidget(highlightedPopupRow.get());

		highlightedRow.select();

		hidePopup();
	}

	void clearTextBox() {
		textBox.setText("");
	}
}
