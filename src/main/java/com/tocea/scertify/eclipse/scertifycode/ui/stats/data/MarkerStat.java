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


import java.util.ArrayList;
import java.util.Collection;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.ui.texteditor.MarkerUtilities;

import com.tocea.scertify.eclipse.scertifycode.ui.Activator;
import com.tocea.scertify.eclipse.scertifycode.ui.api.IScertifyMarkerInformationsProvider;

/**
 * Objet qui donne des statistiques sur les marqueurs.
 * 
 * @author Fabrice BELLINGARD
 */
public class MarkerStat implements Comparable
{
    
    /**
     * Identifiant du marqueur : dans notre cas, il s'agit du message du
     * marqueur Scertify.
     */
    private final String     mIdentifiant;
    
    /**
     * List of the markers of this categories.
     */
    private final Collection mMarkers;
    
    /**
     * The maximum severity for this marker group.
     */
    private int              mMaxSeverity;
    private int              mMaxEclipseSeverity;
    
    private String           criticity;
    
    /**
     * Cr�e un MarkerStat pour un marqueur Scertify correspondant �
     * l'identifiant pass� en param�tre.
     * 
     * @param identifiant
     *            : le message du marqueur Scertify
     */
    public MarkerStat(String identifiant) {
    
        super();
        this.mIdentifiant = identifiant;
        this.mMarkers = new ArrayList();
    }
    
    /**
     * Reference the marker as one fo this category.
     * 
     * @param marker
     *            : the marker to add to this category
     */
    public void addMarker(IMarker marker) {
    
        this.mMarkers.add(marker);
        IScertifyMarkerInformationsProvider infos = Activator.markersInfoFactory().newInformationsProvider(marker);
        if (criticity == null) {
            
            this.criticity = infos.getCriticity();
            
        }
        final int severity = infos.getSeverity();
        if (severity > this.mMaxSeverity) {
            this.mMaxSeverity = severity;
        }
        int eclipseSeverity = infos.getEclipseSeverity();
        if(eclipseSeverity>this.mMaxEclipseSeverity){
            this.mMaxEclipseSeverity = eclipseSeverity;
        }
    }
    
    /**
     * {@inheritDoc}
     * 
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    
    public final int compareTo(Object o) {
    
        if (o instanceof MarkerStat) {
            final MarkerStat stat = (MarkerStat) o;
            return this.mIdentifiant.compareTo(stat.getIdentifiant());
        }
        return 0;
    }
    
    /**
     * Retourne le nombre d 'occurence.
     * 
     * @return Returns the count.
     */
    public final int getCount() {
    
        return this.mMarkers.size();
    }
    
    /**
     * Retourne l'identifiant (i.e. le message Scertify) de ce MarkerStat.
     * 
     * @return Returns the identifiant.
     */
    public final String getIdentifiant() {
    
        return this.mIdentifiant;
    }
    
    /**
     * Returns the list of markers for this category.
     * 
     * @return a collection of IMarker
     */
    public Collection getMarkers() {
    
        return this.mMarkers;
    }
    
    /**
     * Returns the maximum severity level occurring in this group.
     * 
     * @return the maximum severity level
     */
    public final int getMaxSeverity() {
    
        return this.mMaxSeverity;
    }
    
    
    /**
     * Returns the maximum severity level occurring in this group.
     * 
     * @return the maximum severity level
     */
    public final int getMaxEclipseSeverity() {
    
        return this.mMaxEclipseSeverity;
    }
    public final String getCriticity() {
    
        return criticity;
    }
}
