package net.shrine.webclient.client.widgets;

import net.shrine.webclient.shared.Versions;

import com.allen_sauer.gwt.log.client.Log;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.SpanElement;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Cookies;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;

/**
 * 
 * @author clint
 * @date Aug 31, 2012
 */
public final class VersionsPanel extends Composite {

    private static VersionsPanelUiBinder uiBinder = GWT.create(VersionsPanelUiBinder.class);

    interface VersionsPanelUiBinder extends UiBinder<Widget, VersionsPanel> { }

    @UiField
    SpanElement versionSpan;
    
    @UiField
    SpanElement revisionSpan;
    
    @UiField
    SpanElement branchSpan;
    
    @UiField
    SpanElement buildDateSpan;
    
    public VersionsPanel() {
        initWidget(uiBinder.createAndBindUi(this));
        
        final Versions versions = getVersions();
        
        versionSpan.setInnerText(versions.appVersion);
        revisionSpan.setInnerText(versions.revision);
        branchSpan.setInnerText(versions.branch);
        buildDateSpan.setInnerText(versions.buildDate);
    }
    
    private static Versions getVersions() {
        final String versionCookieName = net.shrine.webclient.shared.Cookies.Version.cookieName;
        
        try { 
            final String versionsCookie = Cookies.getCookie(versionCookieName);
            
            return Versions.fromString(versionsCookie);
        } catch (Exception e) {
            Log.warn("Error determining application versions. Is the '" + versionCookieName + "' cookie set?  Cookie value: '" + Cookies.getCookie(versionCookieName) + "'");
            
            return Versions.Unknown;
        }
    }
}
