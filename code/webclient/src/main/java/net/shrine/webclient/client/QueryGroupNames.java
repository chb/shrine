package net.shrine.webclient.client;

import java.util.Iterator;

/**
 * 
 * @author clint
 * @date Mar 23, 2012
 */
public final class QueryGroupNames {
	public QueryGroupNames() {
		super();
	}

	public final String next() {
		return names.next();
	}

	Iterator<String> getNamesIterator() {
		return names;
	}
	
	private int i = 0;
	
	private char current = 'A';

	private final Iterator<String> names = new Iterator<String>() {
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
