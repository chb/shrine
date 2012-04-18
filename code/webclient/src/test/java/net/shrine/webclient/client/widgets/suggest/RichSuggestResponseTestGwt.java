package net.shrine.webclient.client.widgets.suggest;

import static java.util.Arrays.asList;

import java.util.ArrayList;

import net.shrine.webclient.client.AbstractWebclientTest;
import net.shrine.webclient.client.domain.TermSuggestion;

/**
 * 
 * @author clint
 * @date Apr 13, 2012
 */
public class RichSuggestResponseTestGwt extends AbstractWebclientTest {
	public void testConstructor() {
		
		{
			final RichSuggestResponse<TermSuggestion> empty = new RichSuggestResponse<TermSuggestion>(new ArrayList<TermSuggestion>());
			
			assertNotNull(empty.getSuggestions());
			assertEquals(0, empty.getSuggestions().size());
		}
		
		{
			final MockSuggestion suggestion = new MockSuggestion("/SHRINE/foo");
			
			final RichSuggestResponse<MockSuggestion> response = RichSuggestResponse.of(asList(suggestion));
			
			assertNotNull(response.getSuggestions());
			assertEquals(suggestion, response.getSuggestions().iterator().next());
			assertEquals(1, response.getSuggestions().size());
		}
	}
	
	public void testOf() {
		final MockSuggestion suggestion = new MockSuggestion("/SHRINE/foo");
		
		final RichSuggestResponse<MockSuggestion> response = RichSuggestResponse.of(asList(suggestion));
		
		assertNotNull(response.getSuggestions());
		assertEquals(suggestion, response.getSuggestions().iterator().next());
		assertEquals(1, response.getSuggestions().size());
	}
}
