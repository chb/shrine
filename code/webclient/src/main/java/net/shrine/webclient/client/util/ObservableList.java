package net.shrine.webclient.client.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

/**
 * 
 * @author clint
 * @date Mar 23, 2012
 */
public final class ObservableList<T> extends AbstractObservable implements List<T>, ReadOnlyObservableList<T> {
	private final List<T> delegate;

	public ObservableList(final List<T> delegate) {
		super();

		Util.requireNotNull(delegate);

		this.delegate = delegate;
	}

	public static <T> ObservableList<T> empty() {
		return new ObservableList<T>(new ArrayList<T>());
	}

	/* (non-Javadoc)
	 * @see net.shrine.webclient.client.util.ReadOnlyObservableList#size()
	 */
	@Override
	public int size() {
		return delegate.size();
	}

	/* (non-Javadoc)
	 * @see net.shrine.webclient.client.util.ReadOnlyObservableList#isEmpty()
	 */
	@Override
	public boolean isEmpty() {
		return delegate.isEmpty();
	}

	/* (non-Javadoc)
	 * @see net.shrine.webclient.client.util.ReadOnlyObservableList#contains(java.lang.Object)
	 */
	@Override
	public boolean contains(final Object o) {
		return delegate.contains(o);
	}

	/* (non-Javadoc)
	 * @see net.shrine.webclient.client.util.ReadOnlyObservableList#iterator()
	 */
	@Override
	public Iterator<T> iterator() {
		return delegate.iterator();
	}

	/* (non-Javadoc)
	 * @see net.shrine.webclient.client.util.ReadOnlyObservableList#toArray()
	 */
	@Override
	public Object[] toArray() {
		return delegate.toArray();
	}

	/* (non-Javadoc)
	 * @see net.shrine.webclient.client.util.ReadOnlyObservableList#toArray(A[])
	 */
	@Override
	public <A> A[] toArray(final A[] a) {
		return delegate.toArray(a);
	}

	public boolean add(final T e) {
		try {
			return delegate.add(e);
		} finally {
			notifyObservers();
		}
	}

	public boolean remove(final Object o) {
		try {
			return delegate.remove(o);
		} finally {
			notifyObservers();
		}
	}

	/* (non-Javadoc)
	 * @see net.shrine.webclient.client.util.ReadOnlyObservableList#containsAll(java.util.Collection)
	 */
	@Override
	public boolean containsAll(final Collection<?> c) {
		return delegate.containsAll(c);
	}

	public boolean addAll(final Collection<? extends T> c) {
		try {
			return delegate.addAll(c);
		} finally {
			notifyObservers();
		}
	}

	public boolean addAll(final int index, final Collection<? extends T> c) {
		try {
			return delegate.addAll(index, c);
		} finally {
			notifyObservers();
		}
	}

	public boolean removeAll(final Collection<?> c) {
		try {
			return delegate.removeAll(c);
		} finally {
			notifyObservers();
		}
	}

	public boolean retainAll(final Collection<?> c) {
		try {
			return delegate.retainAll(c);
		} finally {
			notifyObservers();
		}
	}

	public void clear() {
		try {
			delegate.clear();
		} finally {
			notifyObservers();
		}
	}

	@Override
	public boolean equals(final Object o) {
		return delegate.equals(o);
	}

	@Override
	public int hashCode() {
		return delegate.hashCode();
	}

	/* (non-Javadoc)
	 * @see net.shrine.webclient.client.util.ReadOnlyObservableList#get(int)
	 */
	@Override
	public T get(final int index) {
		return delegate.get(index);
	}

	public T set(final int index, final T element) {
		try {
			return delegate.set(index, element);
		} finally {
			notifyObservers();
		}
	}

	public void add(final int index, final T element) {
		try {
			delegate.add(index, element);
		} finally {
			notifyObservers();
		}
	}

	public T remove(final int index) {
		try {
			return delegate.remove(index);
		} finally {
			notifyObservers();
		}
	}

	/* (non-Javadoc)
	 * @see net.shrine.webclient.client.util.ReadOnlyObservableList#indexOf(java.lang.Object)
	 */
	@Override
	public int indexOf(final Object o) {
		return delegate.indexOf(o);
	}

	/* (non-Javadoc)
	 * @see net.shrine.webclient.client.util.ReadOnlyObservableList#lastIndexOf(java.lang.Object)
	 */
	@Override
	public int lastIndexOf(final Object o) {
		return delegate.lastIndexOf(o);
	}

	/* (non-Javadoc)
	 * @see net.shrine.webclient.client.util.ReadOnlyObservableList#listIterator()
	 */
	@Override
	public ListIterator<T> listIterator() {
		return delegate.listIterator();
	}

	/* (non-Javadoc)
	 * @see net.shrine.webclient.client.util.ReadOnlyObservableList#listIterator(int)
	 */
	@Override
	public ListIterator<T> listIterator(final int index) {
		return delegate.listIterator(index);
	}

	/* (non-Javadoc)
	 * @see net.shrine.webclient.client.util.ReadOnlyObservableList#subList(int, int)
	 */
	@Override
	public List<T> subList(final int fromIndex, final int toIndex) {
		return delegate.subList(fromIndex, toIndex);
	}

	@Override
	public String toString() {
		return delegate.toString();
	}
}
