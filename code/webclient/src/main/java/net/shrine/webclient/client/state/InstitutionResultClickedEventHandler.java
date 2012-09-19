package net.shrine.webclient.client.state;

import com.google.gwt.event.shared.EventHandler;

/**
 * @author Bill Simons
 * @date 9/18/12
 * @link http://cbmi.med.harvard.edu
 * @link http://chip.org
 * <p/>
 * NOTICE: This software comes with NO guarantees whatsoever and is
 * licensed as Lgpl Open Source
 * @link http://www.gnu.org/licenses/lgpl.html
 */
public interface InstitutionResultClickedEventHandler extends EventHandler {
    void handle(final InstitutionResultClickedEvent event);
}
