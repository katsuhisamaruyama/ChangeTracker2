/*
 *  Copyright 2017
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.changetracker.repository;

/**
 * Defines an interface for handling received events of repository changes.
 * @author Katsuhisa Maruyama
 */
public interface IRepositoryHandler extends IRepositoryListener {
    
    /**
     * Invoked to initialize this handler before receiving repository change events.
     */
    public void initialize();
    
    /**
     * Invoked to terminate this handler.
     */
    public void terminate();
}
