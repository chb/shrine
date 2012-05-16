package net.shrine.webclient.client.util;

/**
 * 
 * @author clint
 * @date May 15, 2012
 */
public abstract class SimpleObserver implements Observer {
	protected final IObservable observed;

	public SimpleObserver(final IObservable observed) {
		super();
		
		Util.requireNotNull(observed);
		
		this.observed = observed;
		
		this.observed.observedBy(this);
	}

	@Override
	public void stopObserving() {
		this.observed.noLongerObservedBy(this);
	}
}
