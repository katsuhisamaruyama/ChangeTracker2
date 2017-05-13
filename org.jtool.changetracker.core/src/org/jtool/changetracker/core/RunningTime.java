/*
 *  Copyright 2017
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.changetracker.core;

import java.time.ZonedDateTime;
import java.time.Duration;

/**
 * Measures the running time
 * @author Katsuhisa Maruyama
 *
 */
public class RunningTime {
    
    /**
     * The time when a process starts.
     */
    private static ZonedDateTime start;
    
    /**
     * Invoked when a process starts.
     */
    public static void start() {
        start = ZonedDateTime.now();
        System.out.println("-Starting");
    }
    
    /**
     * Invoked when a process stops.
     */
    public static void stop() {
        ZonedDateTime stop = ZonedDateTime.now();
        Duration duration = Duration.between(start, stop);
        System.out.println("-Running time = " + duration.toMillis()+ " ms" + " " + duration.toNanos() + " ns");
    }
}
