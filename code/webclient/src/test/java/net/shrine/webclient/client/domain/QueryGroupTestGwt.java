package net.shrine.webclient.client.domain;

import java.util.Date;
import java.util.HashMap;

import net.shrine.webclient.client.AbstractWebclientTest;
import net.shrine.webclient.client.util.MockObserver;
import net.shrine.webclient.client.util.Observable;

import org.junit.Test;

import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.DateTimeFormat.PredefinedFormat;

/**
 * 
 * @author clint
 * @date Apr 24, 2012
 */
public class QueryGroupTestGwt extends AbstractWebclientTest {

	private DateTimeFormat dateFormat;
	
	@Override
	protected void gwtSetUp() throws Exception {
		super.gwtSetUp();
		
		dateFormat = DateTimeFormat.getFormat(PredefinedFormat.ISO_8601);
	}

	@Override
	protected void gwtTearDown() throws Exception {
		super.gwtTearDown();
		
		dateFormat = null;
	}

	@Test
	public void testQueryGroup() {
		try {
			new QueryGroup(null, Observable.<HashMap<String, IntWrapper>>empty());
			fail("Should have thrown");
		} catch(IllegalArgumentException expected) { }
		
		try {
			new QueryGroup(new Term("foo"), null);
			fail("Should have thrown");
		} catch(IllegalArgumentException expected) { }
		
		try {
			new QueryGroup(null, null);
			fail("Should have thrown");
		} catch(IllegalArgumentException expected) { }
	}

	@Test
	public void testDateToXml() {
		final Date date = new Date();
		
		final String expected = "<foo>" + dateFormat.format(date) + "</foo>";
		
		assertEquals(expected, QueryGroup.dateToXml("foo", date));
	}
	
	@Test
	public void testToXmlString() {
		final Term t1 = new Term("foo");
		final Term t2 = new Term("bar");
		
		final QueryGroup queryGroup = new QueryGroup(t1, Observable.<HashMap<String, IntWrapper>>empty());
		
		assertEquals(t1.toXmlString(), queryGroup.toXmlString());
		
		final Or or = new Or(t1, t2);
		
		queryGroup.setExpression(or);
		
		assertEquals(or.toXmlString(), queryGroup.toXmlString());
		
		queryGroup.setNegated(true);
		
		final String negatedOrXml = "<not>" + or.toXmlString() + "</not>";
		
		assertEquals(negatedOrXml, queryGroup.toXmlString());
		
		final Date startDate = new Date();
		
		queryGroup.setStart(startDate);
		
		final String withStartXml = "<dateBounded><start>"+ dateFormat.format(startDate) + "</start>" + negatedOrXml + "</dateBounded>";
		
		assertEquals(withStartXml, queryGroup.toXmlString());
		
		final Date endDate = new Date();
		
		queryGroup.setStart(null);
		queryGroup.setEnd(endDate);
		
		final String withEndXml = "<dateBounded><end>"+ dateFormat.format(endDate) + "</end>" + negatedOrXml + "</dateBounded>";
		
		assertEquals(withEndXml, queryGroup.toXmlString());
		
		queryGroup.setStart(startDate);
		
		final String withStartAndEndXml = "<dateBounded><start>" + dateFormat.format(startDate) + "</start><end>"+ dateFormat.format(endDate) + "</end>" + negatedOrXml + "</dateBounded>";
		
		assertEquals(withStartAndEndXml, queryGroup.toXmlString());
		
		queryGroup.setMinOccurances(99);
		
		final String withMinOccuranceXml = "<occurs><min>99</min>" + withStartAndEndXml + "</occurs>";
		
		assertEquals(withMinOccuranceXml, queryGroup.toXmlString());
	}

	@Test
	public void testSetExpression() {
		final QueryGroup queryGroup = new QueryGroup(new Term("foo"), Observable.<HashMap<String, IntWrapper>>empty());
		
		final MockObserver observer = new MockObserver(queryGroup);
		
		assertFalse(observer.informed);
		
		final Term t2 = new Term("bar");
		
		queryGroup.setExpression(t2);
		
		assertTrue(observer.informed);
		
		assertEquals(t2, queryGroup.getExpression());
		
		try {
			queryGroup.setExpression(null);
			fail("Should have thrown");
		} catch(IllegalArgumentException expected) { }
	}

	@Test
	public void testSetMinOccurances() {
		final QueryGroup queryGroup = new QueryGroup(new Term("foo"), Observable.<HashMap<String, IntWrapper>>empty());
		
		final MockObserver observer = new MockObserver(queryGroup);
		
		assertFalse(observer.informed);
		
		queryGroup.setMinOccurances(99);
		
		assertTrue(observer.informed);
		
		assertEquals(99, queryGroup.getMinOccurances());
		
		try {
			queryGroup.setMinOccurances(0);
			fail("Should have thrown");
		} catch(IllegalArgumentException expected) { }
		
		try {
			queryGroup.setMinOccurances(-10);
			fail("Should have thrown");
		} catch(IllegalArgumentException expected) { }
	}

	@Test
	public void testSetStart() {
		final QueryGroup queryGroup = new QueryGroup(new Term("foo"), Observable.<HashMap<String, IntWrapper>>empty());
		
		final MockObserver observer = new MockObserver(queryGroup);
		
		assertFalse(observer.informed);
		
		assertNull(queryGroup.getStart());
		
		final Date date = new Date();
		
		queryGroup.setStart(date);
		
		assertTrue(observer.informed);
		
		assertEquals(date, queryGroup.getStart());
		
		queryGroup.setStart(null);
		
		assertNull(queryGroup.getStart());
	}

	@Test
	public void testSetEnd() {
		final QueryGroup queryGroup = new QueryGroup(new Term("foo"), Observable.<HashMap<String, IntWrapper>>empty());
		
		final MockObserver observer = new MockObserver(queryGroup);
		
		assertFalse(observer.informed);
		
		assertNull(queryGroup.getEnd());
		
		final Date date = new Date();
		
		queryGroup.setEnd(date);
		
		assertTrue(observer.informed);
		
		assertEquals(date, queryGroup.getEnd());
		
		queryGroup.setEnd(null);
		
		assertNull(queryGroup.getEnd());
	}
}
