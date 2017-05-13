/*
 *  Copyright 2017
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.changetracker.operation;

import java.time.ZonedDateTime;
import java.util.List;

import org.jtool.changetracker.repository.ChangeTrackerFile;

/**
 * Defines an interface that accesses information about the all kinds of change operations.
 * @author Katsuhisa Maruyama
 */
public interface IChangeOperation {
    
    /**
     * The type of a change operation.
     */
    public enum Type {
        DOCUMENT, COPY, FILE, COMMAND, REFACTOR;
    }
    
    /**
     * Returns the qualified name of this change operation.
     * @return the qualified name
     */
    public String getQualifiedName();
    
    /**
     * Returns the time when this change operation was performed.
     * @return the time of the change operation
     */
    public ZonedDateTime getTime();
    
    /**
     * Returns the time when this change operation was performed.
     * @return the <code>long</code> value
     */
    public long getTimeAsLong();
    
    /**
     * Returns the time when this change operation was performed.
     * @return the formatted <code>String</code> value
     */
    public String getFormatedTime();
    
    /**
     * Returns the time when this change operation was performed.
     * @return the <code>String</code> value
     */
    public String getTimeAsString();
    
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
     * Returns the name of a project containing a resource on which this change operation was performed.
     * @return the project name
     */
    public String getProjectName();
    
    /**
     * Returns the name of a package containing a resource on which this change operation was performed.
     * @return the package name
     */
    public String getPackageName();
    
    /**
     * Returns the name of a file on which this change operation was performed.
     * @return the file name without its location information
     */
    public String getFileName();
    
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
     * Tests if this change operation edits or copies any text of code.
     * @return <code>true</code> if the change operation edits or copies any text, otherwise <code>false</code>
     */
    public boolean isDocumentOrCopy();
    
    /**
     * Tests if this change operation is related to a file.
     * @return <code>true</code> if the change operation is related to a file, otherwise <code>false</code>
     */
    public boolean isFile();
    
    /**
     * Tests if this change operation is related to a command.
     * @return <code>true</code> if the change operation is related to a command, otherwise <code>false</code>
     */
    public boolean isCommand();
    
   /**
    * Tests if this change operation is related to refactoring.
    * @return <code>true</code> if the change operation is related to refactoring, otherwise <code>false</code>
    */
    public boolean isRefactor();
    
    /**
     * Tests if this change operation is performed on a resource represented by a branch and a path.
     * @param branch the branch of the resource
     * @param path the path of the resource
     * @return <code>true</code> if this change operation is performed on the resource, otherwise <code>false</code>
     */
    public boolean isPerformedOn(String branch, String path);
    
    /**
     * Obtains change operations that affect this change operation.
     * Note that this method execution requires much time for huge number of change operations.
     * Instead an operation history graph can be used if you code will invoke this method many times.
     * @return the collection of the affecting change operations
     */
    public List<ICodeOperation> getAffectingOperations();
    
    /**
     * Tests if this change operation depends on a given change operation.
     * @param op the change operation that might affect this change operation
     * @return <code>true</code> if this change operation depends on the given operation, otherwise <code>false</code>
     */
    public boolean dependsOn(IChangeOperation op);
    
    /**
     * Returns information about a file that this change operation affects.
     * @return the file information
     */
    public ChangeTrackerFile getFile();
    
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
