package net.shrine.webclient.client.util;

import static java.util.Arrays.asList;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import net.shrine.webclient.client.AbstractWebclientTest;

import org.junit.Test;

/**
 * 
 * @author clint
 * @date Apr 18, 2012
 */
public class UtilTestGwt extends AbstractWebclientTest {
	@Test
	public void testCount() {
		assertEquals(0, Util.count(Collections.emptyList()));
		assertEquals(0, Util.count(new ArrayList<String>()));
		assertEquals(1, Util.count(asList(1)));
		assertEquals(2, Util.count(asList(99, 42)));
		assertEquals(3, Util.count(new HashSet<String>(asList("A", "b", "c"))));
	}

	@Test
	public void testToList() {
		final List<String> i1 = asList("a", "F", "x");
		final Collection<String> i2 = new HashSet<String>(i1);
		
		assertSame(i1, Util.toList(i1));

		//Convert to hashmap again to disregard order
		assertEquals(new HashSet<String>(i1), new HashSet<String>(Util.toList(i2)));
	}

	@Test
	public void testJoinIterableOfT() {
		assertEquals("", Util.join(Collections.emptyList()));
		assertEquals("1234", Util.join(asList(1,2,3,4)));
	}

	@Test
	public void testJoinStringIterableOfT() {
		assertEquals("", Util.join("!!!", Collections.emptyList()));
		assertEquals("1!!!2!!!3!!!4", Util.join("!!!", asList(1,2,3,4)));
	}

	@Test
	public void testRequireNotNull() {
		Util.requireNotNull("foo");
		Util.requireNotNull(new Object());
		Util.requireNotNull(-9991);
		
		try {
			final Object o = null;
			
			Util.requireNotNull(o);
			
			fail("Should have thrown");
		} catch (IllegalArgumentException expected) { }
	}

	@Test
	public void testRequireBoolean() {
		final Integer one = 1;
		final Integer four = 4;
		
		Util.require(true);
		Util.require(one == 1);
		Util.require(four % 2 == 0);
		
		try {
			Util.require(1 == 3);
			
			fail("Should have thrown");
		} catch (IllegalArgumentException expected) { }
	}

	@Test
	public void testMakeArrayListCollectionOfT() {
		final ArrayList<String> empty = Util.makeArrayList(Collections.<String>emptyList());
		
		assertTrue(empty.isEmpty());
		
		final List<Integer> list = asList(42, 99, 123, 83573);
		
		final ArrayList<Integer> copy = Util.makeArrayList(list);
		
		assertNotSame(list, copy);
		
		assertEquals(list, copy);
	}
}
