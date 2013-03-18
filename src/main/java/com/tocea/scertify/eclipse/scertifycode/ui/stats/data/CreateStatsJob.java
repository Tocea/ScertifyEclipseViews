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

package com.tocea.scertify.eclipse.scertifycode.ui.stats.data;


import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.commons.lang.ObjectUtils;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;

import com.tocea.scertify.eclipse.scertifycode.ui.Activator;
import com.tocea.scertify.eclipse.scertifycode.ui.api.IScertifyMarkerInformationsProvider;
import com.tocea.scertify.eclipse.scertifycode.ui.stats.Messages;
import com.tocea.scertify.eclipse.scertifycode.ui.stats.views.internal.ScertifyMarkerFilter;

/**
 * Job implementation that builds the data objects for the statistic views.
 * 
 * @author Lars K�dderitzsch
 */
public class CreateStatsJob extends Job
{
    
    /** Regexp to find {0}-like strings. */
    private static final Pattern       REGEXP_HOLES = Pattern.compile("\\{[0-9]+(\\S)*\\}"); //$NON-NLS-1$
                                                                                             
    /** Regexp to find suites of ' character. */
    private static final Pattern       REGEXP_QUOTE = Pattern.compile("'+");                //$NON-NLS-1$
                                                                                             
    /** The filter to analyze. */
    private final ScertifyMarkerFilter mFilter;
    
    /** The statistics data object. */
    private Stats                      mStats;
    
    /** The job family this job belongs to. */
    private final String               mFamily;
    
    /**
     * Creates the job.
     * 
     * @param filter
     *            the marker filter to analyze
     */
    public CreateStatsJob(final ScertifyMarkerFilter filter, final String family) {
    
        super(Messages.CreateStatsJob_msgAnalyzeMarkers);
        this.mFilter = (ScertifyMarkerFilter) filter.clone();
        this.mFamily = family;
    }
    
    /**
     * Cleans the unlocalized message so that it is more readable.
     * 
     * @param message
     *            : the message to clean
     * @return the cleaned message
     */
    public static String cleanMessage(final String message) {
    
        if (message != null) {
            // replacements
            String finalMessage = REGEXP_HOLES.matcher(message).replaceAll("X"); //$NON-NLS-1$
            finalMessage = REGEXP_QUOTE.matcher(finalMessage).replaceAll("'"); //$NON-NLS-1$
            
            return finalMessage;
        } else {
            return "";
        }
    }
    
    @Override
    public final boolean belongsTo(final Object family) {
    
        return ObjectUtils.equals(this.mFamily, family);
    }
    
    /**
     * Returns the statistics data compiled by the job.
     * 
     * @return the statistics data
     */
    public final Stats getStats() {
    
        return this.mStats;
    }
    
    @Override
    protected final IStatus run(final IProgressMonitor monitor) {
    
        try {
            
            final IMarker[] markers = this.mFilter.findMarkers(monitor);
            
            final Map<String, MarkerStat> markerStats = new HashMap<String, MarkerStat>();
            int currentCount = 0;
            for (final IMarker marker : markers) {
                
                final IScertifyMarkerInformationsProvider wrapper = Activator.markersInfoFactory().newInformationsProvider(marker);
                if (wrapper.isScertify()) {
                    final String rule = wrapper.getRuleIdentifier();
                    ++currentCount;
                    String message = null;
                    
                    message = wrapper.getDescription();
                    message = cleanMessage(message);
                    
                    // check that the message is not empty
                    if (message == null || message.trim().length() == 0) {
                        // cela ne devrait pas arriver, mais bon, on laisse
                        // faire
                        if (Activator.isLogging()) {
                            Activator.log(null, Messages.CreateStatsJob_markerMessageShouldntBeEmpty);
                        }
                        continue;
                    }
                    
                    // puis on recherche
                    MarkerStat stat = markerStats.get(rule);
                    if (stat == null) {
                        // 1ere fois qu'on rencontre un marqueur de ce type
                        stat = new MarkerStat(rule);
                        markerStats.put(rule, stat);
                    }
                    stat.addMarker(marker);
                }
            }
            
            final int lastAuditMarkerCount = mFilter.getPreviousNumberOfViolations();
            this.mStats = new Stats(markerStats.values(), lastAuditMarkerCount, currentCount, currentCount);
        } catch (final CoreException e) {
            return new Status(IStatus.ERROR, Activator.PLUGIN_ID, IStatus.OK, Messages.CreateStatsJob_errorAnalyzingMarkers, e);
        }
        
        return Status.OK_STATUS;
    }
    
    @Override
    public final boolean shouldSchedule() {
    
        final Job[] similarJobs = getJobManager().find(this.mFamily);
        return similarJobs.length == 0;
    }
    
}
