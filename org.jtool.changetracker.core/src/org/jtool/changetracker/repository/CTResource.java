/*
 *  Copyright 2017
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.changetracker.repository;

import org.jtool.changetracker.operation.IChangeOperation;

import java.util.List;
import java.util.Collections;
import java.util.Comparator;
import java.time.ZonedDateTime;

/**
 * Stores information about a resource stored into the repository.
 * @author Katsuhisa Maruyama
 */
public class CTResource {
    
    /**
     * The name of this resource.
     */
    protected String name;
    
    /**
     * The fully-qualified name of this resource.
     */
    protected String qualifiedName;
    
    /**
     * The time range for change operations related to this resource.
     */
    protected TimeRange timeRange = null;
    
    /**
     * Creates an instance that stores information about this resource.
     * @param name the name of this resource
     * @param qname the qualified name of this resource
     */
    protected CTResource(String name, String qname) {
        this.name = name;
        this.qualifiedName = qname;
    }
    
    /**
     * Returns the name of this resource.
     * @return the name of the resource
     */
    public String getName() {
        return name;
    }
    
    /**
     * Returns the qualified name of this resource.
     * @return the qualified name of the resource
     */
    public String getQualifiedName() {
        return qualifiedName;
    }
    
    /**
     * Updates the time range for change operations related to this resource.
     * @param op a change operation related to this resource
     */
    void updateTimeRange(IChangeOperation op) {
        if (timeRange == null) {
            timeRange = new TimeRange(op.getTime(), op.getTime());
        }
        
        if (op.getTime().isAfter(timeRange.getTo())) {
            timeRange.setTo(op.getTime());
        }
    }
    
    /**
     * Returns the time range for change operations related to this resource.
     * @return the time range, or <code>null</code> if this resource is not related to any change operation
     */
    public TimeRange getTimeRange() {
        return timeRange;
    }
    
    /**
     * Returns the earliest time for change operations of this resource.
     * @return the earliest time, or <code>null</code> if this resource is not related to any change operation
     */
    public ZonedDateTime getFromTime() {
        if (timeRange != null) {
            return timeRange.getFrom();
        }
        return null;
    }
    
    /**
     * Returns the latest time for change operations of this resource.
     * @return the latest time, or <code>null</code> if this resource is not related to any change operation
     */
    public ZonedDateTime getToTime() {
        if (timeRange != null) {
            return timeRange.getTo();
        }
        return null;
    }
    
    
    
    /**
     * Tests if this resource is the same as a given one.
     * @param res the resource
     * @return <code>true</code> if the two resources are the same, otherwise <code>false</code>
     */
    public boolean equals(CTResource res) {
        if (res == null) {
            return false;
        }
        
        return getQualifiedName().equals(res.getQualifiedName());
    }
    
    /**
     * Tests if this object is the same as a given object.
     * @param obj the object
     * @return <code>true</code> if the two objects are the same, otherwise <code>false</code>
     */
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof CTResource) {
            return equals((CTResource)obj);
        }
        return false;
    }
    
    /**
     * Returns a hash code value for this object.
     * @return always <code>0</code> that means all objects have the same hash code
     */
    @Override
    public int hashCode() {
        return 0;
    }
    
    /**
     * Sorts the collection of resources in alphabetical order.
     * @param rs the collection of the resources to be sorted
     */
    static void sort(List<? extends CTResource> rs) {
        
        /**
         * Compares two resources in alphabetical order.
         * @param res1 - the first resource to be compared
         * @param res2 - the second resource to be compared
         */
        Collections.sort(rs, new Comparator<CTResource>() {
            public int compare(CTResource res1, CTResource res2) {
                return res1.getName().compareTo(res2.getName());
            }
        });
    }
}
