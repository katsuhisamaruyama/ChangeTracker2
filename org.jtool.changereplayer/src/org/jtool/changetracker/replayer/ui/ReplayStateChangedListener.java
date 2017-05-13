/*
 *  Copyright 2017
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.changetracker.replayer.ui;

import java.util.EventListener;

/**
 * Defines the listener interface for receiving an event that represents the change of replay state.
 * @author Katsuhisa Maruyama
 */
public interface ReplayStateChangedListener extends EventListener {
    
    /**
     * Notifies an event that represents the change of replay state.
     * @param evt the event
     */
    public void notify(ReplayStateChangedEvent evt);
}
