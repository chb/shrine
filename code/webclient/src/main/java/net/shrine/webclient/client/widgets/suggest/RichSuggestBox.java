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
public class RichSuggestBox<S> extends Composite implements SuggestRowContainer<S>, HasText {

    private final TextBox textBox = new TextBox();

    private RichSuggestOracle<S> oracle;

    private WidgetMaker<S> widgetMaker;

    private final PopupPanel suggestionPopup = new PopupPanel(true, false);

    private SuggestionsPanel suggestionsPanel;

    private final Observable<Integer> highlightedPopupRow = Observable.empty();

    private final int maxSuggestions;

    private static final int DefaultMaxSuggestions = 20;

    private int suggestionRequestSequenceNumber = Integer.MIN_VALUE;

    public RichSuggestBox(final RichSuggestOracle<S> oracle, final WidgetMaker<S> widgetMaker) {
        this(oracle, widgetMaker, DefaultMaxSuggestions);
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

        hidePopup();

        initWidget(textBox);
    }

    final void clampHighlightedPopupRow() {
        if (highlightedPopupRow.isDefined()) {
            if (highlightedPopupRow.get() < 0) {
                highlightedPopupRow.set(0);
            } else if (highlightedPopupRow.get() >= suggestionsPanel.getWidgetCount()) {
                highlightedPopupRow.set(suggestionsPanel.getWidgetCount() - 1);
            }
        }
    }

    @Override
    public final String getText() {
        return textBox.getText();
    }

    @Override
    public final void setText(final String text) {
        textBox.setText(text);
    }

    final boolean isEmpty() {
        return getText().length() == 0;
    }

    @Override
    public final void fireSuggestionEvent(final RichSuggestionEvent<S> event) {
        fireEvent(event);

        hidePopup();
    }

    public final HandlerRegistration addSelectionHandler(final RichSuggestionEventHandler<S> handler) {
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
        textBox.addKeyUpHandler(new SuggestBoxKeyUpHandler());

        textBox.removeStyleName("gwt-TextBox");
    }

    final void setHighlightedRow(final int r) {
        highlightedPopupRow.set(r);

        clampHighlightedPopupRow();

        Log.trace("highlightedPopupRow: " + highlightedPopupRow.getOrElse(-1));
    }

    final void zeroHighlightedRow() {
        highlightedPopupRow.set(0);

        Log.trace("highlightedPopupRow: " + highlightedPopupRow.getOrElse(-1));
    }

    final void decrementHighlightedRow() {
        if (highlightedPopupRow.isDefined()) {
            highlightedPopupRow.set(highlightedPopupRow.get() - 1);

            clampHighlightedPopupRow();
        } else {
            zeroHighlightedRow();
        }

        Log.trace("highlightedPopupRow: " + highlightedPopupRow.getOrElse(-1));
    }

    final void incrementHighlightedRow() {
        if (highlightedPopupRow.isDefined()) {
            highlightedPopupRow.set(highlightedPopupRow.get() + 1);

            clampHighlightedPopupRow();
        } else {
            zeroHighlightedRow();
        }

        Log.trace("highlightedPopupRow: " + highlightedPopupRow.getOrElse(-1));
    }

    public final RichSuggestOracle<S> getOracle() {
        return oracle;
    }

    public final void setOracle(final RichSuggestOracle<S> oracle) {
        Util.requireNotNull(oracle);

        this.oracle = oracle;
    }

    public final WidgetMaker<S> getWidgetMaker() {
        return widgetMaker;
    }

    public final void setWidgetMaker(final WidgetMaker<S> widgetMaker) {
        Util.requireNotNull(widgetMaker);

        this.widgetMaker = widgetMaker;
    }

    final Observable<Integer> getHighlightedPopupRow() {
        return highlightedPopupRow;
    }

    final int getMaxSuggestions() {
        return maxSuggestions;
    }

    final TextBox getTextBox() {
        return textBox;
    }

    private void initPopup() {
        suggestionPopup.setAnimationEnabled(false);

        // TODO: HACK ALERT (Match Seth's HTML)
        suggestionPopup.setStyleName("searchBoxPopup");
        suggestionPopup.getElement().setId("searchBoxPopup");
    }

    private void positionPopup() {
        final Element textBoxElement = textBox.getElement();

        final int left = textBoxElement.getAbsoluteLeft();

        final int bottom = textBoxElement.getAbsoluteBottom();

        suggestionPopup.setPopupPosition(left, bottom);
    }

    // NB: Exposed for testing
    public final void fillSuggestionPanel(final RichSuggestResponse<S> response) {
        refreshSuggestionPanel();

        int i = 0;

        for (final S suggestion : response.getSuggestions()) {
            final Widget widget = widgetMaker.makeWidget(suggestion);

            final RichSuggestionRow row = rowFromWidget(i, suggestion, widget);

            suggestionsPanel.add(row);

            ++i;
        }
    }

    final void refreshSuggestionPanel() {
        highlightedPopupRow.clear();

        if (suggestionsPanel != null) {
            suggestionsPanel.stopObserving();
            suggestionsPanel.clear();
        }

        suggestionsPanel = new SuggestionsPanel(highlightedPopupRow);

        suggestionPopup.setWidget(suggestionsPanel);
    }

    final RichSuggestionRow rowFromWidget(final int index, final S suggestion, final Widget widget) {
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
    public final void hidePopup() {
        Log.debug("Hiding popup");

        suggestionPopup.hide();
    }

    // NB: For testing
    final PopupPanel getSuggestionPopup() {
        return suggestionPopup;
    }

    // NB: For testing
    final SuggestionsPanel getSuggestionsPanel() {
        return suggestionsPanel;
    }

    final void selectHighlightedRow() {
        final RichSuggestionRow highlightedRow = (RichSuggestionRow) suggestionsPanel.getWidget(highlightedPopupRow.get());

        highlightedRow.select();

        hidePopup();

        clearTextBox();
    }

    @Override
    public final void clearTextBox() {
        textBox.setText("");
    }

    private static boolean isKeyThatTriggersSuggestRequest(final KeyUpEvent event) {
        final int keyCode = event.getNativeKeyCode();

        return isSpace(keyCode) || isLetterOrNumber(keyCode) || isBackspace(keyCode) || isDelete(keyCode);
    }

    private static boolean isSpace(final int keyCode) {
        return keyCode == ' ';
    }

    private static boolean isDelete(final int keyCode) {
        return keyCode == KeyCodes.KEY_DELETE;
    }

    private static boolean isBackspace(final int keyCode) {
        return keyCode == KeyCodes.KEY_BACKSPACE;
    }

    private static boolean isLetterOrNumber(final int keyCode) {
        return isLetter(keyCode) || isNumber(keyCode);
    }

    private static boolean isNumber(final int keyCode) {
        return (keyCode >= '0' && keyCode <= '9');
    }

    private static boolean isLetter(final int keyCode) {
        return (keyCode >= 'A' && keyCode <= 'Z') || (keyCode >= 'a' && keyCode <= 'z');
    }

    private static boolean isEnter(final KeyUpEvent event) {
        return event.getNativeKeyCode() == KeyCodes.KEY_ENTER;
    }

    private final class SuggestBoxKeyUpHandler implements KeyUpHandler {
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
                if (!isEmpty()) {
                    requestSuggestions();
                } else {
                    hidePopup();
                }
            }
        }
    }

    // NB: Default access for tests
    boolean isNew(final RichSuggestResponse<S> response) {
        return response.getSequenceNumber() >= suggestionRequestSequenceNumber;
    }

    // NB: Default access for tests
    int getSuggestionRequestSequenceNumber() {
        return suggestionRequestSequenceNumber;
    }

    final void requestSuggestions() {
        final int sequenceNumber = SequenceNumbers.next();

        suggestionRequestSequenceNumber = sequenceNumber;

        oracle.requestSuggestions(new RichSuggestRequest(maxSuggestions, textBox.getText(), sequenceNumber), new RichSuggestCallback<S>() {
            @Override
            public void onSuggestionsReady(final RichSuggestRequest request, final RichSuggestResponse<S> response) {
                if (isNew(response)) {
                    if (response.hasSuggestions()) {
                        fillSuggestionPanel(response);

                        positionAndShowPopup();
                    } else {
                        hidePopup();
                    }
                }
            }
        });
    }
}
