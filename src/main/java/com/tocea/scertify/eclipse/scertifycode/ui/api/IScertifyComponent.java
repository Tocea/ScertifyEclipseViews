
package com.tocea.scertify.eclipse.scertifycode.ui.api;


/**
 * Provide all Scertify specific informations providers and services to the checkstyle based UI views.
 * <p>
 * 
 * @author Antoine Floc'h
 * @since 25 févr. 2013
 * @copyright Copyright (C) 2013 - TOCEA
 */
public interface IScertifyComponent
{
    
    IScertifyLogger getLogger();
    
    IScertifyMarkersInformationsFactory getFactory();
    
    IScertifyAuditInformationsProvider getAuditInformations();
}
