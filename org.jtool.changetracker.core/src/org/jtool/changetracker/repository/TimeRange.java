/*
 *  Copyright 2017
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.changetracker.repository;

import org.jtool.changetracker.operation.ChangeOperation;
import java.time.ZonedDateTime;
import java.time.Duration;
import java.time.temporal.ChronoUnit;

/**
 * Represents the time range.
 * @author Katsuhisa Maruyama
 */
public class TimeRange {
    
    /**
     * The starting time of this time range.
     */
    private ZonedDateTime fromTime;
    
    /**
     * The ending time of this time range.
     */
    private ZonedDateTime toTime;
    
    /**
     * Creates an instance that represents this time range.
     * @param from the starting time of this time range
     * @param to the ending time of this time range
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
     * Returns the starting time of this time range
     * @return the starting time
     */
    public ZonedDateTime getFrom() {
        return fromTime;
    }
    
    /**
     * Sets the starting time of this time range
     * @param time the starting time
     */
    public void setFrom(ZonedDateTime time) {
        fromTime = time;
    }
    
    /**
     * Returns the ending time of this time range
     * @return the ending time
     */
    public ZonedDateTime getTo() {
        return toTime;
    }
    
    /**
     * Sets the ending time of this time range
     * @param time the ending time
     */
    public void setTo(ZonedDateTime time) {
        toTime = time;
    }
    
    /**
     * Returns the time duration between the starting and ending times.
     * @return the time duration for this time range
     */
    public Duration getDuration() {
        return Duration.between(fromTime, toTime);
    }
    
    /**
     * Returns the time that is a given milliseconds after the starting time.
     * @param ms the milliseconds of the time duration
     * @return the the time after the milliseconds
     */
    public ZonedDateTime afterFromTime(long ms) {
        return fromTime.plus(ms, ChronoUnit.MILLIS);
    }
    
    /**
     * Returns the time that is a given milliseconds before the ending time.
     * @param ms the milliseconds of the time duration
     * @return the time before the milliseconds
     */
    public ZonedDateTime beforeToTime(long ms) {
        return toTime.minus(ms, ChronoUnit.MILLIS);
    }
    
    /**
     * Tests if a specified time is inclusive between this time range.
     * @param time the specified time
     * @return <code>true</code> if the time is between this time range, otherwise <code>false</code>
     */
    public boolean isBetween(ZonedDateTime time) {
        return !(fromTime.isAfter(time) || toTime.isBefore(time));
    }
    
    /**
     * Returns the time duration between the starting and ending times.
     * @return the milliseconds of the time duration
     */
    public long getDurationAsMillis() {
        return fromTime.until(toTime, ChronoUnit.MILLIS);
    }
    
    /**
     * Returns the time duration between the starting time and a given time.
     * @param time the time
     * @return the milliseconds of the time duration
     */
    public long afterFromTime(ZonedDateTime time) {
        return fromTime.until(time, ChronoUnit.MILLIS);
    }
    
    /**
     * Returns the time duration between a given time and the ending time.
     * @param time the time
     * @return the milliseconds of the time duration
     */
    public long beforeFromTime(ZonedDateTime time) {
        return time.until(toTime, ChronoUnit.MILLIS);
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
