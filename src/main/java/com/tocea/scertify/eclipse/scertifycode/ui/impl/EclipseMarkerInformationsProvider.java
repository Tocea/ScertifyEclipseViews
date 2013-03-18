
package com.tocea.scertify.eclipse.scertifycode.ui.impl;


import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.ui.texteditor.MarkerUtilities;

import com.tocea.scertify.eclipse.scertifycode.ui.api.IScertifyMarkerInformationsProvider;

public class EclipseMarkerInformationsProvider implements IScertifyMarkerInformationsProvider
{
    
    private IMarker marker;
    
    public EclipseMarkerInformationsProvider(IMarker marker) {
    
        super();
        this.marker = marker;
    }
    
    public final boolean isScertify() {
    
        return true;
    }
    
    public IMarker getEclipseMarker() {
    
        return marker;
    }
    
    public String getType() {
    
        try {
            return marker.getType();
        } catch (final CoreException e) {
            return "";
        }
    }
    
    public String getDescription() {
    
        return MarkerUtilities.getMessage(marker);
    }
    
    public String getRuleIdentifier() {
    
        return "identifier";
    }
    
    public String getCriticity() {
    
        int severity = MarkerUtilities.getSeverity(marker);
        switch (severity) {
            case IMarker.SEVERITY_WARNING:
                return "Warning";
            case IMarker.SEVERITY_INFO:
                return "Info";
            case IMarker.SEVERITY_ERROR:
                return "Error";
        }
        return "";
    }
    
    public String getFamily() {
    
        // TODO Auto-generated method stub
        return null;
    }
    
    public int getSeverity() {
    
        return MarkerUtilities.getSeverity(marker);
    }
    
    
    public int getEclipseSeverity() {
    
        return getSeverity();
    }
}
