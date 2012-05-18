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
	public void testFirst() {
		try {
			Util.first(Collections.emptyList());
			fail("Should have thrown");
		} catch(IllegalArgumentException expected) { }
		
		assertEquals(Integer.valueOf(99), Util.first(asList(99)));
		
		assertEquals(Integer.valueOf(42), Util.first(asList(42, 99, 123)));
		
		assertEquals(Integer.valueOf(42), Util.first(Util.makeHashSet(42)));
	}
	
	@Test
	public void testLast() {
		try {
			Util.first(Collections.emptyList());
			fail("Should have thrown");
		} catch(IllegalArgumentException expected) { }
		
		assertEquals(Integer.valueOf(99), Util.last(asList(99)));
		
		assertEquals(Integer.valueOf(123), Util.last(asList(42, 99, 123)));
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void testPairWise() {
		{
			final List<Integer> empty = Util.makeArrayList();
			
			final List<List<Integer>> pairWise = Util.pairWise(empty);
			
			assertNotSame(empty, pairWise);
			
			assertTrue(empty.isEmpty());
			assertTrue(pairWise.isEmpty());
		}
		
		{
			final List<Integer> oneThing = asList(1);
			
			final List<List<Integer>> pairWise = Util.pairWise(oneThing);
			
			assertNotSame(oneThing, pairWise);
			
			assertEquals(1, oneThing.size());
			assertEquals(pairWise, asList(asList(1)));
		}
		
		{
			final List<Integer> sixThings = asList(1,2,3,4,5,6);
			
			final List<List<Integer>> pairWise = Util.pairWise(sixThings);
			
			assertNotSame(sixThings, pairWise);
			
			assertEquals(6, sixThings.size());
			assertEquals(pairWise, asList(asList(1, 2), asList(2, 3), asList(3, 4), asList(4, 5), asList(5, 6)));
		}
		
		{
			final List<Integer> fiveThings = asList(1,2,3,4,5);
			
			final List<List<Integer>> pairWise = Util.pairWise(fiveThings);
			
			assertNotSame(fiveThings, pairWise);
			
			assertEquals(5, fiveThings.size());
			assertEquals(pairWise, asList(asList(1, 2), asList(2, 3), asList(3, 4), asList(4, 5)));
		}
	}
	
	@Test
	public void testSorted() {
		final Iterable<String> stuff = asList("z", "asd", "x", "foo");
		
		final List<String> sorted = Util.sorted(stuff);
		
		assertNotSame(stuff, sorted);
		assertNotNull(sorted);
		assertEquals(asList("asd", "foo", "x", "z"), sorted);
	}
	
	@Test
	public void testTake() {
		try {
			Util.take(-1, new ArrayList<String>());
			fail("Should have thrown");
		} catch(IllegalArgumentException expected) { }
		
		try {
			Util.take(1, (Iterable<?>)null);
			fail("Should have thrown");
		} catch(IllegalArgumentException expected) { }
		
		try {
			Util.take(-99, (Iterable<?>)null);
			fail("Should have thrown");
		} catch(IllegalArgumentException expected) { }
		
		{
			final List<String> list = Util.take(0, asList("foo"));
			
			assertTrue(list.isEmpty());
		}
		
		{
			final List<String> list = Util.take(5, asList("foo"));
			
			assertEquals(asList("foo"), list);
		}
		
		{
			final List<Integer> list = Util.take(3, asList(1,2,3,4,5,6,7,8,9,10));
			
			assertEquals(asList(1,2,3), list);
		}
	}
	
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
