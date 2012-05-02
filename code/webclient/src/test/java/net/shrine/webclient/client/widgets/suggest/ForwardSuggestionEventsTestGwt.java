package net.shrine.webclient.client.widgets.suggest;

import net.shrine.webclient.client.AbstractWebclientTest;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * 
 * @author clint
 * @date Apr 11, 2012
 */
public class ForwardSuggestionEventsTestGwt extends AbstractWebclientTest {

	public void testTo() throws Exception {

		final FooEventSink sink = new FooEventSink();

		final RichSuggestionEventHandler<Foo> handler = ForwardSuggestionEvents.to(sink);

		assertNotNull(handler);
		assertNull(sink.lastReceived);

		final Foo foo1 = new Foo(123);
		final Foo foo2 = new Foo(99);

		handler.onSelectionMade(RichSuggestionEvent.from(foo1));

		assertEquals(foo1, sink.lastReceived);
		
		handler.onSelectionMade(RichSuggestionEvent.from(foo2));

		assertEquals(foo2, sink.lastReceived);
	}

	private static final class Foo implements IsSerializable {
		public int value = -1;

		private Foo(final int value) {
			super();
			this.value = value;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + value;
			return result;
		}

		@Override
		public boolean equals(final Object obj) {
			if (this == obj) {
				return true;
			}
			if (obj == null) {
				return false;
			}
			if (getClass() != obj.getClass()) {
				return false;
			}
			final Foo other = (Foo) obj;
			if (value != other.value) {
				return false;
			}
			return true;
		}
	}

	private static final class FooEventSink implements SuggestRowContainer<Foo> {
		public Foo lastReceived = null;

		@Override
		public void fireSuggestionEvent(final RichSuggestionEvent<Foo> event) {
			lastReceived = event.getSuggestion();
		}

		@Override
		public void hidePopup() {
			// NOOP
		}
	}
}
