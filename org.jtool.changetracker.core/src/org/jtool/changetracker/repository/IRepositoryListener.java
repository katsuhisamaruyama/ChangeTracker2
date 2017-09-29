/*
 *  Copyright 2017
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.changetracker.repository;

/**
 * Defines the listener interface for receiving events of repository changes.
 * @author Katsuhisa Maruyama
 */
public interface IRepositoryListener {
    
    /**
     * Invoked before a repository change event is about to occur.
     * @param evt the sent event
     */
    public void aboutTo(RepositoryEvent evt);
    
    
    /**
     * Invoked after a repository change event occurred.
     * @param evt the sent event
     */
    public void changed(RepositoryEvent evt);
}
