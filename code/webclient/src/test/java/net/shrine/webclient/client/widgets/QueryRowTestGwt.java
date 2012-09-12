package net.shrine.webclient.client.widgets;

import java.util.Collection;
import java.util.Date;
import java.util.Iterator;

import net.shrine.webclient.client.AbstractWebclientTest;
import net.shrine.webclient.client.controllers.Controllers;
import net.shrine.webclient.client.controllers.MockQueryService;
import net.shrine.webclient.client.state.QueryGroup;
import net.shrine.webclient.shared.domain.And;
import net.shrine.webclient.shared.domain.Or;
import net.shrine.webclient.shared.domain.Term;

import org.junit.Test;

import com.allen_sauer.gwt.dnd.client.PickupDragController;
import com.google.gwt.event.shared.SimpleEventBus;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.ui.RootPanel;

/**
 * 
 * @author clint
 * @date Apr 23, 2012
 */
public class QueryRowTestGwt extends AbstractWebclientTest {

	@Test
	public void testConstructorGuards() {
		final MockQueryService mockQueryService = new MockQueryService(); 
		
		final Controllers controllers = new Controllers(state(), mockQueryService);
		final QueryGroup queryGroup = new QueryGroup(new SimpleEventBus(), "foo", term("foo"));
		final PickupDragController dragController = new PickupDragController(RootPanel.get(), false);
		
		try {
			new QueryRow(null, controllers, dragController);

			fail("Should have thrown");
		} catch (IllegalArgumentException expected) { }

		try {
			
			
			new QueryRow(queryGroup, null, dragController);

			fail("Should have thrown");
		} catch (IllegalArgumentException expected) { }

		try {
			new QueryRow(queryGroup, controllers, null);

			fail("Should have thrown");
		} catch (IllegalArgumentException expected) { }
		
		try {
			new QueryRow(null, null, null);

			fail("Should have thrown");
		} catch (IllegalArgumentException expected) { }
	}

	@Test
	public void testQueryRow() throws Exception {
		final Controllers controllers = new Controllers(state(), new MockQueryService());
		final PickupDragController dragController = new PickupDragController(RootPanel.get(), false);

		final QueryRow queryRow = new QueryRow(new QueryGroup(new SimpleEventBus(), "blah", term("foo")), controllers, dragController);

		//Test negate box init
		{
			assertFalse(queryRow.negationCheckbox.getValue());

			final QueryGroup group2 = new QueryGroup(new SimpleEventBus(), "nuh", term("foo"));
			
			group2.setNegated(true);
			
			final QueryRow row2 = new QueryRow(group2, controllers, dragController);
			
			assertTrue(row2.negationCheckbox.getValue());
		}
		
		//test date box inits
		{
			assertNull(queryRow.startDate.getValue());
			assertNull(queryRow.endDate.getValue());

			final QueryGroup group2 = new QueryGroup(new SimpleEventBus(), "bar", term("foo"));

			final Date start = new Date();
			final Date end = new Date();
			
			group2.setStart(start);
			group2.setEnd(end);
			
			final QueryRow row2 = new QueryRow(group2, controllers, dragController);
			
			assertEquals(trim(start), row2.startDate.getValue());
			assertEquals(trim(end), row2.endDate.getValue());
		}
		
		//test min occurs spinner init
		{
			assertEquals(1, queryRow.minOccursSpinner.getValue());

			final QueryGroup group2 = new QueryGroup(new SimpleEventBus(), "zuh", term("foo"));
			
			group2.setMinOccurances(99);
			
			final QueryRow row2 = new QueryRow(group2, controllers, dragController);
			
			assertEquals(99, row2.minOccursSpinner.getValue());
		}
	}

	private Date trim(final Date date) throws Exception {
		final DateTimeFormat dateFormat = DateTimeFormat.getFormat("yyyyMMdd");
		
		return dateFormat.parse(dateFormat.format(date));
	}

	@Test
	public void testInform() {
		final Controllers controllers = new Controllers(state(), new MockQueryService());
		final PickupDragController dragController = new PickupDragController(RootPanel.get(), false);

		final QueryRow queryRow = new QueryRow(new QueryGroup(new SimpleEventBus(), "blah", term("foo")), controllers, dragController);
		
		assertFalse(queryRow.negationCheckbox.getValue());
		assertNull(queryRow.startDate.getValue());
		assertNull(queryRow.endDate.getValue());
		assertEquals(1, queryRow.minOccursSpinner.getValue());
	}

	@Test
	public void testMakeQueryTermsFrom() {
		final Controllers controllers = new Controllers(state(), new MockQueryService());
		final PickupDragController dragController = new PickupDragController(RootPanel.get(), false);

		final QueryRow queryRow = new QueryRow(new QueryGroup(new SimpleEventBus(), "nuh", term("foo")), controllers, dragController);
		
		try {
			queryRow.makeQueryTermsFrom(null);
			
			fail("Should have thrown");
		} catch(IllegalArgumentException expected) { }
		
		try {
			queryRow.makeQueryTermsFrom(new And());
			
			fail("Should have thrown");
		} catch(IllegalArgumentException expected) { }
		
		final Term t1 = term("foo");
		final Term t2 = term("foo");
		final Term t3 = term("foo");
		
		{
			final Collection<QueryTerm> terms = queryRow.makeQueryTermsFrom(t1);
			
			assertEquals(1, terms.size());
			
			assertEquals(t1.getSimpleName(), terms.iterator().next().termSpan.getInnerText());
			assertEquals(t1.getPath(), terms.iterator().next().getTitle());
		}
		
		{
			final Collection<QueryTerm> terms = queryRow.makeQueryTermsFrom(new Or(t2, t3));
			
			assertEquals(2, terms.size());
			
			final Iterator<QueryTerm> iterator = terms.iterator();
			
			final QueryTerm queryTerm1 = iterator.next();
			final QueryTerm queryTerm2 = iterator.next();
			
			assertEquals(t2.getSimpleName(), queryTerm1.termSpan.getInnerText());
			assertEquals(t2.getPath(), queryTerm1.getTitle());
			
			assertEquals(t3.getSimpleName(), queryTerm2.termSpan.getInnerText());
			assertEquals(t3.getPath(), queryTerm2.getTitle());
		}
	}
}
