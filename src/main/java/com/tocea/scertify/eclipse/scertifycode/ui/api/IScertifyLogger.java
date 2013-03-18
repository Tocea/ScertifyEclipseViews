
package com.tocea.scertify.eclipse.scertifycode.ui.api;


public interface IScertifyLogger
{
    boolean isLogging();
    
    public void log(Exception e, String message);
    
    public void log(Exception e);
}
