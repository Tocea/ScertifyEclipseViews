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


import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.ui.IWorkingSet;

import com.tocea.scertify.eclipse.scertifycode.ui.Activator;
import com.tocea.scertify.eclipse.scertifycode.ui.api.IScertifyAuditInformationsProvider;

/**
 * Filter class for Scertify markers. This filter is used by the Scertify
 * statistics views.
 * 
 * @author Lars K�dderitzsch
 * @author Tocea
 */
public class ScertifyMarkerFilter implements Cloneable
{
    
    //
    // constants
    //
    
    private static final String  TAG_DIALOG_SECTION                = "filter";            //$NON-NLS-1$
                                                                                           
    private static final String  TAG_ENABLED                       = "enabled";           //$NON-NLS-1$
                                                                                           
    private static final String  TAG_ON_RESOURCE                   = "onResource";        //$NON-NLS-1$
                                                                                           
    private static final String  TAG_WORKING_SET                   = "workingSet";        //$NON-NLS-1$
                                                                                           
    private static final String  TAG_SELECT_BY_SEVERITY            = "selectBySeverity";  //$NON-NLS-1$
                                                                                           
    private static final String  TAG_SEVERITY                      = "severity";          //$NON-NLS-1$
                                                                                           
    private static final String  TAG_SELECT_BY_REGEX               = "selectByRegex";     //$NON-NLS-1$
                                                                                           
    private static final String  TAG_REGULAR_EXPRESSIONS           = "regularExpressions"; //$NON-NLS-1$
                                                                                           
    public static final int      ON_ANY_RESOURCE                   = 0;
    
    public static final int      ON_SELECTED_RESOURCE_ONLY         = 1;
    
    public static final int      ON_SELECTED_RESOURCE_AND_CHILDREN = 2;
    
    public static final int      ON_ANY_RESOURCE_OF_SAME_PROJECT   = 3;
    
    public static final int      ON_WORKING_SET                    = 4;
    
    private static final int     DEFAULT_SEVERITY                  = 0;
    
    public static final int      SEVERITY_ERROR                    = 1 << 2;
    
    public static final int      SEVERITY_WARNING                  = 1 << 1;
    
    public static final int      SEVERITY_INFO                     = 1 << 0;
    
    private static final int     DEFAULT_ON_RESOURCE               = ON_ANY_RESOURCE;
    
    private static final boolean DEFAULT_SELECT_BY_SEVERITY        = false;
    
    private static final boolean DEFAULT_ACTIVATION_STATUS         = true;
    
    //
    // attributes
    //
    
    /**
     * Returns the set of projects that contain the given set of resources.
     * 
     * @param resources
     *            the resources
     * @return the array of projects for the given resources
     */
    private static IProject[] getProjects(final IResource[] resources) {
    
        final Collection<IProject> projects = getProjectsAsCollection(resources);
        return (IProject[]) projects.toArray(new IProject[projects.size()]);
    }
    
    /**
     * Returns the set of projects that contain the given set of resources.
     * 
     * @param resources
     *            the resources
     * @return the collection of projects for the given resources
     */
    public static Collection<IProject> getProjectsAsCollection(final IResource[] resources) {
    
        final HashSet<IProject> projects = new HashSet<IProject>();
        
        for (int idx = 0, size = resources != null ? resources.length : 0; idx < size; idx++) {
            projects.add(resources[idx].getProject());
        }
        return projects;
    }
    
    /**
     * Returns all resources within the working set.
     * 
     * @param workingSet
     *            the working set
     * @return the array of resources from the given working set
     */
    private static IResource[] getResourcesInWorkingSet(final IWorkingSet workingSet) {
    
        if (workingSet == null) {
            return new IResource[0];
        }
        
        final IAdaptable[] elements = workingSet.getElements();
        final List<IResource> result = new ArrayList<IResource>(elements.length);
        
        for (final IAdaptable element : elements) {
            final IResource next = (IResource) element.getAdapter(IResource.class);
            
            if (next != null) {
                result.add(next);
            }
        }
        
        return (IResource[]) result.toArray(new IResource[result.size()]);
    }
    
    /** Determines if this filter is enabled. */
    private boolean      mEnabled;
    
    /** The selection mode. */
    private int          mOnResource;
    
    /** The selected working set. */
    private IWorkingSet  mWorkingSet;
    
    /** Flags if the severity filtering is active. */
    private boolean      mSelectBySeverity;
    
    /** The selected severity. */
    private int          mSeverity;
    
    //
    // methods
    //
    
    /** The focused resources within the current workbench page. */
    private IResource[]  mFocusResources;
    
    /** Flags if the regex filter is enabled. */
    private boolean      mFilterByRegex;
    
    /** List of regular expressions used to filter messages. */
    private List<String> mFilterRegex;
    
    /**
     * {@inheritDoc}
     * 
     * @see java.lang.Object#clone()
     */
    @Override
    public Object clone() {
    
        try {
            return super.clone();
        } catch (final CloneNotSupportedException e) {
            throw new InternalError(); // this should never happen
        }
    }
    
    /**
     * Searches the workspace for markers that pass this filter.
     * 
     * @param mon
     *            the progress monitor
     * @return the array of Scertify markers that pass this filter.
     * @throws CoreException
     *             an unexpected error occurred
     */
    public IMarker[] findMarkers(final IProgressMonitor mon) throws CoreException {
    
        List<IMarker> unfiltered = Collections.EMPTY_LIST;
        
        if (!this.isEnabled()) {
            unfiltered = this.findScertifyMarkers(new IResource[] {
                ResourcesPlugin.getWorkspace().getRoot() }, IResource.DEPTH_INFINITE, mon);
        } else {
            
            switch (this.getOnResource()) {
                case ON_ANY_RESOURCE: {
                    unfiltered = this.findScertifyMarkers(new IResource[] {
                        ResourcesPlugin.getWorkspace().getRoot() }, IResource.DEPTH_INFINITE, mon);
                    break;
                }
                case ON_SELECTED_RESOURCE_ONLY: {
                    unfiltered = this.findScertifyMarkers(this.mFocusResources, IResource.DEPTH_ZERO, mon);
                    break;
                }
                case ON_SELECTED_RESOURCE_AND_CHILDREN: {
                    unfiltered = this.findScertifyMarkers(this.mFocusResources, IResource.DEPTH_INFINITE, mon);
                    break;
                }
                case ON_ANY_RESOURCE_OF_SAME_PROJECT: {
                    unfiltered = this.findScertifyMarkers(getProjects(this.mFocusResources), IResource.DEPTH_INFINITE, mon);
                    break;
                }
                case ON_WORKING_SET: {
                    unfiltered = this.findScertifyMarkers(getResourcesInWorkingSet(this.mWorkingSet), IResource.DEPTH_INFINITE, mon);
                    break;
                }
                default: {
                    break;
                }
            }
        }
        
        if (unfiltered == null) {
            unfiltered = Collections.EMPTY_LIST;
        }
        
        return (IMarker[]) unfiltered.toArray(new IMarker[unfiltered.size()]);
    }
    
    /**
     * Returns a list of all markers in the given set of resources.
     * 
     * @param resources
     *            the resources
     * @param depth
     *            the depth with which the markers are searched
     * @param mon
     *            the progress monitor
     * @throws CoreException
     */
    private List<IMarker> findScertifyMarkers(final IResource[] resources, final int depth, final IProgressMonitor mon)
            throws CoreException {
    
        if (resources == null) {
            return Collections.EMPTY_LIST;
        }
        
        final List<IMarker> resultList = new ArrayList<IMarker>(resources.length * 2);
        
        for (final IResource resource : resources) {
            if (resource.isAccessible()) {
                
                resultList.addAll(Arrays.asList(Activator.auditInfos().findScertifyMarkers(resource, depth)));
                
            }
        }
        
        if (this.mSelectBySeverity) {
            // further filter the markers by severity
            final int size = resultList.size();
            for (int i = size - 1; i >= 0; i--) {
                final IMarker marker = resultList.get(i);
                if (!this.selectBySeverity(marker)) {
                    resultList.remove(i);
                }
            }
        }
        
        if (this.mFilterByRegex) {
            // further filter the markers by regular expressions
            final int size = resultList.size();
            for (int i = size - 1; i >= 0; i--) {
                final IMarker marker = resultList.get(i);
                if (!this.selectByRegex(marker)) {
                    resultList.remove(i);
                }
            }
        }
        
        return resultList;
    }
    
    /**
     * Returns the regular expressions.
     * 
     * @return the regular expressions
     */
    public List<String> getFilterRegex() {
    
        return this.mFilterRegex;
    }
    
    /**
     * @return the selected resource(s) withing the workbench.
     */
    public IResource[] getFocusResource() {
    
        return this.mFocusResources;
    }
    
    /**
     * @return <ul>
     *         <li><code>MarkerFilter.ON_ANY_RESOURCE</code> if showing items associated with any resource.</li>
     *         <li><code>MarkerFilter.ON_SELECTED_RESOURCE_ONLY</code> if showing items associated with the selected resource within the
     *         workbench.</li>
     *         <li><code>MarkerFilter.ON_SELECTED_RESOURCE_AND_CHILDREN</code> if showing items associated with the selected resource within
     *         the workbench and its children.</li>
     *         <li><code>MarkerFilter.ON_ANY_RESOURCE_OF_SAME_PROJECT</code> if showing items in the same project as the selected resource
     *         within the workbench.</li>
     *         <li><code>MarkerFilter.ON_WORKING_SET</code> if showing items in some working set.</li>
     *         </ul>
     */
    public int getOnResource() {
    
        return this.mOnResource;
    }
    
    /**
     * Returns if the markers will be selected by severity.
     * 
     * @return <code>true</code> if markers will be selected by severity
     */
    public boolean getSelectBySeverity() {
    
        return this.mSelectBySeverity;
    }
    
    /**
     * Returns the severity.
     * 
     * @return the severity
     */
    public int getSeverity() {
    
        return this.mSeverity;
    }
    
    /**
     * @return the current working set or <code>null</code> if no working set is
     *         defined.
     */
    public IWorkingSet getWorkingSet() {
    
        return this.mWorkingSet;
    }
    
    /**
     * @return <ul>
     *         <li><code>true</code> if the filter is enabled.</li>
     *         <li><code>false</code> if the filter is not enabled.</li>
     *         </ul>
     */
    public boolean isEnabled() {
    
        return this.mEnabled;
    }
    
    /**
     * Returns if the regex filter is enabled.
     * 
     * @return <code>true</code> if the regex filter is enabled
     */
    public boolean isFilterByRegex() {
    
        return this.mFilterByRegex;
    }
    
    /**
     * Restores the default state of the filter.
     */
    public void resetState() {
    
        this.mEnabled = DEFAULT_ACTIVATION_STATUS;
        this.mOnResource = DEFAULT_ON_RESOURCE;
        this.setWorkingSet(null);
        this.mSelectBySeverity = DEFAULT_SELECT_BY_SEVERITY;
        this.mSeverity = DEFAULT_SEVERITY;
        this.mFilterByRegex = false;
        this.mFilterRegex = new ArrayList<String>();
    }
    
    /**
     * Restors the state of the filter from the given dialog settings.
     * 
     * @param dialogSettings
     *            the dialog settings
     */
    public void restoreState(final IDialogSettings dialogSettings) {
    
        this.resetState();
        final IDialogSettings settings = dialogSettings.getSection(TAG_DIALOG_SECTION);
        
        if (settings != null) {
            
            String setting = null;
            if ((setting = settings.get(TAG_ENABLED)) != null) {
                this.mEnabled = Boolean.valueOf(setting).booleanValue();
            }
            
            if ((setting = settings.get(TAG_ON_RESOURCE)) != null) {
                try {
                    this.mOnResource = Integer.parseInt(setting);
                } catch (final NumberFormatException eNumberFormat) {
                    // ignore and use default value
                }
            }
            
            if ((setting = settings.get(TAG_WORKING_SET)) != null) {
                this.setWorkingSet(Activator.getDefault().getWorkbench().getWorkingSetManager().getWorkingSet(setting));
            }
            
            if ((setting = settings.get(TAG_SELECT_BY_SEVERITY)) != null) {
                this.mSelectBySeverity = Boolean.valueOf(setting).booleanValue();
            }
            
            if ((setting = settings.get(TAG_SEVERITY)) != null) {
                try {
                    this.mSeverity = Integer.parseInt(setting);
                } catch (final NumberFormatException eNumberFormat) {
                    // ignore and use default value
                }
            }
            
            if ((setting = settings.get(TAG_SELECT_BY_REGEX)) != null) {
                this.mFilterByRegex = Boolean.valueOf(setting).booleanValue();
            }
            
            final String[] regex = settings.getArray(TAG_REGULAR_EXPRESSIONS);
            if (regex != null) {
                this.mFilterRegex = Arrays.asList(regex);
            }
        }
    }
    
    /**
     * Saves the state of the filter into the given dialog settings.
     * 
     * @param dialogSettings
     *            the dialog settings
     */
    public void saveState(final IDialogSettings dialogSettings) {
    
        if (dialogSettings != null) {
            IDialogSettings settings = dialogSettings.getSection(TAG_DIALOG_SECTION);
            
            if (settings == null) {
                settings = dialogSettings.addNewSection(TAG_DIALOG_SECTION);
            }
            
            settings.put(TAG_ENABLED, this.mEnabled);
            settings.put(TAG_ON_RESOURCE, this.mOnResource);
            
            if (this.mWorkingSet != null) {
                settings.put(TAG_WORKING_SET, this.mWorkingSet.getName());
            }
            
            settings.put(TAG_SELECT_BY_SEVERITY, this.mSelectBySeverity);
            settings.put(TAG_SEVERITY, this.mSeverity);
            
            settings.put(TAG_SELECT_BY_REGEX, this.mFilterByRegex);
            
            if (this.mFilterRegex != null) {
                settings.put(TAG_REGULAR_EXPRESSIONS, (String[]) this.mFilterRegex.toArray(new String[this.mFilterRegex.size()]));
            }
        }
    }
    
    /**
     * Selects marker by matching the message against regular expressions.
     * 
     * @param item
     *            the marker
     * @return <code>true</code> if the marker is selected
     */
    private boolean selectByRegex(final IMarker item) {
    
        if (this.mFilterByRegex) {
            
            final int size = this.mFilterRegex != null ? this.mFilterRegex.size() : 0;
            for (int i = 0; i < size; i++) {
                
                final String regex = (String) this.mFilterRegex.get(i);
                
                final String message = item.getAttribute(IMarker.MESSAGE, null);
                
                if (message != null && message.matches(regex)) {
                    return false;
                }
            }
        }
        return true;
        
    }
    
    /**
     * Selects markers by its severity.
     * 
     * @param item
     *            the marker
     * @return <code>true</code> if the marker is selected
     */
    private boolean selectBySeverity(final IMarker item) {
    
        if (this.mSelectBySeverity) {
            final int markerSeverity = item.getAttribute(IMarker.SEVERITY, -1);
            if (markerSeverity == IMarker.SEVERITY_ERROR) {
                return (this.mSeverity & SEVERITY_ERROR) > 0;
            } else if (markerSeverity == IMarker.SEVERITY_WARNING) {
                return (this.mSeverity & SEVERITY_WARNING) > 0;
            } else if (markerSeverity == IMarker.SEVERITY_INFO) {
                return (this.mSeverity & SEVERITY_INFO) > 0;
            }
        }
        
        return true;
    }
    
    /**
     * Sets the enablement state of the filter.
     * 
     * @param enabled
     *            the enablement
     */
    public void setEnabled(final boolean enabled) {
    
        this.mEnabled = enabled;
    }
    
    /**
     * Sets if the regex filter is enabled.
     * 
     * @param filterByRegex
     *            <code>true</code> if messages are filtered by the regular
     *            expressions
     */
    public void setFilterByRegex(final boolean filterByRegex) {
    
        this.mFilterByRegex = filterByRegex;
    }
    
    /**
     * Sets the list of regular expressions.
     * 
     * @param filterRegex
     *            the list of regular expression to filter by
     */
    public void setFilterRegex(final List<String> filterRegex) {
    
        this.mFilterRegex = filterRegex;
    }
    
    /**
     * Sets the focused resources.
     * 
     * @param resources
     *            the focuse resources
     */
    public void setFocusResource(final IResource[] resources) {
    
        this.mFocusResources = resources;
    }
    
    /**
     * Sets the type of filtering by selection.
     * 
     * @param onResource
     *            must be one of:
     *            <ul>
     *            <li><code>MarkerFilter.ON_ANY_RESOURCE</code></li>
     *            <li><code>MarkerFilter.ON_SELECTED_RESOURCE_ONLY</code></li>
     *            <li>
	 *            <code>MarkerFilter.ON_SELECTED_RESOURCE_AND_CHILDREN</code></li>
     *            <li><code>MarkerFilter.ON_ANY_RESOURCE_OF_SAME_PROJECT</code></li>
     *            <li><code>MarkerFilter.ON_WORKING_SET</code></li>
     *            </ul>
     */
    public void setOnResource(final int onResource) {
    
        if (onResource >= ON_ANY_RESOURCE && onResource <= ON_WORKING_SET) {
            this.mOnResource = onResource;
        }
    }
    
    /**
     * Sets if the markers will be selected by severity.
     * 
     * @param selectBySeverity
     *            <code>true</code> if markers will be selected by severity
     */
    public void setSelectBySeverity(final boolean selectBySeverity) {
    
        this.mSelectBySeverity = selectBySeverity;
    }
    
    /**
     * Sets the severity.
     * 
     * @param severity
     *            the severity
     */
    public void setSeverity(final int severity) {
    
        this.mSeverity = severity;
    }
    
    /**
     * Sets the current working set.
     * 
     * @param workingSet
     *            the working set
     */
    public void setWorkingSet(final IWorkingSet workingSet) {
    
        this.mWorkingSet = workingSet;
    }
    
    public int getPreviousNumberOfViolations() {
    
        IScertifyAuditInformationsProvider infos = Activator.auditInfos();
        int count = 0;
        switch (getOnResource()) {
            case ON_ANY_RESOURCE: {
                count = infos.getPreviousNumberOfViolations(IResource.DEPTH_INFINITE, ResourcesPlugin.getWorkspace().getRoot());
                
                break;
            }
            case ON_SELECTED_RESOURCE_ONLY: {
                count = infos.getPreviousNumberOfViolations(IResource.DEPTH_ZERO, this.mFocusResources);
                break;
            }
            case ON_SELECTED_RESOURCE_AND_CHILDREN: {
                count = infos.getPreviousNumberOfViolations(IResource.DEPTH_INFINITE, this.mFocusResources);
                break;
            }
            case ON_ANY_RESOURCE_OF_SAME_PROJECT: {
                count = infos.getPreviousNumberOfViolations(IResource.DEPTH_INFINITE, getProjects(this.mFocusResources));
                
                break;
            }
            case ON_WORKING_SET: {
                count = infos.getPreviousNumberOfViolations(IResource.DEPTH_INFINITE, getResourcesInWorkingSet(this.mWorkingSet));
                break;
            }
            default: {
                break;
            }
        }
        return count;
    }

}
