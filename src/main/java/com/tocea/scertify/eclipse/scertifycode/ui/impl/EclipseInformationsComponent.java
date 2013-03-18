
package com.tocea.scertify.eclipse.scertifycode.ui.impl;


import com.tocea.scertify.eclipse.scertifycode.ui.api.IScertifyAuditInformationsProvider;
import com.tocea.scertify.eclipse.scertifycode.ui.api.IScertifyComponent;
import com.tocea.scertify.eclipse.scertifycode.ui.api.IScertifyLogger;
import com.tocea.scertify.eclipse.scertifycode.ui.api.IScertifyMarkersInformationsFactory;

public class EclipseInformationsComponent implements IScertifyComponent
{
    
    private IScertifyLogger                     logger;
    private IScertifyMarkersInformationsFactory factory;
    private IScertifyAuditInformationsProvider  auditInformations;
    
    public EclipseInformationsComponent() {
    
        super();
        this.logger = new EmptyLogger();
        this.factory = new EclipseMarkerInformationsProviderFactory();
        this.auditInformations = new EclipseAudiInformationsProvider();
    }
    
    public IScertifyLogger getLogger() {
    
        return logger;
    }
    
    public IScertifyMarkersInformationsFactory getFactory() {
    
        return factory;
    }
    
    public IScertifyAuditInformationsProvider getAuditInformations() {
    
        return auditInformations;
    }
    
}
