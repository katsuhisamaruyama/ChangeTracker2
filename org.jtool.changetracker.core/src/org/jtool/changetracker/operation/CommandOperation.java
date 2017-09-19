/*
 *  Copyright 2017
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.changetracker.operation;

import java.time.ZonedDateTime;

import org.jtool.changetracker.repository.CTPath;

/**
 * Stores information on a command operation.
 * @author Katsuhisa Maruyama
 */
public class CommandOperation extends ChangeOperation {
    
    /**
     * The action of a command operation.
     */
    public enum Action {
        EXECUTION, REFACTORING, CONTENT_ASSIST, QUICK_ASSIST, GIT;
    }
    
    /**
     * The string representing the command name of this command operation.
     */
    private String name;
    
    /**
     * Creates an instance storing information on this command operation.
     * @param time the time when the command operation was performed
     * @param pathinfo information about path of a resource on which the command operation was performed
     * @param action the action of the command operation
     * @param author the author's name
     */
    public CommandOperation(ZonedDateTime time, CTPath pathinfo, String action, String author) {
        super(time, Type.COMMAND, pathinfo, action, author);
    }
    
    /**
     * Creates an instance storing information on this command operation.
     * @param time the time when the command operation was performed
     * @param pathinfo information about path of a resource on which the command operation was performed
     * @param action the action of the command operation
     */
    public CommandOperation(ZonedDateTime time, CTPath pathinfo, String action) {
        this(time, pathinfo, action, ChangeOperation.getUserName());
    }
    
    /**
     *Sets the string representing the command name of this command operation.
     * @param name the command name of the command operation
     */
    public void setName(String name) {
        assert name != null;
        this.name = name;
    }
    
    /**
     * Returns the string representing the command name of this command operation.
     * @return the command name of the command operation
     */
    public String getName() {
        return name;
    }
    
    /**
     * Returns the string for printing.
     * @return the string for printing
     */
    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder();
        buf.append(super.toString());
        buf.append(" name=[" + name + "]");
        return buf.toString();
    }
}
