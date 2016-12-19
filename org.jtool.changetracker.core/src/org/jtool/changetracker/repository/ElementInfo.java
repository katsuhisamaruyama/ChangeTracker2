/*
 *  Copyright 2016
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.changetracker.repository;

import java.util.List;

import org.jtool.changetracker.operation.IChangeOperation;

import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.Comparator;

/**
 * Stores information about an element stored into the repository.
 * @author Katsuhisa Maruyama
 */
public class ElementInfo {
    
    /**
     * The name of this element.
     */
    protected String name;
    
    /**
     * The fully-qualified name of this element.
     */
    protected String qualifiedName;
    
    /**
     * The time range for the code change operations related to this resource.
     */
    protected TimeRange timeRange;
    
    /**
     * Creates an element that store basic information.
     * @param name the name of this element
     * @param qname the qualified name of this element
     */
    protected ElementInfo(String name, String qname) {
        this.name = name;
        this.qualifiedName = qname;
        this.timeRange = null;
    }
    
    /**
     * Returns the name of this element.
     * @return the name of the element
     */
    public String getName() {
        return name;
    }
    
    /**
     * Returns the qualified name of this element.
     * @return the qualified name of the element
     */
    public String getQualifiedName() {
        return qualifiedName;
    }
    
    void updateTimeRange(IChangeOperation operation) {
        if (timeRange == null) {
            timeRange = new TimeRange(operation.getTime(), operation.getTime());
        }
        if (operation.getTime().isAfter(timeRange.getTo())) {
            timeRange.setTo(operation.getTime());
        }
    }
    
    /**
     * Returns the time range for the code change operations related to this resource.
     * @return the time range, or <code>null</code> if this file information contains no code change operation
     */
    public TimeRange getTimeRange() {
        return timeRange;
    }
    
    /**
     * Returns the time that indicates the start point of the time range of this resource.
     * @return the start point, or <code>null</code> if this file information contains no code change operation
     */
    public ZonedDateTime getFromTime() {
        if (timeRange != null) {
            return timeRange.getFrom();
        }
        return null;
    }
    
    /**
     * Returns the time that indicates the end point of the time range of this resource.
     * @return the end point, or <code>null</code> if this file information contains no code change operation
     */
    public ZonedDateTime getToTime() {
        if (timeRange != null) {
            return timeRange.getTo();
        }
        return null;
    }
    
    /**
     * Sorts the collection of elements in alphabetical order.
     * @param elements the collection of the elements to be sorted
     */
    static void sort(List<? extends ElementInfo> elements) {
        
        /**
         * Compares two elements for order.
         * @param operation1 - the first element to be compared
         * @param operation2 - the second element to be compared
         */
        Collections.sort(elements, new Comparator<ElementInfo>() {
            public int compare(ElementInfo resource1, ElementInfo resource2) {
                return resource1.getName().compareTo(resource2.getName());
            }
        });
    }
}
