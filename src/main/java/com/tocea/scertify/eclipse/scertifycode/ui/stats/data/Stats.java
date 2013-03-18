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


import java.util.Collection;

/**
 * Classe qui v�hicule les statistiques Scertify. Elle contient notamment la
 * liste des diff�rentes erreurs avec leur comptage.
 * 
 * @author Fabrice BELLINGARD
 */
public class Stats
{
    
    /** Liste des diff�rentes erreurs. */
    private final Collection mMarkerStats;
    
    /**
     * Nombre de marqueurs scann�s.
     */
    private final int        mMarkerCount;
    
    private final int        lastAuditMarkerCount;
    
    /** The number of all markers in the workspace. */
    private final int        mMarkerCountWhole;
    
    /**
     * Constructeur.
     * 
     * @param markerStats
     *            la liste des MarkerStats
     * @param markerCount
     *            le nombre de marqueurs scann�s
     * @param markerCountWhole
     *            the number of all scertify markers in the workspace
     */
    public Stats(Collection markerStats, int lastAuditMarkerCount, int markerCount, int markerCountWhole) {
    
        super();
        this.mMarkerStats = markerStats;
        this.mMarkerCount = markerCount;
        this.mMarkerCountWhole = markerCountWhole;
        this.lastAuditMarkerCount = lastAuditMarkerCount;
    }
    
    /**
     * Returns the markerCount.
     * 
     * @return Returns the markerCount.
     */
    public int getMarkerCount() {
    
        return this.mMarkerCount;
    }
    
    /**
     * Returns the difference of violations with the last audit.F
     * 
     * @return
     */
    public int getMarkerDiffWithLastAudit() {
    
        return mMarkerCount - lastAuditMarkerCount;
    }
    
    /**
     * Returns the number of all Scertify markers in the workspace.
     * 
     * @return the number of all Scertify markers
     */
    public int getMarkerCountAll() {
    
        return this.mMarkerCountWhole;
    }
    
    /**
     * Returns the markerStats.
     * 
     * @return Returns the markerStats.
     */
    public Collection getMarkerStats() {
    
        return this.mMarkerStats;
    }
}
