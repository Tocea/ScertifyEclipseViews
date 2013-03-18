package com.tocea.scertify.eclipse.scertifycode.ui.impl;

import org.eclipse.core.resources.IMarker;

import com.tocea.scertify.eclipse.scertifycode.ui.api.IScertifyMarkerInformationsProvider;
import com.tocea.scertify.eclipse.scertifycode.ui.api.IScertifyMarkersInformationsFactory;


public class EclipseMarkerInformationsProviderFactory implements IScertifyMarkersInformationsFactory
{


    public IScertifyMarkerInformationsProvider newInformationsProvider(IMarker marker) {
    
       return new EclipseMarkerInformationsProvider(marker);
    }
    
}
