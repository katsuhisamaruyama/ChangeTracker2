/*
 *  Copyright 2016
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.changetracker.operation;

import java.time.ZonedDateTime;

/**
 * Stores information on a command operation.
 * @author Katsuhisa Maruyama
 */
public class CommandOperation extends ChangeOperation {
    
    /**
     * The action of a command operation.
     */
    public enum Action {
        EXECUTION;
    }
    
    /**
     * Creates an instance storing information on this command operation.
     * @param time the time when the command operation was performed
     * @param path the path name of a resource on which the command operation was performed
     * @param branch the branch name of a resource on which the command operation was performed
     * @param action the action of the command operation
     * @param author the author's name
     */
    public CommandOperation(ZonedDateTime time, String path, String branch, String action, String author) {
        super(time, Type.COMMAND, path, branch, action, author);
    }
    
    /**
     * Creates an instance storing information on this command operation.
     * @param time the time when the command operation was performed
     * @param path the path name of a resource on which the command operation was performed
     * @param branch the branch name of a resource on which the change operation was performed
     * @param action the action of the command operation
     * @param author the author's name
     */
    public CommandOperation(ZonedDateTime time, String path, String branch, String action) {
        this(time, path, branch, action, ChangeOperation.getUserName());
    }
    
    /**
     * Returns the string for printing, which does not contain a new line character at its end.
     * @return the string for printing
     */
    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder();
        buf.append(super.toString());
        
        return buf.toString();
    }
}
