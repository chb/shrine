package net.shrine.webclient.client.util;

import java.util.List;

/**
 * 
 * @author clint
 * @date Mar 16, 2012
 */
public abstract class AbstractObservable implements IObservable {
	private final List<Observer> observers = Util.makeArrayList();

	public final void observedBy(final Observer observer) {
		observers.add(observer);
	}

	public final void noLongerObservedBy(final Observer observer) {
		observers.remove(observer);
	}

	public void notifyObservers() {
		for (final Observer observer : observers) {
			observer.inform();
		}
	}
}
