
package com.tocea.scertify.eclipse.scertifycode.ui.api;


import org.eclipse.core.resources.IMarker;

/**
 * Factory in charge of the instantiation of the Scertify markers informations for any Eclipse marker.
 * <p>
 * 
 * @author Antoine Floc'h
 * @since 25 févr. 2013
 * @copyright Copyright (C) 2013 - TOCEA
 */
public interface IScertifyMarkersInformationsFactory
{
    
    /**
     * Creates a new marker informations provider from an existing Eclispe marker.
     * 
     * @param marker
     * @return
     */
    IScertifyMarkerInformationsProvider newInformationsProvider(IMarker marker);
}
