package net.shrine.webclient.client.util;

/**
 * 
 * @author clint
 * @date Apr 18, 2012
 */
public final class MockObserver implements Observer {
	final IObservable observed;

	public boolean informed = false;
	
	MockObserver(final IObservable observed) {
		super();
		
		Util.requireNotNull(observed);
		
		this.observed = observed;
		
		this.observed.observedBy(this);
	}

	@Override
	public void inform() {
		informed = true;
	}

	@Override
	public void stopObserving() {
		this.observed.noLongerObservedBy(this);
	}
}
