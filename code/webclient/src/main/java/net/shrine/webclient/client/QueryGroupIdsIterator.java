package net.shrine.webclient.client;

import java.util.Iterator;
import java.util.List;

import net.shrine.webclient.client.util.Util;

/**
 * 
 * @author clint
 * @date Mar 23, 2012
 */
public final class QueryGroupIdsIterator implements Iterator<QueryGroupId> {

	private int passNumber = 0;

	private char currentLabel = 'A';

	public QueryGroupIdsIterator() {
		super();
	}

	@Override
	public QueryGroupId next() {
		try {
			return new QueryGroupId(getName());
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
		
		return String.valueOf(currentLabel) + suffix;
	}

	public boolean hasNext() {
		return true;
	}

	public void remove() {
		throw new UnsupportedOperationException();
	}
}
