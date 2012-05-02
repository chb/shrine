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

	private int countSofar = 0;

	static final List<String> cssClasses = makeCssClassNameList();

	static final int numCssClasses = 10;

	static List<String> makeCssClassNameList() {
		final List<String> result = Util.makeArrayList();

		for (int i = 1; i <= numCssClasses; ++i) {
			result.add("row" + i);
		}

		return result;
	}

	public QueryGroupIdsIterator() {
		super();
	}

	@Override
	public QueryGroupId next() {
		try {
			return new QueryGroupId(getName(), getCssClassName());
		} finally {
			++countSofar;
			
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

	String getCssClassName() {
		return cssClasses.get(countSofar % cssClasses.size());
	}

	public boolean hasNext() {
		return true;
	}

	public void remove() {
		throw new UnsupportedOperationException();
	}
}
