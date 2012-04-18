package net.shrine.webclient.client.util;

import net.shrine.webclient.client.AbstractWebclientTest;

import org.junit.Test;

/**
 * 
 * @author clint
 * @date Apr 18, 2012
 */
public class ObservableTestGwt extends AbstractWebclientTest {

	@Test
	public void testObservableT() {
		final Observable<Integer> o = new Observable<Integer>(123);
		
		assertEquals(Integer.valueOf(123), o.get());
		
		assertFalse(o.isEmpty());
		assertTrue(o.isDefined());
	}

	@Test
	public void testObservable() {
		final Observable<Integer> o = new Observable<Integer>(123);
		
		assertFalse(o.isEmpty());
		assertTrue(o.isDefined());
	}

	@Test
	public void testEmpty() {
		final Observable<Integer> o = Observable.empty();
		
		assertTrue(o.isEmpty());
		assertFalse(o.isDefined());
	}

	@Test
	public void testFrom() {
		final Observable<Integer> o = Observable.from(123);
		
		assertEquals(Integer.valueOf(123), o.get());
		
		assertFalse(o.isEmpty());
		assertTrue(o.isDefined());
	}

	@Test
	public void testReadOnly() {
		final Observable<Integer> o = Observable.from(123);
		
		assertSame(o, o.readOnly());
	}

	@Test
	public void testClear() {
		final Observable<Integer> o = Observable.from(123);
		
		assertEquals(Integer.valueOf(123), o.get());
		
		assertFalse(o.isEmpty());
		assertTrue(o.isDefined());
		
		o.clear();
		
		assertTrue(o.isEmpty());
		assertFalse(o.isDefined());
	}
	
	@Test
	public void testClearNotifies() {
		final Observable<Integer> o = Observable.from(123);

		final MockObserver observer = new MockObserver(o);
		
		assertFalse(observer.informed);
		
		o.clear();
		
		assertTrue(observer.informed);
	}

	@Test
	public void testSet() {
		final Observable<Integer> o = Observable.from(123);
		
		assertEquals(Integer.valueOf(123), o.get());
		
		assertFalse(o.isEmpty());
		assertTrue(o.isDefined());
		
		final MockObserver observer = new MockObserver(o);
		
		assertFalse(observer.informed);
		
		o.set(99);
		
		assertTrue(observer.informed);
		
		assertEquals(Integer.valueOf(99), o.get());
		
		assertFalse(o.isEmpty());
		assertTrue(o.isDefined());
	}

	@Test
	public void testGetOrElse() {
		final Observable<Integer> o = Observable.from(123);
		
		assertEquals(Integer.valueOf(123), o.getOrElse(99));
		
		o.clear();
		
		assertEquals(Integer.valueOf(99), o.getOrElse(99));
	}

	@Test
	public void testEqualsObject() {
		final Observable<Integer> x = Observable.from(123);
		final Observable<Integer> y = Observable.from(123);
		final Observable<Integer> z = Observable.from(123);
		final Observable<Integer> a = Observable.from(99);
		final Observable<Integer> b = Observable.from(42);
		
		assertTrue(x.equals(y));
		assertTrue(y.equals(x));
		
		assertTrue(x.equals(z));
		assertTrue(z.equals(x));
		
		assertTrue(y.equals(z));
		assertTrue(z.equals(y));
		
		assertFalse(a.equals(b));
		assertFalse(b.equals(a));
		
		assertFalse(x.equals(a));
		assertFalse(x.equals(b));
		
		assertFalse(y.equals(a));
		assertFalse(y.equals(b));
		
		assertFalse(z.equals(a));
		assertFalse(z.equals(b));
		
		assertTrue(a.equals(a));
		assertTrue(b.equals(b));
		assertTrue(z.equals(z));
		
		assertFalse(a.equals(null));
		
		assertFalse(b.equals(new Object()));
		
		assertFalse(b.equals("hello!"));
	}
}
