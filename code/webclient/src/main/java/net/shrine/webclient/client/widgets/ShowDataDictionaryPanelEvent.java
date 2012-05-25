package net.shrine.webclient.client.widgets;

import net.shrine.webclient.client.domain.Term;

import com.google.gwt.event.shared.GwtEvent;

/**
 * 
 * @author clint
 * @date Apr 5, 2012
 */
public final class ShowDataDictionaryPanelEvent extends GwtEvent<ShowDataDictionaryPanelEventHandler> {

	private static final GwtEvent.Type<ShowDataDictionaryPanelEventHandler> TYPE = WidgetUtil.eventType();

	private final Term startingTerm;

	public ShowDataDictionaryPanelEvent(final Term startingTerm) {
		super();

		this.startingTerm = startingTerm;
	}

	public static final GwtEvent.Type<ShowDataDictionaryPanelEventHandler> getType() {
		return TYPE;
	}

	// NB: must return the singleton TYPE instance; returning a new instance of
	// GwtEvent.Type<ShowBrowsePopupEventHandler> makes handlers not get called.
	// :(
	@Override
	public GwtEvent.Type<ShowDataDictionaryPanelEventHandler> getAssociatedType() {
		return TYPE;
	}

	@Override
	protected void dispatch(final ShowDataDictionaryPanelEventHandler handler) {
		handler.handle(this);
	}

	public Term getStartingTerm() {
		return startingTerm;
	}

	@Override
	public String toString() {
		return "ShowBrowsePopupEvent [startingTerm=" + startingTerm + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (startingTerm == null ? 0 : startingTerm.hashCode());
		return result;
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final ShowDataDictionaryPanelEvent other = (ShowDataDictionaryPanelEvent) obj;
		if (startingTerm == null) {
			if (other.startingTerm != null) {
				return false;
			}
		} else if (!startingTerm.equals(other.startingTerm)) {
			return false;
		}
		return true;
	}
}
