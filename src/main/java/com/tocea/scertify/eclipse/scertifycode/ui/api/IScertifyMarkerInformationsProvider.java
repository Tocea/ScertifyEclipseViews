
package com.tocea.scertify.eclipse.scertifycode.ui.api;


import org.eclipse.core.resources.IMarker;

/**
 * This type describe an interface that provide the informations of any Scertify marker.
 * <p>
 * 
 * @author Antoine Floc'h
 * @since 25 f�vr. 2013
 * @copyright Copyright (C) 2013 - TOCEA
 */
public interface IScertifyMarkerInformationsProvider
{
    
    /**
     * Check that the marker has been produced by Scertify.
     * 
     * @return
     */
    boolean isScertify();
    
    /**
     * Get the eclipse marker bound to this provider.
     * 
     * @return
     */
    IMarker getEclipseMarker();
    
    /**
     * Get the type of the marker.
     * 
     * @return
     */
    String getType();
    
    /**
     * Get the description of the rule instance.
     * 
     * @return
     */
    String getDescription();
    
    /**
     * Get the identifier of the marker rule.
     * 
     * @return
     */
    String getRuleIdentifier();
    
    /**
     * Get the criticity of the marker rule.
     * 
     * @return
     */
    String getCriticity();
    
    /**
     * Get the severity of the marker rule.
     * 
     * @return
     */
    int getSeverity();
    
    int getEclipseSeverity();
    
}
