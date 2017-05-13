/*
 *  Copyright 2017
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.changetracker.repository;

/**
 * Defines the listener interface for receiving a changed event.
 * @author Katsuhisa Maruyama
 */
public interface RepositoryChangedListener {
    
    /**
     * Notifies the repository changed event sent to event listener.
     * @param evt the sent event
     */
    public void notify(RepositoryChangedEvent evt);
}
