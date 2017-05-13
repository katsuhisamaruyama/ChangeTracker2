/*
 *  Copyright 2017
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.changetracker.recorder;

import org.jtool.changetracker.operation.IChangeOperation;

/**
 * Defines a listener interface for receiving a change operation.
 * @author Katsuhisa Maruyama
 */
public interface IChangeOperationListener {
    
    /**
     * Receives a change operation when it is added.
     * @param operation the code change operation
     */
    public void operationAdded(IChangeOperation operation);
}
