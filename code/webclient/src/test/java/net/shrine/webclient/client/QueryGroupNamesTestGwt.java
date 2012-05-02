package net.shrine.webclient.client;

import java.util.Arrays;
import java.util.List;

import net.shrine.webclient.client.util.Util;

import org.junit.Test;

/**
 * 
 * @author clint
 * @date Apr 19, 2012
 * 
 *       TODO: Remove static-ness
 */
public class QueryGroupNamesTestGwt extends AbstractWebclientTest {
	@Test
	public void testNext() {
		final List<String> alphabetChars = Arrays.asList("ABCDEFGHIJKLMNOPQRSTUVWXYZ".split(""));

		final List<String> alphabet = Util.makeArrayList();

		for (final String l : alphabetChars) {
			final String letter = l.trim();

			if (letter.length() > 0) {
				alphabet.add(letter);
			}
		}

		final List<String> alphabet1 = Util.makeArrayList();

		for (final String letter : alphabet) {
			alphabet1.add(letter + "1");
		}

		final List<String> alphabet2 = Util.makeArrayList();

		for (final String letter : alphabet) {
			alphabet2.add(letter + "2");
		}

		final List<QueryGroupId> expected = Util.makeArrayList();

		{
			int count = 0;

			for (final String letter : alphabet) {
				expected.add(new QueryGroupId(letter, QueryGroupIdsIterator.cssClasses.get(count % QueryGroupIdsIterator.numCssClasses)));
				++count;
			}

			for (final String glyph : alphabet1) {
				expected.add(new QueryGroupId(glyph, QueryGroupIdsIterator.cssClasses.get(count % QueryGroupIdsIterator.numCssClasses)));
				++count;
			}

			for (final String glyph : alphabet2) {
				expected.add(new QueryGroupId(glyph, QueryGroupIdsIterator.cssClasses.get(count % QueryGroupIdsIterator.numCssClasses)));
				++count;
			}
		}

		final int howMany = 26 * 3;

		final List<QueryGroupId> ids = Util.take(howMany, new QueryGroupIdsIterator());

		assertEquals(expected, ids);

		assertEquals(expected.size(), ids.size());

		for (int i = 0; i < ids.size(); ++i) {
			assertEquals(expected.get(i).name, ids.get(i).name);
			assertEquals(expected.get(i).cssClass, ids.get(i).cssClass);
		}
	}
}
