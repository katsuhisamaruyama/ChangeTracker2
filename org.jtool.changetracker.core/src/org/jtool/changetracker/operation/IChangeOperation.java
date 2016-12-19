/*
 *  Copyright 2016
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
     * The sort of an operation.
     */
    public enum Type {
        DOCUMENT, COPY, COMMAND, FILE, RESOURCE, GIT;
    }
    
    /**
     * The string that indicates global path.
     */
    public static final String GLOBAL_PATH = "/";
    
    /**
     * Returns the time when this change operation was performed.
     * @return the time of the change operation
     */
    public ZonedDateTime getTime();
    
    /**
     * Returns the time when this change operation was performed.
     * @return the time of the change operation
     */
    public long getTimeAsLong();
    
    /**
     * Returns the type of this change operation.
     * @return the type of the change operation
     */
    public Type getType();
    
    /**
     * Returns the path name of the file on which this change operation was performed.
     * @return the path name of the change operation
     */
    public String getPath();
    
    /**
     * The name of the branch of a resource on which this macro was performed.
     * @return the branch name of the change operation
     */
    public String getBranch();
    
    /**
     * Returns the action type of this change operation.
     * @return the action type of the change operation
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
     * Tests if this code change operations can be bundled.
     * @return <code>true</code> if this code change operations can be bundled, otherwise <code>false</code>
     */
    public boolean canBeBundled();
    
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
     * Tests if this change operation represents any commend.
     * @return <code>true</code> if the change operation represents any commend, otherwise <code>false</code>
     */
    public boolean isCommand();
    
    /**
     * Tests if this change operation operates any file.
     * @return <code>true</code> if the change operation operates any file, otherwise <code>false</code>
     */
    public boolean isFile();
    
    /**
     * Tests if this change operation changes any resource.
     * @return <code>true</code> if the change operation changes any resource, otherwise <code>false</code>
     */
    public boolean isResource();
    
    /**
     * Tests if this change operation represents any git command.
     * @return <code>true</code> if the change operation represents any git command, otherwise <code>false</code>
     */
    public boolean isGit();
    
    /**
     * Tests if this change operation is performed on a resource that exists in a path of a branch.
     * @param branch the branch of the resource
     * @param path the path of the resource
     * @return <code>true</code> if this change operation is performed on the specified resource, otherwise <code>false</code>
     */
    public boolean isPerformedOn(String branch, String path);
    
    /**
     * Tests if this operation is the same as a given one.
     * @param op the given operation
     * @return <code>true</code> if the two operations are the same, otherwise <code>false</code>
     */
    public boolean equals(IChangeOperation op);
    
    /**
     * Returns the string for printing, which does not contain a new line character at its end. 
     * @return the string for printing
     */
    public String toString();
}
