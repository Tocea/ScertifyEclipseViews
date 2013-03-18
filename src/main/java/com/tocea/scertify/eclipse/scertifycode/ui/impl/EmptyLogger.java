
package com.tocea.scertify.eclipse.scertifycode.ui.impl;


import com.tocea.scertify.eclipse.scertifycode.ui.api.IScertifyLogger;

public class EmptyLogger implements IScertifyLogger
{
    
    
    public final void log(Exception e, String message) {
    
    }
    
    
    public final void log(Exception e) {
    
    }
    
    
    public boolean isLogging() {
    
        return false;
    }
    
}
