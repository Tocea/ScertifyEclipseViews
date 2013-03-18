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


import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarkerDelta;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.progress.IWorkbenchSiteProgressService;
import org.eclipse.ui.progress.WorkbenchJob;

import com.tocea.scertify.eclipse.scertifycode.ui.Activator;
import com.tocea.scertify.eclipse.scertifycode.ui.stats.Messages;
import com.tocea.scertify.eclipse.scertifycode.ui.stats.data.CreateStatsJob;
import com.tocea.scertify.eclipse.scertifycode.ui.stats.data.Stats;
import com.tocea.scertify.eclipse.scertifycode.ui.stats.views.internal.ScertifyMarkerFilter;
import com.tocea.scertify.eclipse.scertifycode.ui.stats.views.internal.ScertifyMarkerFilterDialog;

/**
 * Abstract view that gathers common behaviour for the stats views.
 * 
 * @author Fabrice BELLINGARD
 * @author Lars K�dderitzsch
 * @author Tocea
 */
public abstract class AbstractStatsView extends ViewPart
{
    
    //
    // attributes
    //
    
    private int previousNumberOfViolations;
    private int currentNumberOfViolations;
    
    /**
     * *** * Copied from ResourceUtil.getFile() since ResourceUtil is only
     * available since Eclipse 3.1 *** Returns the file corresponding to the
     * given editor input, or <code>null</code> if there is no applicable file.
     * Returns <code>null</code> if the given editor input is <code>null</code>.
     * 
     * @param editorInput
     *            the editor input, or <code>null</code>
     * @return the file corresponding to the editor input, or <code>null</code>
     */
    public static IFile getFile(final IEditorInput editorInput) {
    
        if (editorInput == null) {
            return null;
        }
        // Note: do not treat IFileEditorInput as a special case. Use the
        // adapter mechanism instead.
        // See Bug 87288 [IDE] [EditorMgmt] Should avoid explicit checks for
        // [I]FileEditorInput
        final Object o = editorInput.getAdapter(IFile.class);
        if (o instanceof IFile) {
            return (IFile) o;
        }
        return null;
    }
    
    /** the main composite. */
    private Composite               mMainComposite;
    
    /** The filter for this stats view. */
    private ScertifyMarkerFilter    mFilter;
    
    /** The focused resources. */
    private IResource[]             mFocusedResources;
    
    /** The views private set of statistics. */
    private Stats                   mStats;
    
    /** The listener reacting to selection changes in the workspace. */
    private ISelectionListener      mFocusListener;
    
    //
    // methods
    //
    
    /** The listener reacting on resource changes. */
    private IResourceChangeListener mResourceListener;
    
    private void considerAdaptable(final IAdaptable adaptable, final Collection resources) {
    
        IResource resource = (IResource) adaptable.getAdapter(IResource.class);
        
        if (resource == null) {
            resource = (IResource) adaptable.getAdapter(IFile.class);
        }
        
        if (resource != null) {
            resources.add(resource);
        }
    }
    
    /**
     * {@inheritDoc}
     * 
     * @see org.eclipse.ui.IWorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
     */
    @Override
    public void createPartControl(final Composite parent) {
    
        this.mMainComposite = parent;
        
        // create and register the workspace focus listener
        this.mFocusListener = new ISelectionListener()
        {
            
         
            public void selectionChanged(final IWorkbenchPart part, final ISelection selection) {
            
                AbstractStatsView.this.focusSelectionChanged(part, selection);
            }
        };
        
        getSite().getPage().addSelectionListener(this.mFocusListener);
        focusSelectionChanged(getSite().getPage().getActivePart(), getSite().getPage().getSelection());
        
        // create and register the listener for resource changes
        this.mResourceListener = new IResourceChangeListener()
        {
            
            
            public void resourceChanged(final IResourceChangeEvent event) {
            
                final IMarkerDelta[] deltas = Activator.auditInfos().findScertifyMarkerDeltas(event);
                if (deltas.length > 0) {
                    
                    AbstractStatsView.this.refresh(Job.DECORATE);
                    
                }
                
            }
        };
        
        ResourcesPlugin.getWorkspace().addResourceChangeListener(this.mResourceListener);
        
        makeActions();
        initActionBars(getViewSite().getActionBars());
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void dispose() {
    
        // IMPORTANT: Deregister listeners
        getSite().getPage().removeSelectionListener(this.mFocusListener);
        ResourcesPlugin.getWorkspace().removeResourceChangeListener(this.mResourceListener);
        
        super.dispose();
    }
    
    public final void openFiltersDialog() {
    
        ScertifyMarkerFilterDialog dialog = new ScertifyMarkerFilterDialog(this.mMainComposite.getShell(),
                (ScertifyMarkerFilter) getFilter().clone());
        
        if (dialog.open() == 0) {
            ScertifyMarkerFilter filter = dialog.getFilter();
            filter.saveState(getDialogSettings());
            
            this.mFilter = filter;
            refresh(Job.DECORATE);
        }
    }
    
    /**
     * Invoked on selection changes within the workspace.
     * 
     * @param part
     *            the workbench part the selection occurred
     * @param selection
     *            the selection
     */
    private void focusSelectionChanged(final IWorkbenchPart part, final ISelection selection) {
    
        final List resources = new ArrayList();
        if (part instanceof IEditorPart) {
            final IEditorPart editor = (IEditorPart) part;
            final IFile file = getFile(editor.getEditorInput());
            if (file != null) {
                resources.add(file);
            }
        } else {
            if (selection instanceof IStructuredSelection) {
                for (final Iterator iterator = ((IStructuredSelection) selection).iterator(); iterator.hasNext();) {
                    final Object object = iterator.next();
                    if (object instanceof IWorkingSet) {
                        
                        final IWorkingSet workingSet = (IWorkingSet) object;
                        final IAdaptable[] elements = workingSet.getElements();
                        for (final IAdaptable element : elements) {
                            considerAdaptable(element, resources);
                        }
                    } else if (object instanceof IAdaptable) {
                        considerAdaptable((IAdaptable) object, resources);
                    }
                }
            }
        }
        
        final IResource[] focusedResources = new IResource[resources.size()];
        resources.toArray(focusedResources);
        
        // check if update necessary -> if so then update
        final boolean updateNeeded = updateNeeded(this.mFocusedResources, focusedResources);
        if (updateNeeded) {
            this.mFocusedResources = focusedResources;
            getFilter().setFocusResource(focusedResources);
            refresh(Job.DECORATE);
        }
    }
    
    /**
     * Returns the dialog settings for this view.
     * 
     * @return the dialog settings
     */
    protected final IDialogSettings getDialogSettings() {
    
        final String concreteViewId = getViewId();
        
        final IDialogSettings workbenchSettings = Activator.getDefault().getDialogSettings();
        IDialogSettings settings = workbenchSettings.getSection(concreteViewId);
        
        if (settings == null) {
            settings = workbenchSettings.addNewSection(concreteViewId);
        }
        
        return settings;
    }
    
    /**
     * Returns the filter of this view.
     * 
     * @return the filter
     */
    protected final ScertifyMarkerFilter getFilter() {
    
        if (this.mFilter == null) {
            this.mFilter = new ScertifyMarkerFilter();
            this.mFilter.restoreState(getDialogSettings());
        }
        
        return this.mFilter;
    }
    
    /**
     * Returns the statistics data.
     * 
     * @return the data of this view
     */
    protected final Stats getStats() {
    
        return this.mStats;
    }
    
    /**
     * Returns the view id of the concrete view. This is used to make separate
     * filter settings (stored in dialog settings) for different concrete views
     * possible.
     * 
     * @return the view id
     */
    protected abstract String getViewId();
    
    /**
     * Callback for subclasses to refresh the content of their controls, since
     * the statistics data has been updated. <br/>
     * Note that the subclass should check if their controls have been disposed,
     * since this method is called by a job that might run even if the view has
     * been closed.
     */
    protected abstract void handleStatsRebuilt();
    
    /**
     * Initializes the action bars of this view.
     * 
     * @param actionBars
     *            the action bars
     */
    protected void initActionBars(final IActionBars actionBars) {
    
        initMenu(actionBars.getMenuManager());
        initToolBar(actionBars.getToolBarManager());
    }
    
    protected abstract void initMenu(IMenuManager menu);
    
    protected abstract void initToolBar(IToolBarManager tbm);
    
    /**
     * Create the viewer actions.
     */
    protected abstract void makeActions();
    
    /**
     * Causes the view to re-sync its contents with the workspace. Note that
     * changes will be scheduled in a background job, and may not take effect
     * immediately.
     */
    protected final void refresh(final int priority) {
    
        final IWorkbenchSiteProgressService service = (IWorkbenchSiteProgressService) getSite().getAdapter(
                IWorkbenchSiteProgressService.class);
        
        // rebuild statistics data
        final CreateStatsJob job = new CreateStatsJob(getFilter(), getViewId());
        job.setPriority(priority);
        // job.setSystem(true);
        
        job.setRule(null);
        job.addJobChangeListener(new JobChangeAdapter()
        {
            
            @Override
            public void done(final IJobChangeEvent event) {
            
                AbstractStatsView.this.mStats = ((CreateStatsJob) event.getJob()).getStats();
                previousNumberOfViolations = currentNumberOfViolations;
                currentNumberOfViolations = mStats.getMarkerCountAll();
                final Job uiJob = new WorkbenchJob(Messages.AbstractStatsView_msgRefreshStats)
                {
                    
                    @Override
                    public IStatus runInUIThread(final IProgressMonitor monitor) {
                    
                        AbstractStatsView.this.handleStatsRebuilt();
                        return Status.OK_STATUS;
                    }
                };
                uiJob.setPriority(Job.DECORATE);
                uiJob.setSystem(true);
                uiJob.schedule();
            }
        });
        // Schedule the job after a sleeping time
        service.schedule(job, 500, true);
        
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void setFocus() {
    
    }
    
    /**
     * Checks if an update of the statistics data is needed, based on the
     * current and previously selected resources. The current filter setting is
     * also taken into consideration.
     * 
     * @param oldResources
     *            the previously selected resources.
     * @param newResources
     *            the currently selected resources
     * @return <code>true</code> if an update of the statistics data is needed
     */
    private boolean updateNeeded(final IResource[] oldResources, final IResource[] newResources) {
    
        // determine if an update if refiltering is required
        final ScertifyMarkerFilter filter = getFilter();
        if (!filter.isEnabled()) {
            return false;
        }
        
        final int onResource = filter.getOnResource();
        if (onResource == ScertifyMarkerFilter.ON_ANY_RESOURCE || onResource == ScertifyMarkerFilter.ON_WORKING_SET) {
            return false;
        }
        if (newResources == null || newResources.length < 1) {
            return false;
        }
        if (oldResources == null || oldResources.length < 1) {
            return true;
        }
        if (Arrays.equals(oldResources, newResources)) {
            return false;
        }
        if (onResource == ScertifyMarkerFilter.ON_ANY_RESOURCE_OF_SAME_PROJECT) {
            final Collection oldProjects = ScertifyMarkerFilter.getProjectsAsCollection(oldResources);
            final Collection newProjects = ScertifyMarkerFilter.getProjectsAsCollection(newResources);
            
            if (oldProjects.size() == newProjects.size()) {
                return !newProjects.containsAll(oldProjects);
            }
            return true;
        }
        
        return true;
    }
    
}
