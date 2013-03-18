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

package com.tocea.scertify.eclipse.scertifycode.ui.stats.views;


import java.awt.Color;
import java.awt.Font;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import org.eclipse.core.internal.resources.ResourceException;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.texteditor.MarkerUtilities;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartMouseEvent;
import org.jfree.chart.ChartMouseListener;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.entity.PieSectionEntity;
import org.jfree.chart.plot.PiePlot3D;
import org.jfree.experimental.chart.swt.ChartComposite;
import org.jfree.util.Rotation;
import org.osgi.service.prefs.BackingStoreException;

import com.tocea.scertify.eclipse.scertifycode.ui.Activator;
import com.tocea.scertify.eclipse.scertifycode.ui.ScertifyUIPluginImages;
import com.tocea.scertify.eclipse.scertifycode.ui.stats.Messages;
import com.tocea.scertify.eclipse.scertifycode.ui.stats.data.MarkerStat;
import com.tocea.scertify.eclipse.scertifycode.ui.stats.data.Stats;
import com.tocea.scertify.eclipse.scertifycode.ui.util.table.EnhancedTableViewer;
import com.tocea.scertify.eclipse.scertifycode.ui.util.table.ITableComparableProvider;
import com.tocea.scertify.eclipse.scertifycode.ui.util.table.ITableSettingsProvider;

/**
 * View that shows a graph for the Checkstyle marker distribution.
 * 
 * @author Fabrice BELLINGARD
 * @author Lars K�dderitzsch
 * @author Tocea
 */

public class GraphStatsView extends AbstractStatsView
{
    
    //
    // constants
    //
    
    /**
     * Content provider for the detail table viewer.
     * 
     * @author Lars K�dderitzsch
     */
    private class DetailContentProvider implements IStructuredContentProvider
    {
        
        private Object[] mCurrentDetails;
        
        /**
         * @see org.eclipse.jface.viewers.IContentProvider#dispose()
         */
        
        public void dispose() {
        
            this.mCurrentDetails = null;
        }
        
        /**
         * @see org.eclipse.jface.viewers.IStructuredContentProvider#getElements(java.lang.Object)
         */
        
        public Object[] getElements(final Object inputElement) {
        
            if (this.mCurrentDetails == null) {
                // find the marker statistics for the current category
                final Stats currentStats = (Stats) inputElement;
                final Collection markerStats = currentStats.getMarkerStats();
                final Iterator it = markerStats.iterator();
                while (it.hasNext()) {
                    final MarkerStat markerStat = (MarkerStat) it.next();
                    if (markerStat.getIdentifiant().equals(GraphStatsView.this.mCurrentDetailCategory)) {
                        this.mCurrentDetails = markerStat.getMarkers().toArray();
                        break;
                    }
                }
            }
            
            return this.mCurrentDetails != null ? this.mCurrentDetails : new Object[0];
        }
        
        /**
         * @see org.eclipse.jface.viewers.IContentProvider#inputChanged(org.eclipse.jface.viewers.Viewer, java.lang.Object,
         *      java.lang.Object)
         */
        
        public void inputChanged(final Viewer viewer, final Object oldInput, final Object newInput) {
        
            this.mCurrentDetails = null;
        }
        
    }
    
    /**
     * Label provider for the detail table viewer.
     * 
     * @author Lars K�dderitzsch
     */
    private class DetailViewMultiProvider extends LabelProvider implements ITableLabelProvider, ITableComparableProvider,
            ITableSettingsProvider
    {
        
        /**
         * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnImage(java.lang.Object, int)
         */
        
        public Image getColumnImage(final Object obj, final int index) {
        
            Image image = null;
            final IMarker marker = (IMarker) obj;
            
            if (index == 0) {
                final int severity = MarkerUtilities.getSeverity(marker);
                final ISharedImages sharedImages = PlatformUI.getWorkbench().getSharedImages();
                
                if (IMarker.SEVERITY_ERROR == severity) {
                    image = sharedImages.getImage(ISharedImages.IMG_OBJS_ERROR_TSK);
                } else if (IMarker.SEVERITY_WARNING == severity) {
                    image = sharedImages.getImage(ISharedImages.IMG_OBJS_WARN_TSK);
                } else if (IMarker.SEVERITY_INFO == severity) {
                    image = sharedImages.getImage(ISharedImages.IMG_OBJS_INFO_TSK);
                }
            }
            return image;
        }
        
        /**
         * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnText(java.lang.Object, int)
         */
        
        public String getColumnText(final Object obj, final int index) {
        
            final IMarker marker = (IMarker) obj;
            String text = null;
            
            try {
                switch (index) {
                    case 1:
                        text = marker.getResource().getName();
                        break;
                    case 2:
                        text = marker.getResource().getParent().getFullPath().toString();
                        break;
                    case 3:
                        text = marker.getAttribute(IMarker.LINE_NUMBER).toString();
                        break;
                    case 4:
                        text = marker.getAttribute(IMarker.MESSAGE).toString();
                        break;
                    
                    default:
                        text = ""; //$NON-NLS-1$
                        break;
                }
            } catch (final ResourceException e) {
                //NOOP Marker not found. probably regenerating
            } catch (final Exception e) {
                // Can't do anything: let's put a default value
                text = Messages.MarkerStatsView_unknownProblem;
                Activator.log(e);
            }
            
            return text;
        }
        
        
        public Comparable getComparableValue(final Object element, final int colIndex) {
        
            final IMarker marker = (IMarker) element;
            Comparable comparable = null;
            
            switch (colIndex) {
                case 0:
                    comparable = new Integer(marker.getAttribute(IMarker.SEVERITY, Integer.MAX_VALUE) * -1);
                    break;
                case 1:
                    comparable = marker.getResource().getName();
                    break;
                case 2:
                    comparable = marker.getResource().getParent().getFullPath().toString();
                    break;
                case 3:
                    comparable = new Integer(marker.getAttribute(IMarker.LINE_NUMBER, Integer.MAX_VALUE));
                    break;
                case 4:
                    comparable = marker.getAttribute(IMarker.MESSAGE, "").toString();
                    break;
                
                default:
                    comparable = ""; //$NON-NLS-1$
                    break;
            }
            
            return comparable;
        }
        
        
        public IDialogSettings getTableSettings() {
        
            final IDialogSettings mainSettings = getDialogSettings();
            
            IDialogSettings settings = mainSettings.getSection(TAG_SECTION_DETAIL);
            
            if (settings == null) {
                settings = mainSettings.addNewSection(TAG_SECTION_DETAIL);
            }
            
            return settings;
        }
    }
    
    //
    // attributes
    //
    
    /** The unique view id. */
    public static final String  VIEW_ID            = GraphStatsView.class.getName();
    
    private static final String TAG_SECTION_DETAIL = "detailView";
    
    /** The label containing the view description. */
    private Label               mLabelDesc;
    
    /** The main composite. */
    private Composite           mMainSection;
    
    /** The stack layout of the main composite. */
    private StackLayout         mStackLayout;
    
    /** The detail viewer. */
    private EnhancedTableViewer mDetailViewer;
    
    /** The composite to harbor the JFreeChart control. */
    private Composite           mMasterComposite;
    
    /** The graph component. */
    private JFreeChart          mGraph;
    
    /** The dataset for the graph. */
    private GraphPieDataset     mPieDataset;
    
    /** The action to go back to the master view. */
    private Action              mDrillBackAction;
    
    /** Opens the editor and shows the error in the code. */
    private Action              mShowErrorAction;
    
    /** Action to go back to the marker overview view. */
    private Action              mListingAction;
    
    // /** Allows to show all the categories or not. */
    // private Action mShowAllCategoriesAction;
    
    /** The current violation category to show in details view. */
    private String              mCurrentDetailCategory;
    
    /** The state if the view is currently drilled down to details. */
    private boolean             mIsDrilledDown;
    
    //
    // methods
    //
    
    /**
     * Cr�e le graphe JFreeChart.
     * 
     * @param piedataset
     *            : la source de donn�es � afficher
     * @return le diagramme
     */
    private JFreeChart createChart(final GraphPieDataset piedataset) {
    
        final JFreeChart jfreechart = ChartFactory.createPieChart3D(null, piedataset, false, true, false);
        jfreechart.setAntiAlias(true);
        jfreechart.setTextAntiAlias(true);
        
        final PiePlot3D pieplot3d = (PiePlot3D) jfreechart.getPlot();
        // pieplot3d.setInsets(new RectangleInsets(0, 0, 0, 0));
        
        final double angle = 290D;
        pieplot3d.setStartAngle(angle);
        pieplot3d.setDirection(Rotation.CLOCKWISE);
        final float foreground = 0.5F;
        pieplot3d.setForegroundAlpha(foreground);
        pieplot3d.setBackgroundAlpha(0.0F);
        pieplot3d.setNoDataMessage(Messages.GraphStatsView_noDataToDisplay);
        
        pieplot3d.setOutlinePaint(null);
        pieplot3d.setLabelFont(new Font("SansSerif", Font.PLAIN, 10));
        pieplot3d.setLabelGap(0.02);
        pieplot3d.setLabelOutlinePaint(null);
        pieplot3d.setLabelShadowPaint(null);
        pieplot3d.setLabelBackgroundPaint(Color.WHITE);
        pieplot3d.setBackgroundPaint(Color.WHITE);
        
        pieplot3d.setInteriorGap(0.02);
        pieplot3d.setMaximumLabelWidth(0.20);
        
        return jfreechart;
    }
    
    /**
     * Creates the table viewer for the detail view.
     * 
     * @param parent
     *            the parent composite
     * @return the detail table viewer
     */
    private EnhancedTableViewer createDetailView(final Composite parent) {
    
        // le tableau
        final EnhancedTableViewer detailViewer = new EnhancedTableViewer(parent, SWT.H_SCROLL | SWT.V_SCROLL | SWT.SINGLE
                | SWT.FULL_SELECTION);
        final GridData gridData = new GridData(GridData.FILL_BOTH);
        detailViewer.getControl().setLayoutData(gridData);
        
        // setup the table columns
        final Table table = detailViewer.getTable();
        table.setLinesVisible(true);
        table.setHeaderVisible(true);
        
        final TableColumn severityCol = new TableColumn(table, SWT.CENTER, 0);
        severityCol.setWidth(20);
        severityCol.setResizable(false);
        
        final TableColumn idCol = new TableColumn(table, SWT.LEFT, 1);
        idCol.setText(Messages.MarkerStatsView_fileColumn);
        idCol.setWidth(150);
        
        final TableColumn folderCol = new TableColumn(table, SWT.LEFT, 2);
        folderCol.setText(Messages.MarkerStatsView_folderColumn);
        folderCol.setWidth(300);
        
        final TableColumn countCol = new TableColumn(table, SWT.CENTER, 3);
        countCol.setText(Messages.MarkerStatsView_lineColumn);
        countCol.pack();
        
        final TableColumn messageCol = new TableColumn(table, SWT.LEFT, 4);
        messageCol.setText(Messages.MarkerStatsView_messageColumn);
        messageCol.setWidth(300);
        
        // set the providers
        detailViewer.setContentProvider(new DetailContentProvider());
        final DetailViewMultiProvider multiProvider = new DetailViewMultiProvider();
        detailViewer.setLabelProvider(multiProvider);
        detailViewer.setTableComparableProvider(multiProvider);
        detailViewer.setTableSettingsProvider(multiProvider);
        detailViewer.installEnhancements();
        
        // add selection listener to maintain action state
        detailViewer.addSelectionChangedListener(new ISelectionChangedListener()
        {
            
            
            public void selectionChanged(final SelectionChangedEvent event) {
            
                GraphStatsView.this.updateActions();
            }
        });
        
        // hooks the action to double click
        hookDoubleClickAction(this.mShowErrorAction, detailViewer);
        
        // and to the context menu too
        final ArrayList actionList = new ArrayList(1);
        actionList.add(this.mDrillBackAction);
        actionList.add(this.mShowErrorAction);
        actionList.add(new Separator());
        hookContextMenu(actionList, detailViewer);
        
        return detailViewer;
    }
    
    /**
     * Creates the master view containing the chart.
     * 
     * @param parent
     *            the parent composite
     * @return the chart composite
     */
    public Composite createMasterView(final Composite parent) {
    
        // create the date set for the chart
        this.mPieDataset = new GraphPieDataset();
        // this.mPieDataset.setShowAllCategories(this.mShowAllCategoriesAction.isChecked());
        this.mPieDataset.setShowAllCategories(true);
        this.mGraph = createChart(this.mPieDataset);
        
        // creates the chart component
        // final Composite embeddedComposite = new Composite(parent, SWT.EMBEDDED);
        // embeddedComposite.setLayoutData(new GridData(GridData.FILL_VERTICAL));
        
        // experimental usage of JFreeChart SWT
        // the composite to harbor the Swing chart control
        ChartComposite embeddedComposite = new ChartComposite(parent, SWT.NONE, mGraph, true);
        embeddedComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
        
        embeddedComposite.addChartMouseListener(new ChartMouseListener()
        {
            
            
            public void chartMouseClicked(final ChartMouseEvent event) {
            
                final MouseEvent trigger = event.getTrigger();
                if (trigger.getButton() == MouseEvent.BUTTON1 && event.getEntity() instanceof PieSectionEntity) {
                    
                    // && trigger.getClickCount() == 2 //doubleclick not correctly detected with the SWT composite
                    
                    GraphStatsView.this.mMasterComposite.getDisplay().syncExec(new Runnable()
                    {
                        
                        
                        public void run() {
                        
                            GraphStatsView.this.mIsDrilledDown = true;
                            GraphStatsView.this.mCurrentDetailCategory = (String) ((PieSectionEntity) event.getEntity()).getSectionKey();
                            GraphStatsView.this.mStackLayout.topControl = GraphStatsView.this.mDetailViewer.getTable();
                            GraphStatsView.this.mMainSection.layout();
                            GraphStatsView.this.mDetailViewer.setInput(GraphStatsView.this.mDetailViewer.getInput());
                            
                            GraphStatsView.this.updateActions();
                            GraphStatsView.this.updateLabel();
                        }
                    });
                } else {
                    event.getTrigger().consume();
                }
            }
            
            
            public void chartMouseMoved(final ChartMouseEvent event) {
            
                // NOOP
            }
        });
        
        return embeddedComposite;
    }
    
    /**
     * {@inheritDoc}
     * 
     * @see org.eclipse.ui.IWorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
     */
    
    public void createPartControl(final Composite parent) {
    
        super.createPartControl(parent);
        
        // set up the main layout
        final GridLayout layout = new GridLayout(1, false);
        layout.marginWidth = 0;
        layout.marginHeight = 0;
        parent.setLayout(layout);
        
        // the label
        this.mLabelDesc = new Label(parent, SWT.NONE);
        final GridData gridData = new GridData();
        gridData.horizontalAlignment = GridData.FILL;
        gridData.grabExcessHorizontalSpace = true;
        this.mLabelDesc.setLayoutData(gridData);
        
        // the main section
        this.mMainSection = new Composite(parent, SWT.NONE);
        this.mStackLayout = new StackLayout();
        this.mStackLayout.marginHeight = 0;
        this.mStackLayout.marginWidth = 0;
        this.mMainSection.setLayout(this.mStackLayout);
        this.mMainSection.setLayoutData(new GridData(GridData.FILL_BOTH));
        
        // create the master viewer
        this.mMasterComposite = createMasterView(this.mMainSection);
        
        // create the detail viewer
        this.mDetailViewer = createDetailView(this.mMainSection);
        
        this.mStackLayout.topControl = this.mMasterComposite;
        
        updateActions();
        
        // initialize the view data
        refresh(Job.DECORATE);
    }
    
    /**
     * {@inheritDoc}
     */
    
    protected String getViewId() {
    
        return VIEW_ID;
    }
    
    /**
     * {@inheritDoc}
     */
    
    protected void handleStatsRebuilt() {
    
        if (!this.mMasterComposite.isDisposed()) {
            // change the marker stats on the data set
            this.mPieDataset.setStats(getStats());
            this.mDetailViewer.setInput(getStats());
            
            // update the actions and the label
            updateActions();
            updateLabel();
        }
    }
    
    /**
     * Adds the actions to the tableviewer context menu.
     * 
     * @param actions
     *            a collection of IAction objets
     */
    private void hookContextMenu(final Collection actions, final StructuredViewer viewer) {
    
        final MenuManager menuMgr = new MenuManager();
        menuMgr.setRemoveAllWhenShown(true);
        menuMgr.addMenuListener(new IMenuListener()
        {
            
            
            public void menuAboutToShow(final IMenuManager manager) {
            
                for (final Iterator iter = actions.iterator(); iter.hasNext();) {
                    final Object item = iter.next();
                    if (item instanceof IContributionItem) {
                        manager.add((IContributionItem) item);
                    } else if (item instanceof IAction) {
                        manager.add((IAction) item);
                    }
                }
                manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
            }
        });
        final Menu menu = menuMgr.createContextMenu(viewer.getControl());
        viewer.getControl().setMenu(menu);
        
        getSite().registerContextMenu(menuMgr, viewer);
    }
    
    /**
     * Specifies which action will be run when double clicking on the viewer.
     * 
     * @param action
     *            the IAction to add
     */
    private void hookDoubleClickAction(final IAction action, final StructuredViewer viewer) {
    
        viewer.addDoubleClickListener(new IDoubleClickListener()
        {
            
            
            public void doubleClick(final DoubleClickEvent event) {
            
                action.run();
            }
        });
    }
    
    /**
     * {@inheritDoc}
     */
    
    protected void initMenu(final IMenuManager menu) {
    
        // menu.add(new FiltersAction(this));
        // menu.add(new Separator());
        // menu.add(this.mShowAllCategoriesAction);
    }
    
    /**
     * {@inheritDoc}
     */
    
    protected void initToolBar(final IToolBarManager tbm) {
    
        tbm.add(this.mListingAction);
        // tbm.add(new Separator());
        // tbm.add(this.mShowAllCategoriesAction);
        // tbm.add(new FiltersAction(this));
    }
    
    /**
     * See method below.
     * 
     * @see com.tocea.scertify.eclipse.stats.views.AbstractStatsView#makeActions()
     */
    
    protected void makeActions() {
    
        this.mListingAction = new Action()
        {
            
            
            public void run() {
            
                try {
                    GraphStatsView.this.getSite().getWorkbenchWindow().getActivePage().showView(MarkerStatsView.VIEW_ID);
                } catch (final PartInitException e) {
                    if (Activator.isLogging()) {
                        Activator.log(e, NLS.bind(Messages.GraphStatsView_unableToOpenListingView, MarkerStatsView.VIEW_ID));
                        // TO DO : mettre message d'erreur � l'utilisateur
                    }
                }
            }
        };
        this.mListingAction.setText(Messages.GraphStatsView_displayListing);
        this.mListingAction.setToolTipText(Messages.GraphStatsView_displayListing);
        this.mListingAction.setImageDescriptor(ScertifyUIPluginImages.LIST_VIEW_ICON);
        
        // action used to go back to the master view
        this.mDrillBackAction = new Action()
        {
            
            
            public void run() {
            
                GraphStatsView.this.mIsDrilledDown = false;
                GraphStatsView.this.mCurrentDetailCategory = null;
                GraphStatsView.this.mStackLayout.topControl = GraphStatsView.this.mMasterComposite;
                GraphStatsView.this.mMainSection.layout();
                
                GraphStatsView.this.updateActions();
                GraphStatsView.this.updateLabel();
            }
        };
        this.mDrillBackAction.setText(Messages.MarkerStatsView_actionBack);
        this.mDrillBackAction.setToolTipText(Messages.MarkerStatsView_actionBackTooltip);
        this.mDrillBackAction.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages()
                .getImageDescriptor(ISharedImages.IMG_TOOL_BACK));
        this.mDrillBackAction.setDisabledImageDescriptor(PlatformUI.getWorkbench().getSharedImages()
                .getImageDescriptor(ISharedImages.IMG_TOOL_BACK_DISABLED));
        
        // action used to show a specific error in the editor
        this.mShowErrorAction = new Action()
        {
            
            
            public void run() {
            
                final IStructuredSelection selection = (IStructuredSelection) GraphStatsView.this.mDetailViewer.getSelection();
                if (selection.getFirstElement() instanceof IMarker) {
                    final IMarker marker = (IMarker) selection.getFirstElement();
                    try {
                        IDE.openEditor(GraphStatsView.this.getSite().getPage(), marker);
                    } catch (final PartInitException e) {
                        Activator.log(e, Messages.MarkerStatsView_unableToShowMarker);
                        // TO DO : Open information dialog to notify the user
                    }
                }
            }
        };
        this.mShowErrorAction.setText(Messages.MarkerStatsView_displayError);
        this.mShowErrorAction.setToolTipText(Messages.MarkerStatsView_displayErrorTooltip);
        this.mShowErrorAction.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages()
                .getImageDescriptor(IDE.SharedImages.IMG_OPEN_MARKER));
        
    }
    
    /**
     * Helper method to manage the state of the view's actions.
     */
    private void updateActions() {
    
        this.mDrillBackAction.setEnabled(this.mIsDrilledDown);
        this.mShowErrorAction.setEnabled(this.mIsDrilledDown && !this.mDetailViewer.getSelection().isEmpty());
    }
    
    /**
     * Updates the title label.
     */
    private void updateLabel() {
    
        if (!this.mIsDrilledDown) {
            
            final Stats stats = getStats();
            if (stats != null) {
                final String text = NLS.bind(
                        Messages.GraphStatsView_lblViewMessage,
                        new Object[] {
                                new Integer(stats.getMarkerCount()), new Integer(stats.getMarkerStats().size()),
                                new Integer(stats.getMarkerCountAll()) });
                this.mLabelDesc.setText(text);
            } else {
                this.mLabelDesc.setText("");
            }
        } else {
            
            final String text = NLS.bind(Messages.MarkerStatsView_lblDetailMessage, new Object[] {
                    this.mCurrentDetailCategory, new Integer(this.mDetailViewer.getTable().getItemCount()) });
            this.mLabelDesc.setText(text);
        }
    }
}
