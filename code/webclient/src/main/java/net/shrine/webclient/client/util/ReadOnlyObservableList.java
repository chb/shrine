package net.shrine.webclient.client.util;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

/**
 * 
 * @author clint
 * @date May 1, 2012
 * @param <T>
 */
public interface ReadOnlyObservableList<T> extends IObservable, Iterable<T> {

	int size();

	boolean isEmpty();

	boolean contains(final Object o);

	Iterator<T> iterator();

	Object[] toArray();

	<A> A[] toArray(final A[] a);

	boolean containsAll(final Collection<?> c);

	T get(final int index);

	int indexOf(final Object o);

	int lastIndexOf(final Object o);

	ListIterator<T> listIterator();

	ListIterator<T> listIterator(final int index);

	List<T> subList(final int fromIndex, final int toIndex);

}