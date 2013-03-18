
package com.tocea.scertify.eclipse.scertifycode.ui;


import java.util.Locale;

import org.eclipse.core.runtime.Platform;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import com.tocea.scertify.eclipse.scertifycode.ui.api.IScertifyAuditInformationsProvider;
import com.tocea.scertify.eclipse.scertifycode.ui.api.IScertifyComponent;
import com.tocea.scertify.eclipse.scertifycode.ui.api.IScertifyMarkersInformationsFactory;
import com.tocea.scertify.eclipse.scertifycode.ui.impl.EclipseInformationsComponent;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends AbstractUIPlugin
{
    
    /****************************************************************************************
     * STATIC
     */
    
    /** Identifier of the plug-in. */
    public static final String     PLUGIN_ID = "com.tocea.scertify.eclipse.scertifycode.ui.views"; //$NON-NLS-1$
                                                                                                   
    /** The shared instance. */
    private static Activator sPlugin;
    
    /****************************************************************************************
     * INSTANCE
     */
    private IScertifyComponent     scertify;
    
    public Activator() {
    
        super();
        this.scertify = new EclipseInformationsComponent();
        
        sPlugin = this;
    }
    
    /**
     * Returns the shared instance.
     * 
     * @return The shared plug-in instance.
     */
    public static Activator getDefault() {
    
        return sPlugin;
    }
    
    public IScertifyComponent getScertify() {
    
        return this.scertify;
    }
    
    public void setScertify(IScertifyComponent scertify) {
    
        this.scertify = scertify;
    }
    
    public IScertifyComponent getScertifyComponent() {
    
        return this.scertify;
    }
    
    public static IScertifyMarkersInformationsFactory markersInfoFactory() {
    
        return sPlugin.getScertifyComponent().getFactory();
    }
    
    public static IScertifyAuditInformationsProvider auditInfos() {
    
        return sPlugin.getScertifyComponent().getAuditInformations();
    }
    
    public static void log(Exception e, String message) {
    
        sPlugin.getScertifyComponent().getLogger().log(e, message);
    }
    
    public static boolean isLogging() {
    
        return sPlugin.getScertifyComponent().getLogger().isLogging();
    }
    
    public static void log(Exception e) {
    
        sPlugin.getScertifyComponent().getLogger().log(e);
    }
    
    /**
     * Helper method to get the current platform locale.
     * 
     * @return the platform locale
     */
    public static Locale getPlatformLocale() {
    
        final String nl = Platform.getNL();
        final String[] parts = nl.split("_"); //$NON-NLS-1$
        
        final String language = parts.length > 0 ? parts[0] : ""; //$NON-NLS-1$
        final String country = parts.length > 1 ? parts[1] : ""; //$NON-NLS-1$
        final String variant = parts.length > 2 ? parts[2] : ""; //$NON-NLS-1$
        
        return new Locale(language, country, variant);
    }
    
}
