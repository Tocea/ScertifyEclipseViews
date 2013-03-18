// ============================================================================
//
// Copyright (C) 2002-2011 David Schneider, Lars K�dderitzsch
//
// This library is free software; you can redistribute it and/or
// modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either
// version 2.1 of the License, or (at your option) any later version.
//
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
// Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public
// License along with this library; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
//
// ============================================================================

package com.tocea.scertify.eclipse.scertifycode.ui.util;


import java.lang.reflect.Method;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.DialogSettings;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseTrackListener;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

/**
 * Some widely used helper funktionality regarding SWT shortcomings.
 * 
 * @author Lars K�dderitzsch
 */
public final class SWTUtil
{
    
    /**
     * Verifier that allows only digits to be input.
     * 
     * @author Lars K�dderitzsch
     */
    private static final class OnlyDigitsVerifyListener implements VerifyListener
    {
        
        /**
         * @see org.eclipse.swt.events.VerifyListener#verifyText(org.eclipse.swt.events.VerifyEvent)
         */
        
        public void verifyText(VerifyEvent e) {
        
            boolean doit = true;
            
            // only let digits pass (and del, backspace)
            if (!(Character.isDigit(e.character) || e.character == SWT.DEL || e.character == SWT.BS)) {
                doit = false;
            }
            
            // check if inserted text is an integer
            if (!doit) {
                try {
                    Integer.parseInt(e.text);
                    doit = true;
                } catch (final NumberFormatException ex) {
                    doit = false;
                }
            }
            
            e.doit = doit;
            if (!e.doit) {
                Display.getCurrent().beep();
            }
        }
    }
    
    /**
     * Listener that adds resize support ((re)storing of size and location
     * information).
     * 
     * @author Lars K�dderitzsch
     */
    private static final class ShellResizeSupportListener extends ShellAdapter implements ControlListener,
            DisposeListener
    {
        
        //
        // constants
        //
        
        /** constant for the x location key. */
        private static final String   X                  = "x";        //$NON-NLS-1$
                                                                        
        /** constant for the y location key. */
        private static final String   Y                  = "y";        //$NON-NLS-1$
                                                                        
        /** constant for the width key. */
        private static final String   WIDTH              = "width";    //$NON-NLS-1$
                                                                        
        /** constant for the height key. */
        private static final String   HEIGHT             = "height";   //$NON-NLS-1$
                                                                        
        /** constant for the maximized key. */
        private static final String   MAXIMIZED          = "maximized"; //$NON-NLS-1$
                                                                        
        /** constant for the minimized key. */
        private static final String   MINIMIZED          = "minimized"; //$NON-NLS-1$
                                                                        
        //
        // attributes
        //
        
        /** the current bounds of the dialog shell. */
        private Rectangle             mNewBounds;
        
        /** the maximized state of the shell. */
        private boolean               mMaximized;
        
        /** the minmized state of the shell. */
        private boolean               mMinimized;
        
        /** the dialog. */
        private final Dialog          mDialog;
        
        /** the plugins dialog settings instance. */
        private final IDialogSettings mSettings;
        
        /** the dialogs unique key. */
        private final String          mDialogKey;
        
        /** flag that indicates if the dialog was already initally activated. */
        private boolean               mInitialyActivated = false;
        
        //
        // constructors
        //
        
        /**
         * Creates the resize support listener.
         * 
         * @param settings
         *            the dialog settings instance for the plugin
         * @param dialogKey
         *            the unique key of the dialog
         */
        public ShellResizeSupportListener(Dialog dialog, IDialogSettings settings, String dialogKey) {
        
            this.mDialog = dialog;
            this.mSettings = settings;
            this.mDialogKey = dialogKey;
        }
        
        //
        // methods
        //
        
        /**
         * @see ControlListener#controlMoved(org.eclipse.swt.events.ControlEvent)
         */
        
        public void controlMoved(ControlEvent e) {
        
            this.controlResized(e);
        }
        
        /**
         * @see ControlListener#controlResized(org.eclipse.swt.events.ControlEvent)
         */
        
        public void controlResized(ControlEvent e) {
        
            // update the internal bounds
            final Shell shell = (Shell) e.getSource();
            this.mMaximized = shell.getMaximized();
            this.mMinimized = shell.getMinimized();
            
            // only store new bounds if the shell is not minimized or maximized.
            // This way the original size (before minimizing/maximizing will be
            // remembered.
            if (!this.mMinimized && !this.mMaximized) {
                this.mNewBounds = shell.getBounds();
            }
        }
        
        /**
         * @see org.eclipse.swt.events.ShellListener#shellActivated(org.eclipse.swt.events.ShellEvent)
         */
        
        public void shellActivated(ShellEvent e) {
        
            // do only on initial activation (aka when the dialog is opened)
            if (!this.mInitialyActivated) {
                
                Point initialSize = null;
                
                // hack to get the initial size computed for this dialog
                try {
                    final Method getInitialSizeMethod = Window.class.getDeclaredMethod("getInitialSize", //$NON-NLS-1$
                            (Class<?>) null);
                    getInitialSizeMethod.setAccessible(true);
                    initialSize = (Point) getInitialSizeMethod.invoke(this.mDialog, (Object) null);
                } catch (final Exception e1) {
                    initialSize = new Point(0, 0);
                }
                final Shell shell = (Shell) e.getSource();
                
                // this is only supported in Eclipse 3.1 and greater
                // shell.setMinimumSize(initialSize);
                
                final IDialogSettings bounds = this.mSettings.getSection(this.mDialogKey);
                if (bounds != null) {
                    
                    this.mMaximized = bounds.getBoolean(MAXIMIZED);
                    this.mMinimized = bounds.getBoolean(MINIMIZED);
                    
                    // restore the size from the dialog settings
                    try {
                        this.mNewBounds =
                                new Rectangle(bounds.getInt(X), bounds.getInt(Y), bounds.getInt(WIDTH),
                                        bounds.getInt(HEIGHT));
                    } catch (final NumberFormatException ex) {
                        this.mNewBounds = shell.getBounds();
                    }
                    
                    shell.removeControlListener(this);
                    
                    if (this.mNewBounds.width < initialSize.x) {
                        this.mNewBounds.width = initialSize.x;
                    }
                    if (this.mNewBounds.height < initialSize.y) {
                        this.mNewBounds.height = initialSize.y;
                    }
                    
                    shell.setBounds(this.mNewBounds);
                    
                    if (this.mMaximized) {
                        shell.setMaximized(true);
                    } else if (this.mMinimized) {
                        shell.setMinimized(true);
                    }
                    shell.addControlListener(this);
                    
                }
                
                this.mInitialyActivated = true;
            }
        }
        
        /**
         * @see DisposeListener#widgetDisposed(org.eclipse.swt.events.DisposeEvent)
         */
        
        public void widgetDisposed(DisposeEvent e) {
        
            // store the dialog size and location to the settings
            IDialogSettings bounds = this.mSettings.getSection(this.mDialogKey);
            if (bounds == null) {
                bounds = new DialogSettings(this.mDialogKey);
                this.mSettings.addSection(bounds);
            }
            
            if (this.mNewBounds != null) {
                bounds.put(WIDTH, this.mNewBounds.width);
                bounds.put(HEIGHT, this.mNewBounds.height);
                bounds.put(X, this.mNewBounds.x);
                bounds.put(Y, this.mNewBounds.y);
            }
            
            bounds.put(MAXIMIZED, this.mMaximized);
            bounds.put(MINIMIZED, this.mMinimized);
        }
    }
    
    /**
     * Listener that adds tooltip-on-press support.
     * 
     * @author Lars K�dderitzsch
     */
    private static final class TooltipOnPressListener extends MouseAdapter implements MouseTrackListener
    {
        
        /**
         * @see org.eclipse.swt.events.MouseListener#mouseDown(org.eclipse.swt.events.MouseEvent)
         */
        
        public void mouseDown(MouseEvent e) {
        
            final Control theControl = (Control) e.widget;
            
            final Display display = theControl.getDisplay();
            final Shell tip = new Shell(theControl.getShell(), SWT.ON_TOP | SWT.TOOL);
            tip.setBackground(display.getSystemColor(SWT.COLOR_INFO_BACKGROUND));
            final FillLayout layout = new FillLayout();
            layout.marginHeight = 1;
            layout.marginWidth = 2;
            tip.setLayout(layout);
            final Label label = new Label(tip, SWT.NONE);
            label.setForeground(display.getSystemColor(SWT.COLOR_INFO_FOREGROUND));
            label.setBackground(display.getSystemColor(SWT.COLOR_INFO_BACKGROUND));
            
            label.setText(theControl.getToolTipText());
            label.addMouseTrackListener(this);
            final Point size = tip.computeSize(SWT.DEFAULT, SWT.DEFAULT);
            final Rectangle rect = theControl.getBounds();
            final Point pt = theControl.getParent().toDisplay(rect.x, rect.y);
            tip.setBounds(pt.x, pt.y, size.x, size.y);
            tip.setVisible(true);
        }
        
        /**
         * @see org.eclipse.swt.events.MouseTrackListener#mouseEnter(org.eclipse.swt.events.MouseEvent)
         */
        
        public void mouseEnter(MouseEvent e) {
        
            // NOOP
        }
        
        /**
         * @see org.eclipse.swt.events.MouseTrackListener#mouseExit(org.eclipse.swt.events.MouseEvent)
         */
        
        public void mouseExit(MouseEvent e) {
        
            // dispose the tooltip shell
            final Label label = (Label) e.widget;
            final Shell shell = label.getShell();
            shell.dispose();
        }
        
        /**
         * @see org.eclipse.swt.events.MouseTrackListener#mouseHover(org.eclipse.swt.events.MouseEvent)
         */
        
        public void mouseHover(MouseEvent e) {
        
            // NOOP
        }
    }
    
    /**
     * Adds a verifier to the given text control which allows only digits to be
     * entered.
     * 
     * @param text
     *            the text control
     */
    public static void addOnlyDigitInputSupport(Text text) {
    
        text.addVerifyListener(new OnlyDigitsVerifyListener());
    }
    
    /**
     * Adds support to resizable dialogs for (re)storing the dialog size.
     * 
     * @param dialog
     *            the dialog to add support to
     * @param settings
     *            the dialog settings to store the size in
     * @param dialogKey
     *            the unique key for the dialog
     */
    public static void addResizeSupport(Dialog dialog, IDialogSettings settings, String dialogKey) {
    
        final Shell shell = dialog.getShell();
        final ShellResizeSupportListener shellSupport = new ShellResizeSupportListener(dialog, settings, dialogKey);
        
        shell.addControlListener(shellSupport);
        shell.addShellListener(shellSupport);
        shell.addDisposeListener(shellSupport);
    }
    
    /**
     * Adds support to a control which shows the tooltip of the control when the
     * mouse button is pressed on it.
     * 
     * @param control
     *            the control
     */
    public static void addTooltipOnPressSupport(Control control) {
    
        control.addMouseListener(new TooltipOnPressListener());
    }
    
    /**
     * Hidden default constructor.
     */
    private SWTUtil() {
    
        // NOOP
    }
}
