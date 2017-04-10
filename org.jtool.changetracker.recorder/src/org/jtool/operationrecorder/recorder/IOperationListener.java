/*
 *  Copyright 2016
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.operationrecorder.recorder;

import org.jtool.changetracker.operation.IChangeOperation;

/**
 * Defines a listener interface for receiving a code change operation.
 * @author Katsuhisa Maruyama
 */
public interface IOperationListener {
    
    /**
     * Receives a code change operation when it is added.
     * @param operation the code change operation
     */
    public void operationAdded(IChangeOperation operation);
}
