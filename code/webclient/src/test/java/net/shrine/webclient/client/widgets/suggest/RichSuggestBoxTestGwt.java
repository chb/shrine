package net.shrine.webclient.client.widgets.suggest;

import static java.util.Arrays.asList;

import java.util.List;

import net.shrine.webclient.client.AbstractWebclientTest;
import net.shrine.webclient.client.util.Util;

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
