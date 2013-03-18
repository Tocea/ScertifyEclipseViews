// ============================================================================
//
// Copyright (C) 2002-2006 David Schneider, Lars K�dderitzsch, Fabrice Bellingard
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

package com.tocea.scertify.eclipse.scertifycode.ui.stats.views.internal;


import org.eclipse.jface.action.Action;

import com.tocea.scertify.eclipse.scertifycode.ui.ScertifyUIPluginImages;
import com.tocea.scertify.eclipse.scertifycode.ui.stats.Messages;
import com.tocea.scertify.eclipse.scertifycode.ui.stats.views.AbstractStatsView;

/**
 * Action implementation for the filters action.
 * 
 * @author Lars K�dderitzsch
 */
public class FiltersAction extends Action
{
    
    //
    // attributes
    //
    
    /** the view that uses this action. */
    private final AbstractStatsView mStatsView;
    
    //
    // constructor
    //
    
    /**
     * Creates the action.
     * 
     * @param view
     *            the stats view
     */
    public FiltersAction(final AbstractStatsView view) {
    
        super(Messages.FiltersAction_text);
        this.setImageDescriptor(ScertifyUIPluginImages.FILTER_ICON);
        this.setToolTipText(Messages.FiltersAction_tooltip);
        this.mStatsView = view;
        this.setEnabled(true);
    }
    
    //
    // methods
    //
    
    /**
     * Opens the dialog. Notifies the view if the filter has been modified.
     */
    @Override
    public void run() {
    
         mStatsView.openFiltersDialog();
    }
}
