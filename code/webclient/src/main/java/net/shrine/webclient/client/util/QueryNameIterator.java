package net.shrine.webclient.client.util;

import java.util.Iterator;

/**
 * 
 * @author clint
 * @date Mar 23, 2012
 */
public final class QueryNameIterator implements Iterator<String> {

	private int passNumber = 0;

	private char currentLabel = 'A';

	public QueryNameIterator() {
		super();
	}

	@Override
	public String next() {
		try {
			return getName();
		} finally {
			if (currentLabel == 'Z') {
				currentLabel = 'A';
				++passNumber;
			} else {
				++currentLabel;
			}
		}
	}

	String getName() {
		final String suffix = (passNumber == 0 ? "" : String.valueOf(passNumber));
		
		return currentLabel + suffix;
	}

	public boolean hasNext() {
		return true;
	}

	public void remove() {
		throw new UnsupportedOperationException();
	}
}
