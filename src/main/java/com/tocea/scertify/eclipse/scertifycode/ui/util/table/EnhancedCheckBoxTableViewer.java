/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * Contributors:
 * IBM Corporation - initial API and implementation
 *******************************************************************************/

package com.tocea.scertify.eclipse.scertifycode.ui.util.table;


import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.NoSuchElementException;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ICheckable;
import org.eclipse.jface.viewers.IElementComparer;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Widget;

// import org.eclipse.jface.util.Assert; // deprecated

/**
 * A concrete viewer based on an SWT <code>Table</code> control with checkboxes
 * on each node.
 * <p>
 * This class is not intended to be subclassed outside the viewer framework. It is designed to be instantiated with a pre-existing SWT table
 * control and configured with a domain-specific content provider, label provider, element filter (optional), and element sorter (optional).
 * </p>
 */
public class EnhancedCheckBoxTableViewer extends EnhancedTableViewer implements ICheckable
{
    
    // CHECKSTYLE:OFF
    
    final static class CustomHashtable
    {
        
        private static final class EmptyEnumerator implements Enumeration
        {
            
            
            public boolean hasMoreElements() {
            
                return false;
            }
            
            
            public Object nextElement() {
            
                throw new NoSuchElementException();
            }
        }
        
        private class HashEnumerator implements Enumeration
        {
            
            boolean      key;
            
            int          start;
            
            HashMapEntry entry;
            
            HashEnumerator(boolean isKey) {
            
                this.key = isKey;
                this.start = CustomHashtable.this.firstSlot;
            }
            
            
            public boolean hasMoreElements() {
            
                if (this.entry != null) {
                    return true;
                }
                while (this.start <= CustomHashtable.this.lastSlot) {
                    if (CustomHashtable.this.elementData[this.start++] != null) {
                        this.entry = CustomHashtable.this.elementData[this.start - 1];
                        return true;
                    }
                }
                return false;
            }
            
            
            public Object nextElement() {
            
                if (this.hasMoreElements()) {
                    final Object result = this.key ? this.entry.key : this.entry.value;
                    this.entry = this.entry.next;
                    return result;
                }
                throw new NoSuchElementException();
            }
        }
        
        /**
         * HashMapEntry is an internal class which is used to hold the entries
         * of a Hashtable.
         */
        private static class HashMapEntry
        {
            
            Object       key, value;
            
            HashMapEntry next;
            
            HashMapEntry(Object theKey, Object theValue) {
            
                this.key = theKey;
                this.value = theValue;
            }
        }
        
        transient int                            elementCount;
        
        transient HashMapEntry[]                 elementData;
        
        private float                            loadFactor;
        
        private int                              threshold;
        
        transient int                            firstSlot        = 0;
        
        transient int                            lastSlot         = -1;
        
        transient private final IElementComparer comparer;
        
        private static final EmptyEnumerator     emptyEnumerator  = new EmptyEnumerator();
        
        /**
         * The default capacity used when not specified in the constructor.
         */
        public static final int                  DEFAULT_CAPACITY = 13;
        
        /**
         * Constructs a new Hashtable using the default capacity and load
         * factor.
         */
        public CustomHashtable() {
        
            this(13);
        }
        
        /**
         * Constructs a new hash table with enough capacity to hold all keys in
         * the given hash table, then adds all key/value pairs in the given hash
         * table to the new one, using the given element comparer.
         * 
         * @param capacity
         *            the maximum number of elements that can be added without
         *            rehashing
         * @param comparer
         *            the element comparer to use to compare keys and obtain
         *            hash codes for keys, or <code>null</code> to use the
         *            normal <code>equals</code> and <code>hashCode</code> methods
         */
        public CustomHashtable(CustomHashtable table, IElementComparer comparer) {
        
            this(table.size() * 2, comparer);
            for (int i = table.elementData.length; --i >= 0;) {
                HashMapEntry entry = table.elementData[i];
                while (entry != null) {
                    this.put(entry.key, entry.value);
                    entry = entry.next;
                }
            }
        }
        
        /**
         * Constructs a new hash table with the default capacity and the given
         * element comparer.
         * 
         * @param comparer
         *            the element comparer to use to compare keys and obtain
         *            hash codes for keys, or <code>null</code> to use the
         *            normal <code>equals</code> and <code>hashCode</code> methods
         */
        public CustomHashtable(IElementComparer comparer) {
        
            this(DEFAULT_CAPACITY, comparer);
        }
        
        /**
         * Constructs a new Hashtable using the specified capacity and the
         * default load factor.
         * 
         * @param capacity
         *            the initial capacity
         */
        public CustomHashtable(int capacity) {
        
            this(capacity, null);
        }
        
        /**
         * Constructs a new hash table with the given capacity and the given
         * element comparer.
         * 
         * @param capacity
         *            the maximum number of elements that can be added without
         *            rehashing
         * @param comparer
         *            the element comparer to use to compare keys and obtain
         *            hash codes for keys, or <code>null</code> to use the
         *            normal <code>equals</code> and <code>hashCode</code> methods
         */
        public CustomHashtable(int capacity, IElementComparer comparer) {
        
            if (capacity >= 0) {
                this.elementCount = 0;
                this.elementData = new HashMapEntry[capacity == 0 ? 1 : capacity];
                this.firstSlot = this.elementData.length;
                this.loadFactor = 0.75f;
                this.computeMaxSize();
            } else {
                throw new IllegalArgumentException();
            }
            this.comparer = comparer;
        }
        
        private void computeMaxSize() {
        
            this.threshold = (int) (this.elementData.length * this.loadFactor);
        }
        
        /**
         * Answers if this Hashtable contains the specified object as a key of
         * one of the key/value pairs.
         * 
         * @param key
         *            the object to look for as a key in this Hashtable
         * @return true if object is a key in this Hashtable, false otherwise
         */
        public boolean containsKey(Object key) {
        
            return this.getEntry(key) != null;
        }
        
        /**
         * Answers an Enumeration on the values of this Hashtable. The results
         * of the Enumeration may be affected if the contents of this Hashtable
         * are modified.
         * 
         * @return an Enumeration of the values of this Hashtable
         */
        public Enumeration elements() {
        
            if (this.elementCount == 0) {
                return emptyEnumerator;
            }
            return new HashEnumerator(false);
        }
        
        /**
         * Answers the value associated with the specified key in this
         * Hashtable.
         * 
         * @param key
         *            the key of the value returned
         * @return the value associated with the specified key, null if the
         *         specified key does not exist
         */
        public Object get(Object key) {
        
            final int index = (this.hashCode(key) & 0x7FFFFFFF) % this.elementData.length;
            HashMapEntry entry = this.elementData[index];
            while (entry != null) {
                if (this.keyEquals(key, entry.key)) {
                    return entry.value;
                }
                entry = entry.next;
            }
            return null;
        }
        
        private HashMapEntry getEntry(Object key) {
        
            final int index = (this.hashCode(key) & 0x7FFFFFFF) % this.elementData.length;
            HashMapEntry entry = this.elementData[index];
            while (entry != null) {
                if (this.keyEquals(key, entry.key)) {
                    return entry;
                }
                entry = entry.next;
            }
            return null;
        }
        
        /**
         * Answers the hash code for the given key.
         */
        private int hashCode(Object key) {
        
            if (this.comparer == null) {
                return key.hashCode();
            }
            return this.comparer.hashCode(key);
        }
        
        /**
         * Compares two keys for equality.
         */
        private boolean keyEquals(Object a, Object b) {
        
            if (this.comparer == null) {
                return a.equals(b);
            }
            return this.comparer.equals(a, b);
        }
        
        /**
         * Answers an Enumeration on the keys of this Hashtable. The results of
         * the Enumeration may be affected if the contents of this Hashtable are
         * modified.
         * 
         * @return an Enumeration of the keys of this Hashtable
         */
        public Enumeration keys() {
        
            if (this.elementCount == 0) {
                return emptyEnumerator;
            }
            return new HashEnumerator(true);
        }
        
        /**
         * Associate the specified value with the specified key in this
         * Hashtable. If the key already exists, the old value is replaced. The
         * key and value cannot be null.
         * 
         * @param key
         *            the key to add
         * @param value
         *            the value to add
         * @return the old value associated with the specified key, null if the
         *         key did not exist
         */
        public Object put(Object key, Object value) {
        
            if (key != null && value != null) {
                int index = (this.hashCode(key) & 0x7FFFFFFF) % this.elementData.length;
                HashMapEntry entry = this.elementData[index];
                while (entry != null && !this.keyEquals(key, entry.key)) {
                    entry = entry.next;
                }
                if (entry == null) {
                    if (++this.elementCount > this.threshold) {
                        this.rehash();
                        index = (this.hashCode(key) & 0x7FFFFFFF) % this.elementData.length;
                    }
                    if (index < this.firstSlot) {
                        this.firstSlot = index;
                    }
                    if (index > this.lastSlot) {
                        this.lastSlot = index;
                    }
                    entry = new HashMapEntry(key, value);
                    entry.next = this.elementData[index];
                    this.elementData[index] = entry;
                    return null;
                }
                final Object result = entry.value;
                entry.key = key; // important to avoid hanging onto keys that
                // are equal but "old" -- see bug 30607
                entry.value = value;
                return result;
            }
            throw new NullPointerException();
        }
        
        /**
         * Increases the capacity of this Hashtable. This method is sent when
         * the size of this Hashtable exceeds the load factor.
         */
        private void rehash() {
        
            int length = this.elementData.length << 1;
            if (length == 0) {
                length = 1;
            }
            this.firstSlot = length;
            this.lastSlot = -1;
            final HashMapEntry[] newData = new HashMapEntry[length];
            for (int i = this.elementData.length; --i >= 0;) {
                HashMapEntry entry = this.elementData[i];
                while (entry != null) {
                    final int index = (this.hashCode(entry.key) & 0x7FFFFFFF) % length;
                    if (index < this.firstSlot) {
                        this.firstSlot = index;
                    }
                    if (index > this.lastSlot) {
                        this.lastSlot = index;
                    }
                    final HashMapEntry next = entry.next;
                    entry.next = newData[index];
                    newData[index] = entry;
                    entry = next;
                }
            }
            this.elementData = newData;
            this.computeMaxSize();
        }
        
        /**
         * Remove the key/value pair with the specified key from this Hashtable.
         * 
         * @param key
         *            the key to remove
         * @return the value associated with the specified key, null if the
         *         specified key did not exist
         */
        public Object remove(Object key) {
        
            HashMapEntry last = null;
            final int index = (this.hashCode(key) & 0x7FFFFFFF) % this.elementData.length;
            HashMapEntry entry = this.elementData[index];
            while (entry != null && !this.keyEquals(key, entry.key)) {
                last = entry;
                entry = entry.next;
            }
            if (entry != null) {
                if (last == null) {
                    this.elementData[index] = entry.next;
                } else {
                    last.next = entry.next;
                }
                this.elementCount--;
                return entry.value;
            }
            return null;
        }
        
        /**
         * Answers the number of key/value pairs in this Hashtable.
         * 
         * @return the number of key/value pairs in this Hashtable
         */
        public int size() {
        
            return this.elementCount;
        }
        
        /**
         * Answers the string representation of this Hashtable.
         * 
         * @return the string representation of this Hashtable
         */
        
        public String toString() {
        
            if (this.size() == 0) {
                return "{}"; //$NON-NLS-1$
            }
            
            final StringBuffer buffer = new StringBuffer();
            buffer.append('{');
            for (int i = this.elementData.length; --i >= 0;) {
                HashMapEntry entry = this.elementData[i];
                while (entry != null) {
                    buffer.append(entry.key);
                    buffer.append('=');
                    buffer.append(entry.value);
                    buffer.append(", "); //$NON-NLS-1$
                    entry = entry.next;
                }
            }
            // Remove the last ", "
            if (this.elementCount > 0) {
                buffer.setLength(buffer.length() - 2);
            }
            buffer.append('}');
            return buffer.toString();
        }
    }
    
    /**
     * Creates a new table control with one column.
     * 
     * @param parent
     *            the parent control
     * @param style
     *            style bits
     * @return a new table control
     */
    protected static Table createTable(Composite parent, int style) {
    
        final Table table = new Table(parent, SWT.CHECK | style);
        
        // Although this table column is not needed, and can cause resize
        // problems,
        // it can't be removed since this would be a breaking change against
        // R1.0.
        // See bug 6643 for more details.
        new TableColumn(table, SWT.NONE);
        final TableLayout layout = new TableLayout();
        layout.addColumnData(new ColumnWeightData(100));
        table.setLayout(layout);
        
        return table;
    }
    
    /**
     * Creates a table viewer on a newly-created table control under the given
     * parent. The table control is created using the given SWT style bits, plus
     * the <code>SWT.CHECK</code> style bit. The table shows its contents in a
     * single column, with no header. The viewer has no input, no content
     * provider, a default label provider, no sorter, and no filters.
     * <p>
     * No <code>TableColumn</code> is added. SWT does not require a <code>TableColumn</code> if showing only a single column with no header.
     * SWT correctly handles the initial sizing and subsequent resizes in this case.
     * 
     * @param parent
     *            the parent control
     * @param style
     *            SWT style bits
     * @since 2.0
     */
    public static EnhancedCheckBoxTableViewer newCheckList(Composite parent, int style) {
    
        final Table table = new Table(parent, SWT.CHECK | style);
        return new EnhancedCheckBoxTableViewer(table);
    }
    
    /**
     * List of check state listeners (element type: <code>ICheckStateListener</code>).
     */
    private final ListenerList checkStateListeners = new ListenerList(3);
    
    /**
     * Creates a table viewer on a newly-created table control under the given
     * parent. The table control is created using the SWT style bits: <code>SWT.CHECK</code> and <code>SWT.BORDER</code>. The table has one
     * column. The viewer has no input, no content provider, a default label
     * provider, no sorter, and no filters.
     * <p>
     * This is equivalent to calling <code>new CheckboxTableViewer(parent, SWT.BORDER)</code>. See that constructor for more details.
     * </p>
     * 
     * @param parent
     *            the parent control
     * @deprecated use newCheckList(Composite, int) or new
     *             CheckboxTableViewer(Table) instead (see below for details)
     */
    @Deprecated
    public EnhancedCheckBoxTableViewer(Composite parent) {
    
        this(parent, SWT.BORDER);
    }
    
    /**
     * Creates a table viewer on a newly-created table control under the given
     * parent. The table control is created using the given SWT style bits, plus
     * the <code>SWT.CHECK</code> style bit. The table has one column. The
     * viewer has no input, no content provider, a default label provider, no
     * sorter, and no filters.
     * <p>
     * This also adds a <code>TableColumn</code> for the single column, and sets a <code>TableLayout</code> on the table which sizes the
     * column to fill the table for its initial sizing, but does nothing on subsequent resizes.
     * </p>
     * <p>
     * If the caller just needs to show a single column with no header, it is preferable to use the <code>newCheckList</code> factory method
     * instead, since SWT properly handles the initial sizing and subsequent resizes in this case.
     * </p>
     * <p>
     * If the caller adds its own columns, uses <code>Table.setHeadersVisible(true)</code>, or needs to handle dynamic resizing of the
     * table, it is recommended to create the <code>Table</code> itself, specifying the <code>SWT.CHECK</code> style bit (along with any
     * other style bits needed), and use <code>new CheckboxTableViewer(Table)</code> rather than this constructor.
     * </p>
     * 
     * @param parent
     *            the parent control
     * @param style
     *            SWT style bits
     * @deprecated use newCheckList(Composite, int) or new
     *             CheckboxTableViewer(Table) instead (see above for details)
     */
    @Deprecated
    public EnhancedCheckBoxTableViewer(Composite parent, int style) {
    
        this(createTable(parent, style));
    }
    
    /**
     * Creates a table viewer on the given table control. The <code>SWT.CHECK</code> style bit must be set on the given table control.
     * The viewer has no input, no content provider, a default label provider,
     * no sorter, and no filters.
     * 
     * @param table
     *            the table control
     */
    public EnhancedCheckBoxTableViewer(Table table) {
    
        super(table);
    }
    
    /*
     * (non-Javadoc) Method declared on ICheckable.
     */
    
    public void addCheckStateListener(ICheckStateListener listener) {
    
        this.checkStateListeners.add(listener);
    }
    
    /**
     * Notifies any check state listeners that a check state changed has been
     * received. Only listeners registered at the time this method is called are
     * notified.
     * 
     * @param event
     *            a check state changed event
     * @see ICheckStateListener#checkStateChanged
     */
    private void fireCheckStateChanged(final CheckStateChangedEvent event) {
    
        final Object[] array = this.checkStateListeners.getListeners();
        for (final Object element : array) {
            final ICheckStateListener l = (ICheckStateListener) element;
            SafeRunnable.run(new SafeRunnable()
            {
                
                
                public void run() {
                
                    l.checkStateChanged(event);
                }
            });
        }
    }
    
    /*
     * (non-Javadoc) Method declared on ICheckable.
     */
    
    public boolean getChecked(Object element) {
    
        final Widget widget = this.findItem(element);
        if (widget instanceof TableItem) {
            return ((TableItem) widget).getChecked();
        }
        return false;
    }
    
    /**
     * Returns a list of elements corresponding to checked table items in this
     * viewer.
     * <p>
     * This method is typically used when preserving the interesting state of a viewer; <code>setCheckedElements</code> is used during the
     * restore.
     * </p>
     * 
     * @return the array of checked elements
     * @see #setCheckedElements
     */
    public Object[] getCheckedElements() {
    
        final TableItem[] children = this.getTable().getItems();
        final ArrayList v = new ArrayList(children.length);
        for (final TableItem item : children) {
            if (item.getChecked()) {
                v.add(item.getData());
            }
        }
        return v.toArray();
    }
    
    /**
     * Returns the grayed state of the given element.
     * 
     * @param element
     *            the element
     * @return <code>true</code> if the element is grayed, and <code>false</code> if not grayed
     */
    public boolean getGrayed(Object element) {
    
        final Widget widget = this.findItem(element);
        if (widget instanceof TableItem) {
            return ((TableItem) widget).getGrayed();
        }
        return false;
    }
    
    /**
     * Returns a list of elements corresponding to grayed nodes in this viewer.
     * <p>
     * This method is typically used when preserving the interesting state of a viewer; <code>setGrayedElements</code> is used during the
     * restore.
     * </p>
     * 
     * @return the array of grayed elements
     * @see #setGrayedElements
     */
    public Object[] getGrayedElements() {
    
        final TableItem[] children = this.getTable().getItems();
        final List v = new ArrayList(children.length);
        for (final TableItem item : children) {
            if (item.getGrayed()) {
                v.add(item.getData());
            }
        }
        return v.toArray();
    }
    
    /*
     * (non-Javadoc) Method declared on StructuredViewer.
     */
    
    public void handleSelect(SelectionEvent event) {
    
        if (event.detail == SWT.CHECK) {
            super.handleSelect(event); // this will change the current
            // selection
            
            final TableItem item = (TableItem) event.item;
            final Object data = item.getData();
            if (data != null) {
                this.fireCheckStateChanged(new CheckStateChangedEvent(this, data, item.getChecked()));
            }
        } else {
            super.handleSelect(event);
        }
    }
    
    /**
     * Returns a new hashtable using the given capacity and this viewer's
     * element comparer.
     * 
     * @param capacity
     *            the initial capacity of the hashtable
     * @return a new hashtable
     * @since 3.0
     */
    CustomHashtable newAHashtable(final int capacity) {
    
        return new CustomHashtable(capacity, this.getComparer());
    }
    
    /*
     * (non-Javadoc) Method declared on Viewer.
     */
    
    protected void preservingSelection(Runnable updateCode) {
    
        TableItem[] children = this.getTable().getItems();
        final CustomHashtable checked = this.newAHashtable(children.length * 2 + 1);
        final CustomHashtable grayed = this.newAHashtable(children.length * 2 + 1);
        
        for (final TableItem item : children) {
            final Object data = item.getData();
            if (data != null) {
                if (item.getChecked()) {
                    checked.put(data, data);
                }
                if (item.getGrayed()) {
                    grayed.put(data, data);
                }
            }
        }
        
        super.preservingSelection(updateCode);
        
        children = this.getTable().getItems();
        for (final TableItem item : children) {
            final Object data = item.getData();
            if (data != null) {
                item.setChecked(checked.containsKey(data));
                item.setGrayed(grayed.containsKey(data));
            }
        }
    }
    
    /*
     * (non-Javadoc) Method declared on ICheckable.
     */
    
    public void removeCheckStateListener(ICheckStateListener listener) {
    
        this.checkStateListeners.remove(listener);
    }
    
    /**
     * Sets to the given value the checked state for all elements in this
     * viewer.
     * 
     * @param state
     *            <code>true</code> if the element should be checked, and <code>false</code> if it should be unchecked
     */
    public void setAllChecked(boolean state) {
    
        final TableItem[] children = this.getTable().getItems();
        for (final TableItem item : children) {
            item.setChecked(state);
        }
    }
    
    /**
     * Sets to the given value the grayed state for all elements in this viewer.
     * 
     * @param state
     *            <code>true</code> if the element should be grayed, and <code>false</code> if it should be ungrayed
     */
    public void setAllGrayed(boolean state) {
    
        final TableItem[] children = this.getTable().getItems();
        for (final TableItem item : children) {
            item.setGrayed(state);
        }
    }
    
    /*
     * (non-Javadoc) Method declared on ICheckable.
     */
    
    public boolean setChecked(Object element, boolean state) {
    
        Assert.isNotNull(element);
        final Widget widget = this.findItem(element);
        if (widget instanceof TableItem) {
            ((TableItem) widget).setChecked(state);
            return true;
        }
        return false;
    }
    
    /**
     * Sets which nodes are checked in this viewer. The given list contains the
     * elements that are to be checked; all other nodes are to be unchecked.
     * <p>
     * This method is typically used when restoring the interesting state of a viewer captured by an earlier call to
     * <code>getCheckedElements</code>.
     * </p>
     * 
     * @param elements
     *            the list of checked elements (element type: <code>Object</code>)
     * @see #getCheckedElements
     */
    public void setCheckedElements(Object[] elements) {
    
        this.assertElementsNotNull(elements);
        final CustomHashtable set = this.newAHashtable(elements.length * 2 + 1);
        for (final Object element : elements) {
            set.put(element, element);
        }
        final TableItem[] items = this.getTable().getItems();
        for (final TableItem item : items) {
            final Object element = item.getData();
            if (element != null) {
                final boolean check = set.containsKey(element);
                // only set if different, to avoid flicker
                if (item.getChecked() != check) {
                    item.setChecked(check);
                }
            }
        }
    }
    
    /**
     * Sets the grayed state for the given element in this viewer.
     * 
     * @param element
     *            the element
     * @param state
     *            <code>true</code> if the item should be grayed, and <code>false</code> if it should be ungrayed
     * @return <code>true</code> if the element is visible and the gray state
     *         could be set, and <code>false</code> otherwise
     */
    public boolean setGrayed(Object element, boolean state) {
    
        Assert.isNotNull(element);
        final Widget widget = this.findItem(element);
        if (widget instanceof TableItem) {
            ((TableItem) widget).setGrayed(state);
            return true;
        }
        return false;
    }
    
    /**
     * Sets which nodes are grayed in this viewer. The given list contains the
     * elements that are to be grayed; all other nodes are to be ungrayed.
     * <p>
     * This method is typically used when restoring the interesting state of a viewer captured by an earlier call to
     * <code>getGrayedElements</code>.
     * </p>
     * 
     * @param elements
     *            the array of grayed elements
     * @see #getGrayedElements
     */
    public void setGrayedElements(Object[] elements) {
    
        this.assertElementsNotNull(elements);
        final CustomHashtable set = this.newAHashtable(elements.length * 2 + 1);
        for (final Object element : elements) {
            set.put(element, element);
        }
        final TableItem[] items = this.getTable().getItems();
        for (final TableItem item : items) {
            final Object element = item.getData();
            if (element != null) {
                final boolean gray = set.containsKey(element);
                // only set if different, to avoid flicker
                if (item.getGrayed() != gray) {
                    item.setGrayed(gray);
                }
            }
        }
    }
    
    // CHECKSTYLE:ON
}
