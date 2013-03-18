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

package com.tocea.scertify.eclipse.scertifycode.ui;


import java.util.HashMap;
import java.util.Map;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.plugin.AbstractUIPlugin;

/**
 * Manages and caches images for the plugin.
 * 
 * @author Lars K�dderitzsch
 */
public abstract class ScertifyUIPluginImages
{
    
    /** Image descriptor for the plugin logo. */
    public static final ImageDescriptor              PLUGIN_LOGO;
    
    /** Image descriptor for the filter icon. */
    public static final ImageDescriptor              FILTER_ICON;
    
    /** Image descriptor for the Scertify violation view icon. */
    public static final ImageDescriptor              LIST_VIEW_ICON;
    
    /** Image descriptor for the graph view icon. */
    public static final ImageDescriptor              GRAPH_VIEW_ICON;
    
    // public static final ImageDescriptor DEFAULT_SCERTIFY_PRODUCT_ICON;
    
    /** Image cache. */
    private static final Map<ImageDescriptor, Image> CACHED_IMAGES = new HashMap<ImageDescriptor, Image>();
    
    static {
        
        PLUGIN_LOGO = AbstractUIPlugin.imageDescriptorFromPlugin(Activator.PLUGIN_ID, "icons/tocea-logo.png"); //$NON-NLS-1$
        
        FILTER_ICON = AbstractUIPlugin.imageDescriptorFromPlugin(Activator.PLUGIN_ID, "icons/filter_16.gif"); //$NON-NLS-1$
        LIST_VIEW_ICON = AbstractUIPlugin.imageDescriptorFromPlugin(Activator.PLUGIN_ID, "icons/listingView.gif"); //$NON-NLS-1$
        GRAPH_VIEW_ICON = AbstractUIPlugin.imageDescriptorFromPlugin(Activator.PLUGIN_ID, "icons/graphView.gif"); //$NON-NLS-1$
        
    }
    
    /**
     * Disposes the cached images and clears the cache.
     */
    public static void clearCachedImages() {
    
        for (final Image image : CACHED_IMAGES.values()) {
            image.dispose();
        }
        
        CACHED_IMAGES.clear();
    }
    
    /**
     * Gets an image from a given descriptor.
     * 
     * @param descriptor
     *            the descriptor
     * @return the image
     */
    public static Image getImage(final ImageDescriptor descriptor) {
    
        Image image = CACHED_IMAGES.get(descriptor);
        if (image == null) {
            image = descriptor.createImage();
            CACHED_IMAGES.put(descriptor, image);
        }
        return image;
    }
    
    /**
     * Hidden default constructor.
     */
    private ScertifyUIPluginImages() {
    
        // NOOP
    }
}
