package net.shrine.webclient.client;

import java.util.Arrays;
import java.util.List;

import net.shrine.webclient.client.util.Util;

import org.junit.Test;

/**
 * 
 * @author clint
 * @date Apr 19, 2012
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

		final List<String> expected = Util.makeArrayList();

		expected.addAll(alphabet);
		expected.addAll(alphabet1);
		expected.addAll(alphabet2);

		final int howMany = 26 * 3;

		assertEquals(expected, Util.take(howMany, QueryGroupNames.getNamesIterator()));
	}
}
