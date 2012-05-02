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

	public abstract int size();

	public abstract boolean isEmpty();

	public abstract boolean contains(final Object o);

	public abstract Iterator<T> iterator();

	public abstract Object[] toArray();

	public abstract <A> A[] toArray(final A[] a);

	public abstract boolean containsAll(final Collection<?> c);

	public abstract T get(final int index);

	public abstract int indexOf(final Object o);

	public abstract int lastIndexOf(final Object o);

	public abstract ListIterator<T> listIterator();

	public abstract ListIterator<T> listIterator(final int index);

	public abstract List<T> subList(final int fromIndex, final int toIndex);

}