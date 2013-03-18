
package com.tocea.scertify.eclipse.scertifycode.ui.api;


import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IMarkerDelta;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;

public interface IScertifyAuditInformationsProvider
{
    

    int getPreviousNumberOfViolations(int depth,IResource ...resources);
    
    IMarker[] findScertifyMarkers(IResource resource, int depth);
    
    IMarkerDelta[] findScertifyMarkerDeltas(IResourceChangeEvent event);
}
