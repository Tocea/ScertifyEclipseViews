// ============================================================================
//
// Copyright (C) 2002-2011 David Schneider, Lars K�dderitzsch, Fabrice Bellingard
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

package com.tocea.scertify.eclipse.scertifycode.ui.util.table;


import java.text.Collator;

import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.viewers.ColumnPixelData;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

import com.tocea.scertify.eclipse.scertifycode.ui.Activator;


/**
 * This subclass of <code>TableViewer</code> adds easier sorting support and
 * support for storing table settings (column width, sorter state).
 * 
 * @author Lars K�dderitzsch
 */
public class EnhancedTableViewer extends TableViewer
{
    
    //
    // constants
    //
    
    /**
     * Listener for header clicks and resize events.
     * 
     * @author Lars K�dderitzsch
     */
    private class TableListener implements SelectionListener, ControlListener
    {
        
        
        public void controlMoved(ControlEvent e) {
        
            // NOOP
        }
        
        
        public void controlResized(ControlEvent e) {
        
            if (e.getSource() instanceof TableColumn) {
                EnhancedTableViewer.this.saveState();
            }
        }
        
        
        public void widgetDefaultSelected(SelectionEvent e) {
        
            // NOOP
        }
        
        
        public void widgetSelected(SelectionEvent e) {
        
            if (e.getSource() instanceof TableColumn) {
                
                final TableColumn col = (TableColumn) e.getSource();
                final Table table = col.getParent();
                
                final int colIndex = table.indexOf(col);
                
                if (colIndex == EnhancedTableViewer.this.mSortedColumnIndex) {
                    EnhancedTableViewer.this.mSortDirection =
                            EnhancedTableViewer.this.mSortDirection * DIRECTION_REVERSE;
                } else {
                    EnhancedTableViewer.this.mSortedColumnIndex = colIndex;
                    EnhancedTableViewer.this.mSortDirection = DIRECTION_FORWARD;
                }
                
                EnhancedTableViewer.this.resort();
                EnhancedTableViewer.this.saveState();
            }
        }
    }
    
    /**
     * Sorter implementation that uses the values provided by the comparable
     * provider to sort the table.
     * 
     * @author Lars K�dderitzsch
     */
    private class TableSorter extends ViewerSorter
    {
        
        /**
         * {@inheritDoc}
         */
        
        public int compare(Viewer viewer, Object e1, Object e2) {
        
            final Comparable c1 =
                    EnhancedTableViewer.this.mComparableProvider.getComparableValue(e1,
                            EnhancedTableViewer.this.mSortedColumnIndex);
            final Comparable c2 =
                    EnhancedTableViewer.this.mComparableProvider.getComparableValue(e2,
                            EnhancedTableViewer.this.mSortedColumnIndex);
            
            int compareResult = 0;
            
            // support for string collation
            if (c1 instanceof String && c2 instanceof String) {
                compareResult = COLLATOR.compare(c1, c2);
            } else {
                compareResult = c1.compareTo(c2);
            }
            
            // take sort direction into account
            return compareResult * EnhancedTableViewer.this.mSortDirection;
        }
    }
    
    /** Key for the column index in the persistence store. */
    private static final String      TAG_COLUMN_INDEX      = "sortColumn";               //$NON-NLS-1$
                                                                                          
    /** Key for the sort direction in the persistence store. */
    private static final String      TAG_SORT_DIRECTION    = "sortDirection";            //$NON-NLS-1$
                                                                                          
    /** Key for the widths in the persistence store. */
    private static final String      TAG_COLUMN_WIDTH      = "colWidth";                 //$NON-NLS-1$
                                                                                          
    /** Key for the selection index in the persistence store. */
    private static final String      TAG_CURRENT_SELECTION = "selectedRow";              //$NON-NLS-1$
                                                                                          
    /** Integer constant for the forward sort direction value. */
    private static final int         DIRECTION_FORWARD     = 1;
    
    //
    // attributes
    //
    
    /** Integer constant for the reverse sort direction value. */
    private static final int         DIRECTION_REVERSE     = -1;
    
    /** Collator to support natural sorting of strings. */
    private static final Collator    COLLATOR              = Collator.getInstance(Activator
                                                                   .getPlatformLocale());
    
    /** the comparable provider for sorting support. */
    private ITableComparableProvider mComparableProvider;
    
    /** the settings provider. */
    private ITableSettingsProvider   mSettingsProvider;
    
    /** The index of the current sorted column. */
    private int                      mSortedColumnIndex;
    
    //
    // constructors
    //
    
    /** The sort direction. */
    private int                      mSortDirection        = DIRECTION_FORWARD;
    
    /** The table listener. */
    private final TableListener      mTableListener        = new TableListener();
    
    /**
     * Creates the EnhancedTableViewer within the given parent composite.
     * 
     * @param parent
     *            parent composite
     */
    public EnhancedTableViewer(Composite parent) {
    
        super(parent);
    }
    
    //
    // methods
    //
    
    /**
     * Creates the EnhancedTableViewer for the given parent and style.
     * 
     * @param parent
     *            parent composite
     * @param style
     *            the style
     */
    public EnhancedTableViewer(Composite parent, int style) {
    
        super(parent, style);
    }
    
    /**
     * Creates the EnhancedTableViewer for the given table.
     * 
     * @param table
     *            the table to create the viewer for
     */
    public EnhancedTableViewer(Table table) {
    
        super(table);
        
        // table.setLayout(new TableLayout()
        // {});
    }
    
    /**
     * Returns the comparable provider.
     * 
     * @return the comparable provider
     */
    public ITableComparableProvider getTableComparableProvider() {
    
        return this.mComparableProvider;
    }
    
    /**
     * Returns the settings provider.
     * 
     * @return the settings provider
     */
    public ITableSettingsProvider getTableSettingsProvider() {
    
        return this.mSettingsProvider;
    }
    
    /**
     * This method installs the enhancements over the standard TableViewer. This
     * method must be called only after all columns are set up for the
     * associated table.
     */
    public void installEnhancements() {
    
        this.getTable().removeSelectionListener(this.mTableListener);
        
        final TableColumn[] columns = this.getTable().getColumns();
        for (final TableColumn column : columns) {
            
            column.removeSelectionListener(this.mTableListener);
            column.removeControlListener(this.mTableListener);
        }
        
        this.restoreState();
        
        this.getTable().addSelectionListener(this.mTableListener);
        
        for (final TableColumn column : columns) {
            column.addSelectionListener(this.mTableListener);
            column.addControlListener(this.mTableListener);
        }
    }
    
    /**
     * Helper method to resort the table viewer.
     */
    private void resort() {
    
        this.getTable().getDisplay().asyncExec(new Runnable()
        {
            
            
            public void run() {
                
                if (! EnhancedTableViewer.this.getControl().isDisposed()) {
            
                EnhancedTableViewer.this.getControl().setRedraw(false);
                EnhancedTableViewer.this.refresh(false);
                EnhancedTableViewer.this.getControl().setRedraw(true);
                
                }
            }
        });
    }
    
    /**
     * Restores the sorting state from the dialog settings.
     */
    private void restoreState() {
    
        final IDialogSettings settings =
                this.mSettingsProvider != null ? this.mSettingsProvider.getTableSettings() : null;
        
        if (settings == null) {
            return;
        }
        try {
            this.mSortedColumnIndex = settings.getInt(TAG_COLUMN_INDEX);
        } catch (final NumberFormatException e) {
            this.mSortedColumnIndex = 0;
        }
        try {
            this.mSortDirection = settings.getInt(TAG_SORT_DIRECTION);
        } catch (final NumberFormatException e) {
            this.mSortDirection = DIRECTION_FORWARD;
        }
        
        final TableLayout layout = new TableLayout();
        boolean allColumnsHaveStoredData = true;
        
        // store the column widths
        final TableColumn[] columns = this.getTable().getColumns();
        for (int i = 0, size = columns.length; i < size; i++) {
            
            try {
                final int width = settings.getInt(TAG_COLUMN_WIDTH + i);
                columns[i].setWidth(width);
                layout.addColumnData(new ColumnPixelData(width));
            } catch (final NumberFormatException e) {
                // probably a new column
                allColumnsHaveStoredData = false;
            }
        }
        
        // if no all columns have stored width data then probably a new
        // columns has been added, in this case fall back to the default
        // weighted layout
        if (allColumnsHaveStoredData) {
            this.getTable().setLayout(layout);
        }
        
        // restore the selection
        try {
            this.getTable().select(settings.getInt(TAG_CURRENT_SELECTION));
        } catch (final NumberFormatException e) {
            // NOOP
        }
        
        this.resort();
    }
    
    /**
     * Saves the sorting state to the dialog settings.
     */
    private void saveState() {
    
        final IDialogSettings settings =
                this.mSettingsProvider != null ? this.mSettingsProvider.getTableSettings() : null;
        
        if (settings == null) {
            return;
        }
        
        settings.put(TAG_COLUMN_INDEX, this.mSortedColumnIndex);
        settings.put(TAG_SORT_DIRECTION, this.mSortDirection);
        
        // store the column widths
        final TableColumn[] columns = this.getTable().getColumns();
        for (int i = 0, size = columns.length; i < size; i++) {
            final int width = columns[i].getWidth();
            if (width > 0) {
                settings.put(TAG_COLUMN_WIDTH + i, width);
            }
        }
        
        // store the selection
        settings.put(TAG_CURRENT_SELECTION, this.getTable().getSelectionIndex());
    }
    
    /**
     * Sets the comparable provider for this table.
     * 
     * @param comparableProvider
     *            the comparable provider
     */
    public void setTableComparableProvider(ITableComparableProvider comparableProvider) {
    
        this.mComparableProvider = comparableProvider;
        
        if (this.mComparableProvider != null) {
            this.setSorter(new TableSorter());
        } else {
            this.setSorter(null);
        }
    }
    
    /**
     * Sets the settings provider.
     * 
     * @param settingsProvider
     *            the settings provider
     */
    public void setTableSettingsProvider(ITableSettingsProvider settingsProvider) {
    
        this.mSettingsProvider = settingsProvider;
    }
}
