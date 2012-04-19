package net.shrine.webclient.client;

import java.util.Iterator;

/**
 * 
 * @author clint
 * @date Mar 23, 2012
 */
public final class QueryGroupNames {
	public static final String All = "All";
	
	private QueryGroupNames() {
		super();
	}

	public static final String next() {
		return names.next();
	}

	private static int i = 0;
	private static char current = 'A';

	//For tests
	public static Iterator<String> getNamesIterator() {
		return new Iterator<String>() {
			public boolean hasNext() {
				return true;
			}

			public String next() {
				try {
					final String suffix = (i == 0 ? "" : String.valueOf(i));

					return String.valueOf(current) + suffix;
				} finally {
					if (current == 'Z') {
						current = 'A';
						++i;
					} else {
						++current;
					}
				}
			}

			public void remove() {
				throw new UnsupportedOperationException();
			}
		};
	}
	
	private static Iterator<String> names = getNamesIterator();
	
	public static void reset() { 
		names = getNamesIterator();
	}
}
