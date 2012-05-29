package net.shrine.webclient.client.state;

import static java.util.Arrays.asList;

import java.util.Collections;
import java.util.List;

import net.shrine.webclient.client.AbstractWebclientTest;
import net.shrine.webclient.client.domain.And;
import net.shrine.webclient.client.domain.Term;
import net.shrine.webclient.client.state.ExpressionXml;
import net.shrine.webclient.client.state.QueryGroup;

import org.junit.Test;

import com.google.gwt.event.shared.SimpleEventBus;

/**
 * 
 * @author clint
 * @date Apr 23, 2012
 */
public class ExpressionXmlTestGwt extends AbstractWebclientTest {
	@Test
	public void testFromQueryGroups() {
		 
		try {
			ExpressionXml.fromQueryGroups(Collections.<QueryGroup>emptyList());
			
			fail("Should have thrown");
		} catch (IllegalArgumentException expected) { }
		
		final Term t1 = term("foo");
		
		final QueryGroup queryGroup1 = new QueryGroup(new SimpleEventBus(), "foo", t1);
		
		{
			final List<QueryGroup> queryGroups = asList(queryGroup1);
			
			final String xml = ExpressionXml.fromQueryGroups(queryGroups);
			
			assertEquals(t1.toXmlString(), xml);
		}
		
		{
			final Term t2 = term("bar");
			
			final QueryGroup queryGroup2 = new QueryGroup(new SimpleEventBus(), "bar", t2);
			
			final String xml = ExpressionXml.fromQueryGroups(asList(queryGroup1, queryGroup2));
			
			assertEquals(new And(t1, t2).toXmlString(), xml);
		}
	}
}
