package net.shrine.webclient.client.widgets.suggest;

import com.google.gwt.user.client.rpc.IsSerializable;

import net.shrine.webclient.client.AbstractWebclientTest;

/**
 * 
 * @author clint
 * @date Apr 13, 2012
 */
public class RichSuggestionEventTestGwt extends AbstractWebclientTest {

	public void testConstructor() {
		//null should be allowed
		final RichSuggestionEvent<Foo> event1 = new RichSuggestionEvent<Foo>(null);
		
		assertNull(event1.getSuggestion());
		
		assertNull((new RichSuggestionEvent<Foo>()).getSuggestion());
		
		final Foo foo = new Foo(123);
		
		final RichSuggestionEvent<Foo> event2 = new RichSuggestionEvent<Foo>(foo);
		
		assertEquals(foo.value, event2.getSuggestion().value);
	}
	
	public void testFrom() {
		final Foo foo = new Foo(99);
		
		final RichSuggestionEvent<Foo> event = RichSuggestionEvent.from(foo);
		
		assertEquals(foo.value, event.getSuggestion().value);
		
		final RichSuggestionEvent<Foo> event2 = RichSuggestionEvent.from((Foo)null);
		
		assertNull(event2.getSuggestion());
	}
	
	public void testDispatch() {
		final MockHandler handler = new MockHandler();
		
		final Foo foo = new Foo(123);
		
		final RichSuggestionEvent<Foo> event = RichSuggestionEvent.from(foo);
		
		event.dispatch(handler);
		
		assertEquals(foo.value, handler.invokedWith.getSuggestion().value);
	}
	
	private final class MockHandler implements RichSuggestionEventHandler<Foo> {
		public RichSuggestionEvent<Foo> invokedWith = null;

		@Override
		public void onSelectionMade(RichSuggestionEvent<Foo> event) {
			invokedWith = event;
		}
	}

	private static final class Foo implements IsSerializable { 
		public final int value;

		public Foo(final int value) {
			super();
			
			this.value = value;
		}
	} 
}
