package net.shrine.webclient.client.widgets.suggest;

import static java.util.Arrays.asList;

import java.util.List;

import net.shrine.webclient.client.AbstractWebclientTest;
import net.shrine.webclient.client.Events;
import net.shrine.webclient.client.util.Util;

import com.allen_sauer.gwt.log.client.Log;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOutHandler;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.event.dom.client.MouseOverHandler;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

/**
 * 
 * @author clint
 * @date Apr 13, 2012
 */
public class RichSuggestBoxTestGwt extends AbstractWebclientTest {
	public void testConstructor() {
		try {
			new RichSuggestBox<MockSuggestion>(null, new MockWidgetMaker());
			fail("Should have thrown");
		} catch(IllegalArgumentException expected) { }
		
		try {
			new RichSuggestBox<MockSuggestion>(new MockRichSuggestOracle(), null);
			fail("Should have thrown");
		} catch(IllegalArgumentException expected) { }
		
		try {
			new RichSuggestBox<MockSuggestion>(null, null);
			fail("Should have thrown");
		} catch(IllegalArgumentException expected) { }
		
		final RichSuggestBox<MockSuggestion> suggestBox = new RichSuggestBox<MockSuggestion>(new MockRichSuggestOracle(), new MockWidgetMaker());
		
		assertNull(suggestBox.getSuggestionsPanel());
		assertNotNull(suggestBox.getSuggestionPopup());
		assertFalse(suggestBox.getSuggestionPopup().isAnimationEnabled());
		assertEquals(20, suggestBox.getMaxSuggestions());
		assertTrue(suggestBox.getHighlightedPopupRow().isEmpty());
		
		final RichSuggestBox<MockSuggestion> suggestBox2 = new RichSuggestBox<MockSuggestion>(new MockRichSuggestOracle(), new MockWidgetMaker(), 99);
		
		assertEquals(99, suggestBox2.getMaxSuggestions());
	}

	public void testGetText() {
		final RichSuggestBox<MockSuggestion> suggestBox = makeMockedOutRichSuggestBox();
		
		assertEquals("", suggestBox.getText());
		
		final String text = "klfhklafhlksdfhlksdhlksdghlsghl";
		
		suggestBox.setText(text);
		
		assertEquals(text, suggestBox.getText());
	}

	//NB: Also exercises addSelectionHandler()
	public void testFireSuggestionEvent() {
		final RichSuggestBox<MockSuggestion> suggestBox = makeMockedOutRichSuggestBox();
		
		final MockRichSuggestEventHandler handler = new MockRichSuggestEventHandler();
		
		suggestBox.addSelectionHandler(handler);
		
		final MockSuggestion suggestion = new MockSuggestion("foo");
		
		final RichSuggestionEvent<MockSuggestion> event = RichSuggestionEvent.from(suggestion);
		
		assertNull(handler.lastEventReceived);
		
		suggestBox.fireSuggestionEvent(event);
		
		assertEquals(event, handler.lastEventReceived);
	}

	public void testSetOracle() {
		final MockRichSuggestOracle newOracle = new MockRichSuggestOracle();
		
		final RichSuggestBox<MockSuggestion> suggestBox = makeMockedOutRichSuggestBox();
		
		assertNotSame(newOracle, suggestBox.getOracle());
		
		suggestBox.setOracle(newOracle);
		
		assertSame(newOracle, suggestBox.getOracle());
	}

	public void setWidgetMaker() {
		final MockWidgetMaker newWidgetMaker = new MockWidgetMaker();
		
		final RichSuggestBox<MockSuggestion> suggestBox = makeMockedOutRichSuggestBox();
		
		assertNotSame(newWidgetMaker, suggestBox.getWidgetMaker());
		
		suggestBox.setWidgetMaker(newWidgetMaker);
		
		assertSame(newWidgetMaker, suggestBox.getWidgetMaker());
	}

	public void testRefreshSuggestionPanel() {
		final RichSuggestBox<MockSuggestion> suggestBox = makeMockedOutRichSuggestBox();
		
		assertNull(suggestBox.getSuggestionsPanel());
		assertNull(suggestBox.getSuggestionPopup().getWidget());
		
		//refresh first time
		
		suggestBox.refreshSuggestionPanel();
		
		final SuggestionsPanel suggestionsPanel1 = suggestBox.getSuggestionsPanel();
		
		assertNotNull(suggestionsPanel1);
		
		assertSame(suggestionsPanel1, suggestBox.getSuggestionPopup().getWidget());
		
		//refresh again
		
		suggestBox.refreshSuggestionPanel();
		
		final SuggestionsPanel suggestionsPanel2 = suggestBox.getSuggestionsPanel();
		
		assertNotNull(suggestionsPanel2);
		
		assertNotSame(suggestionsPanel1, suggestionsPanel2);
		
		suggestionsPanel2.add(new Label(""));
		
		assertEquals(1, suggestionsPanel2.getWidgetCount());
		
		assertSame(suggestionsPanel2, suggestBox.getSuggestionPopup().getWidget());

		//refresh again
		
		suggestBox.refreshSuggestionPanel();
		
		final SuggestionsPanel suggestionsPanel3 = suggestBox.getSuggestionsPanel();
		
		assertNotSame(suggestionsPanel2, suggestionsPanel3);
		
		assertEquals(0, suggestionsPanel2.getWidgetCount()); //clear() should have been called
		assertNull(suggestionsPanel2.getHighlightedRow()); //stopObserving() should have been called
		
		assertEquals(0, suggestionsPanel3.getWidgetCount());
		assertNotNull(suggestionsPanel3.getHighlightedRow());
		
		assertSame(suggestionsPanel3, suggestBox.getSuggestionPopup().getWidget());
	}
	
	public void testKeyHandlers() {
		final MockRichSuggestOracle oracle = new MockRichSuggestOracle();
		
		final RichSuggestBox<MockSuggestion> suggestBox = new RichSuggestBox<MockSuggestion>(oracle, new MockWidgetMaker());
		
		//Misc modifiers shouldn't do anything
		assertNull(oracle.lastRequest);
		
		suggestBox.getTextBox().fireEvent(Events.keyUp(KeyCodes.KEY_ALT));
		
		assertNull(oracle.lastRequest);
		
		suggestBox.getTextBox().fireEvent(Events.keyUp(KeyCodes.KEY_LEFT));
		
		assertNull(oracle.lastRequest);
		
		suggestBox.getTextBox().fireEvent(Events.keyUp(KeyCodes.KEY_RIGHT));
		
		assertNull(oracle.lastRequest);

		//only spaces, letters, or numbers should trigger autosuggest call
		
		suggestBox.setText("foo");
		
		suggestBox.getTextBox().fireEvent(Events.keyUp('x'));
		
		assertNotNull(oracle.lastRequest);
		
		assertEquals(suggestBox.getMaxSuggestions(), oracle.lastRequest.getLimit());
		assertEquals("foo", oracle.lastRequest.getQuery());
		
		oracle.lastRequest = null;
		
		suggestBox.getTextBox().fireEvent(Events.keyUp(' '));
		
		assertNotNull(oracle.lastRequest);
		
		assertEquals(suggestBox.getMaxSuggestions(), oracle.lastRequest.getLimit());
		assertEquals("foo", oracle.lastRequest.getQuery());
		
		oracle.lastRequest = null;
		
		suggestBox.getTextBox().fireEvent(Events.keyUp('7'));
		
		assertNotNull(oracle.lastRequest);
		
		assertEquals(suggestBox.getMaxSuggestions(), oracle.lastRequest.getLimit());
		assertEquals("foo", oracle.lastRequest.getQuery());
		
		//suggestions panel should be filled, popup should be showing
		
		assertNotNull(suggestBox.getSuggestionsPanel());
		
		assertEquals(1, suggestBox.getSuggestionsPanel().getWidgetCount());
		
		assertEquals(MockRichSuggestOracle.DefaultSuggestionText, ((Label)((RichSuggestionRow)suggestBox.getSuggestionsPanel().getWidget(0)).getWidget()).getText());
		
		assertTrue(suggestBox.getSuggestionPopup().isShowing());
		
		//Subsequent ignored keys shouldn't alter popup
		
		suggestBox.getTextBox().fireEvent(Events.keyUp(KeyCodes.KEY_ALT));
		
		assertEquals(1, suggestBox.getSuggestionsPanel().getWidgetCount());
		
		assertEquals(MockRichSuggestOracle.DefaultSuggestionText, ((Label)((RichSuggestionRow)suggestBox.getSuggestionsPanel().getWidget(0)).getWidget()).getText());
		
		assertTrue(suggestBox.getSuggestionPopup().isShowing());
		
		suggestBox.getTextBox().fireEvent(Events.keyUp(KeyCodes.KEY_LEFT));
		
		assertEquals(MockRichSuggestOracle.DefaultSuggestionText, ((Label)((RichSuggestionRow)suggestBox.getSuggestionsPanel().getWidget(0)).getWidget()).getText());
		
		assertTrue(suggestBox.getSuggestionPopup().isShowing());
		
		suggestBox.getTextBox().fireEvent(Events.keyUp(KeyCodes.KEY_RIGHT));
		
		assertEquals(MockRichSuggestOracle.DefaultSuggestionText, ((Label)((RichSuggestionRow)suggestBox.getSuggestionsPanel().getWidget(0)).getWidget()).getText());
		
		assertTrue(suggestBox.getSuggestionPopup().isShowing());
		
		//Popup should be hidden if we get no suggestions from the oracle
		
		oracle.returnNoSuggestions = true;
		
		suggestBox.getTextBox().fireEvent(Events.keyUp('7'));
		
		assertNotNull(oracle.lastRequest);
		
		assertEquals(suggestBox.getMaxSuggestions(), oracle.lastRequest.getLimit());
		assertEquals("foo", oracle.lastRequest.getQuery());
		
		assertFalse(suggestBox.getSuggestionPopup().isShowing());
		
		oracle.returnNoSuggestions = false;
		
		//Send a key event to get popup filled
		suggestBox.getTextBox().fireEvent(Events.keyUp('7'));
		
		//Enter shouldn't do anything if no row is highlighted
		
		assertTrue(suggestBox.getHighlightedPopupRow().isEmpty());
		
		suggestBox.getTextBox().fireEvent(Events.keyUp(KeyCodes.KEY_ENTER));
		
		//cut-corner: if popup isn't hidden, then selectHighlightedRow() wasn't called 
		assertTrue(suggestBox.getSuggestionPopup().isShowing());
		
		//now highlight a row
		
		suggestBox.getHighlightedPopupRow().set(0);
		
		suggestBox.getTextBox().fireEvent(Events.keyUp(KeyCodes.KEY_ENTER));
		
		//cut-corner: if popup IS hidden, then selectHighlightedRow() WAS called 
		assertFalse(suggestBox.getSuggestionPopup().isShowing());
		
		//Up/Down arrows

		suggestBox.getHighlightedPopupRow().clear();
		
		oracle.numToReturn = 3;
		
		//Send a key event to get popup filled
		suggestBox.getTextBox().fireEvent(Events.keyUp('7'));
		
		suggestBox.getTextBox().fireEvent(Events.keyUp(KeyCodes.KEY_UP));
		
		assertEquals(Integer.valueOf(0), suggestBox.getHighlightedPopupRow().get());
		
		suggestBox.getTextBox().fireEvent(Events.keyUp(KeyCodes.KEY_UP));
		
		assertEquals(Integer.valueOf(0), suggestBox.getHighlightedPopupRow().get());
		
		suggestBox.getTextBox().fireEvent(Events.keyUp(KeyCodes.KEY_UP));
		
		assertEquals(Integer.valueOf(0), suggestBox.getHighlightedPopupRow().get());
		
		suggestBox.getTextBox().fireEvent(Events.keyUp(KeyCodes.KEY_DOWN));
		
		assertEquals(Integer.valueOf(1), suggestBox.getHighlightedPopupRow().get());
		
		suggestBox.getTextBox().fireEvent(Events.keyUp(KeyCodes.KEY_DOWN));
		
		assertEquals(Integer.valueOf(2), suggestBox.getHighlightedPopupRow().get());
		
		suggestBox.getTextBox().fireEvent(Events.keyUp(KeyCodes.KEY_DOWN));
		
		assertEquals(Integer.valueOf(2), suggestBox.getHighlightedPopupRow().get());
		
		suggestBox.getTextBox().fireEvent(Events.keyUp(KeyCodes.KEY_DOWN));
		
		assertEquals(Integer.valueOf(2), suggestBox.getHighlightedPopupRow().get());
		
		suggestBox.getTextBox().fireEvent(Events.keyUp(KeyCodes.KEY_UP));
		
		assertEquals(Integer.valueOf(1), suggestBox.getHighlightedPopupRow().get());
		
		suggestBox.getTextBox().fireEvent(Events.keyUp(KeyCodes.KEY_UP));
		
		assertEquals(Integer.valueOf(0), suggestBox.getHighlightedPopupRow().get());
	}
	
	public void testSetHighlightedRow() {
		/*
		highlightedPopupRow.set(r);

		clampHighlightedPopupRow();

		Log.debug("highlightedPopupRow: " + highlightedPopupRow.getOrElse(-1));
		*/
		//fail("todo");
	}

	public void testZeroHighlightedRow() {
		/*highlightedPopupRow.set(0);

		Log.debug("highlightedPopupRow: " + highlightedPopupRow.getOrElse(-1));*/
		//fail("todo");
	}

	public void testDecrementHighlightedRow() {
		/*if (highlightedPopupRow.isDefined()) {
			highlightedPopupRow.set(highlightedPopupRow.get() - 1);

			clampHighlightedPopupRow();
		} else {
			zeroHighlightedRow();
		}

		Log.debug("highlightedPopupRow: " + highlightedPopupRow.getOrElse(-1));*/
		//fail("todo");
	}

	public void testIncrementHighlightedRow() {
		/*if (highlightedPopupRow.isDefined()) {
			highlightedPopupRow.set(highlightedPopupRow.get() + 1);

			clampHighlightedPopupRow();
		} else {
			zeroHighlightedRow();
		}

		Log.debug("highlightedPopupRow: " + highlightedPopupRow.getOrElse(-1));*/
		//fail("todo");
	}
	
	public void testRowFromWidget() {
		/*final RichSuggestionRow richSuggestionRow = new RichSuggestionRow(this, widget, new Runnable() {
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

		return richSuggestionRow;*/
		//fail("todo");
	}
	
	public void testSelectHighlightedRow() {
		/*final RichSuggestionRow highlightedRow = (RichSuggestionRow) suggestionsPanel.getWidget(highlightedPopupRow.get());

		highlightedRow.select();

		hidePopup();*/
		//fail("todo");
	}
	
	public void testFillSuggestionPanel() {
		final RichSuggestBox<MockSuggestion> suggestBox = makeMockedOutRichSuggestBox();
		
		assertNull(suggestBox.getSuggestionsPanel());
		
		suggestBox.fillSuggestionPanel(RichSuggestResponse.<MockSuggestion>empty());
		
		assertFalse(suggestBox.getSuggestionsPanel().iterator().hasNext());
		
		final MockSuggestion suggestion0 = new MockSuggestion("suggestion1");
		final MockSuggestion suggestion1 = new MockSuggestion("suggestion2");
		
		suggestBox.fillSuggestionPanel(RichSuggestResponse.of(asList(suggestion0, suggestion1)));
		
		final List<Widget> rows = Util.toList(suggestBox.getSuggestionsPanel());
		
		assertNotNull(rows);
		assertEquals(2, rows.size());
		
		final RichSuggestionRow row0 = (RichSuggestionRow)rows.get(0);
		final RichSuggestionRow row1 = (RichSuggestionRow)rows.get(1);
		
		assertNotNull(row0);
		assertNotNull(row1);
		
		//Make sure we get rows with the widgets we expect MockWidgetmaker to make 
		assertEquals("suggestion1", ((Label)row0.getWidget()).getText());
		assertEquals("suggestion2", ((Label)row1.getWidget()).getText());
	}

	public void testHidePopup() {
		final RichSuggestBox<MockSuggestion> suggestBox = makeMockedOutRichSuggestBox();
		
		suggestBox.getSuggestionPopup().show();
		
		assertTrue(suggestBox.getSuggestionPopup().isShowing());
		
		suggestBox.hidePopup();
		
		assertFalse(suggestBox.getSuggestionPopup().isShowing());
	}
	
	private static RichSuggestBox<MockSuggestion> makeMockedOutRichSuggestBox() {
		return new RichSuggestBox<MockSuggestion>(new MockRichSuggestOracle(), new MockWidgetMaker());
	}
}
