package net.shrine.webclient.client.widgets.suggest;

import net.shrine.webclient.client.AbstractWebclientTest;
import net.shrine.webclient.client.Events;
import net.shrine.webclient.client.widgets.suggest.RichSuggestionRow.StyleNames;

import com.google.gwt.user.client.ui.Label;

/**
 * 
 * @author clint
 * @date Apr 13, 2012
 */
public class RichSuggestionRowTestGwt extends AbstractWebclientTest {

	public void testStyleNamesEnum() {
		assertEquals("richSuggestionRow-Highlighted", StyleNames.Highlighted.toStyleName());
		assertEquals("richSuggestionRow-NotHighlighted", StyleNames.NotHighlighted.toStyleName());
	}
	
	private final Runnable Noop = new Runnable() {
		@Override
		public void run() { /* NOOP */ }
	}; 

	public void testHighlightAndUnhighlight() {
		final RichSuggestionRow row = new RichSuggestionRow(new MockHasHideablePopup(), new Label("nuh"), Noop);

		row.unHighlight();

		assertNotHighlighted(row);

		row.highlight();

		assertHighlighted(row);

		row.unHighlight();

		assertNotHighlighted(row);
	}

	private void assertHighlighted(final RichSuggestionRow row) {
		assertEquals(StyleNames.Highlighted.toStyleName(), row.getStyleName());
	}

	private void assertNotHighlighted(final RichSuggestionRow row) {
		assertEquals(StyleNames.NotHighlighted.toStyleName(), row.getStyleName());
	}

	public void testConstructorGuards() {
		try {
			new RichSuggestionRow(null, new Label("nuh"), Noop);

			fail("Expected an NPE");
		} catch (IllegalArgumentException expected) { }

		try {
			new RichSuggestionRow(new MockHasHideablePopup(), null, Noop);

			fail("Expected an NPE");
		} catch (IllegalArgumentException expected) { }
		
		try {
			new RichSuggestionRow(new MockHasHideablePopup(), new Label(""), null);

			fail("Expected an NPE");
		} catch (IllegalArgumentException expected) { }

		try {
			new RichSuggestionRow(null, null, null);

			fail("Expected an NPE");
		} catch (IllegalArgumentException expected) { }
	}

	public void testDefaultEventHandlers() {
		final class MockRunnable implements Runnable {
			public boolean invoked = false;

			@Override
			public void run() { invoked = true; }
		}

		final MockHasHideablePopup container = new MockHasHideablePopup();
		
		final MockRunnable onSelect = new MockRunnable();

		assertFalse(onSelect.invoked);
		
		final RichSuggestionRow row = new RichSuggestionRow(container, new Label("nuh"), onSelect);

		row.fireEvent(Events.mouseOver());

		assertNotHighlighted(row);
		assertFalse(onSelect.invoked);

		row.fireEvent(Events.mouseOver());

		assertNotHighlighted(row);
		assertFalse(onSelect.invoked);

		row.fireEvent(Events.mouseOut());

		assertNotHighlighted(row);
		assertFalse(container.hidden);
		assertFalse(onSelect.invoked);

		row.fireEvent(Events.click());

		assertTrue(container.hidden);
		assertFalse(onSelect.invoked);
		
		container.hidden = false;
		onSelect.invoked = false;
		
		row.select();
		
		assertTrue(container.hidden);
		assertTrue(onSelect.invoked);
	}
	
	private static final class MockHasHideablePopup implements HasHideablePopup {
		public boolean hidden = false;

		@Override
		public void hidePopup() {
			hidden = true;
		}
	}
}
