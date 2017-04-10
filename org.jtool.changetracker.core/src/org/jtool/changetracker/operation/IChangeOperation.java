/*
 *  Copyright 2017
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.changetracker.operation;

import java.time.ZonedDateTime;

/**
 * Defines an interface that accesses information about the all kinds of change operations.
 * @author Katsuhisa Maruyama
 */
public interface IChangeOperation {
    
    /**
     * The type of a change operation.
     */
    public enum Type {
        DOCUMENT, COPY, FILE;
    }
    
    /**
     * Returns the time when this change operation was performed.
     * @return the time of the change operation
     */
    public ZonedDateTime getTime();
    
    /**
     * Returns the time when this change operation was performed.
     * @return the <code>long</code> value that represents the time of the change operation
     */
    public long getTimeAsLong();
    
    /**
     * Returns the type of this change operation.
     * @return the type of the change operation
     */
    public Type getType();
    
    /**
     * Returns the path of the file on which this change operation was performed.
     * @return the path of the change operation
     */
    public String getPath();
    
    /**
     * The branch of the file on which this macro was performed.
     * @return the branch of the change operation
     */
    public String getBranch();
    
    /**
     * Returns the action of this change operation.
     * @return the action of the change operation
     */
    public String getAction();
    
    /**
     * Returns the author name of this change operation.
     * @return the author's name
     */
    public String getAuthor();
    
    /**
     * Returns the description of this change operation.
     * @return the description
     */
    public String getDescription();
    
    /**
     * Tests if this code change operations is compounded.
     * @return <code>true</code> if this code change operations is compounded, otherwise <code>false</code>
     */
    public boolean isCompounded();
    
    /**
     * Returns the identification number for compounded change operations.
     * @return the identification number for the compounded change operations
     */
    public long getCompoundId();
    
    /**
     * Tests if this change operation edits any text of code.
     * @return <code>true</code> if the change operation edits any text, otherwise <code>false</code>
     */
    public boolean isDocument();
    
    /**
     * Tests if this change operation copies any text of code.
     * @return <code>true</code> if the change operation copies any text, otherwise <code>false</code>
     */
    public boolean isCopy();
    
    /**
     * Tests if this change operation is related to a file.
     * @return <code>true</code> if the change operation is related to a file, otherwise <code>false</code>
     */
    public boolean isFile();
    
    /**
     * Tests if this change operation is performed on a file represented by a branch and a path.
     * @param branch the branch of the file
     * @param path the path of the file
     * @return <code>true</code> if this change operation is performed on the specified file, otherwise <code>false</code>
     */
    public boolean isPerformedOn(String branch, String path);
    
    /**
     * Tests if this change operation is the same as a given one.
     * @param op the change operation
     * @return <code>true</code> if the two change operations are the same, otherwise <code>false</code>
     */
    public boolean equals(IChangeOperation op);
    
    /**
     * Returns the string for printing.
     * @return the string for printing
     */
    public String toString();
}
