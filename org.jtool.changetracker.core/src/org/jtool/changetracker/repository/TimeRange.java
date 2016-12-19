/*
 *  Copyright 2016
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.changetracker.repository;

import org.jtool.changetracker.operation.ChangeOperation;
import java.time.ZonedDateTime;
import java.time.Duration;

/**
 * Represents the time range.
 * @author Katsuhisa Maruyama
 */
public class TimeRange {
    
    /**
     * The time that indicates the start point of this time range.
     */
    private ZonedDateTime fromTime;
    
    /**
     * The time that indicates the end point of this time range.
     */
    private ZonedDateTime toTime;
    
    /**
     * Creates an instance that represents this time range.
     * @param from the time that indicates the start point of the time range
     * @param to the time that indicates the end point of the time range
     */
    public TimeRange(ZonedDateTime from, ZonedDateTime to) {
        assert from != null && from != null;
        if (from.isBefore(to)) {
            this.fromTime = from;
            this.toTime = to;
            
        } else {
            this.fromTime = to;
            this.toTime = from;
        }
    }
    
    /**
     * Returns the time that indicates the start point of this time range
     * @return the start point 
     */
    public ZonedDateTime getFrom() {
        return fromTime;
    }
    
    /**
     * Sets the time that indicates the start point of this time range
     * @param time the start point 
     */
    public void setFrom(ZonedDateTime time) {
        fromTime = time;
    }
    
    /**
     * Returns the time that indicates the end point of this time range
     * @return the end point
     */
    public ZonedDateTime getTo() {
        return toTime;
    }
    
    /**
     * Sets the time that indicates the end point of this time range
     * @param time the end point
     */
    public void setTo(ZonedDateTime time) {
        toTime = time;
    }
    
    /**
     * Returns the time duration between the start and end points.
     * @return the time duration for this time range
     */
    public Duration getDuration() {
        return Duration.between(fromTime, toTime);
    }
    
    /**
     * Tests if a specified time is between this time range.
     * @param time the specified time
     * @return <code>true</code> if the time is between this time range, otherwise <code>false</code>
     */
    public boolean isBetween(ZonedDateTime time) {
        return !(fromTime.isAfter(time) || toTime.isBefore(time));
    }
    
    /**
     * Returns the information for debugging.
     * @return the string of the debug information
     */
    @Override
    public String toString() {
        return ChangeOperation.getFormatedTime(fromTime) + "-" + ChangeOperation.getFormatedTime(toTime);
    }
}
