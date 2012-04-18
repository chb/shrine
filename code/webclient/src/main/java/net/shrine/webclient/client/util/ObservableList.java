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
public final class ObservableList<T> extends AbstractObservable implements List<T> {
	private final List<T> delegate;

	public ObservableList(final List<T> delegate) {
		super();

		Util.requireNotNull(delegate);

		this.delegate = delegate;
	}

	public static final <T> ObservableList<T> empty() {
		return new ObservableList<T>(new ArrayList<T>());
	}

	public int size() {
		return delegate.size();
	}

	public boolean isEmpty() {
		return delegate.isEmpty();
	}

	public boolean contains(final Object o) {
		return delegate.contains(o);
	}

	public Iterator<T> iterator() {
		return delegate.iterator();
	}

	public Object[] toArray() {
		return delegate.toArray();
	}

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

	public int indexOf(final Object o) {
		return delegate.indexOf(o);
	}

	public int lastIndexOf(final Object o) {
		return delegate.lastIndexOf(o);
	}

	public ListIterator<T> listIterator() {
		return delegate.listIterator();
	}

	public ListIterator<T> listIterator(final int index) {
		return delegate.listIterator(index);
	}

	public List<T> subList(final int fromIndex, final int toIndex) {
		return delegate.subList(fromIndex, toIndex);
	}
}
