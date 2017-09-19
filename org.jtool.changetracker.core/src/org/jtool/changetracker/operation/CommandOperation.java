/*
 *  Copyright 2017
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.changetracker.operation;

import org.jtool.changetracker.repository.CTPath;
import java.time.ZonedDateTime;

/**
 * Stores information on a command operation.
 * @author Katsuhisa Maruyama
 */
public class CommandOperation extends ChangeOperation {
    
    /**
     * The action of a file operation.
     */
    public enum Action {
        EXECUTE;
    }
    
    /**
     * The string representing the command identification.
     */
    private String commandId;
    
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
     * Sets command information.
     * @param commandId the string representing the command identification
     */
    public void setCommandId(String commandId) {
        assert commandId != null;
        this.commandId = commandId;
    }
    
    /**
     * Returns command information.
     * @return the string representing the command identification
     */
    public String getCommandId() {
        return commandId;
    }
    
    /**
     * Returns the string for printing.
     * @return the string for printing
     */
    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder();
        buf.append(super.toString());
        buf.append(" commandId=[" + commandId + "]");
        return buf.toString();
    }
}
