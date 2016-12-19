/*
 *  Copyright 2016
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.changetracker.operation;

import java.time.ZonedDateTime;

/**
 * Defines an interface that accesses information about document and copy operations.
 * @author Katsuhisa Maruyama
 */
public interface ICodeOperation {
    
    /**
     * Returns the time when this change operation was performed.
     * @return the time of the change operation
     */
    public ZonedDateTime getTime();
    
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
     * Returns the leftmost offset of the text affected by this change operation.
     * @return the leftmost offset of the affected text
     */
    public int getStart();
    
    /**
     * Returns the contents of the text inserted by this change operation.
     * @return the contents of the inserted text, or the empty string
     */
    public String getInsertedText();
    
    /**
     * Returns the contents of the text deleted by this change operation.
     * @return the contents of the deleted text, or the empty string
     */
    public String getDeletedText();
    
    /**
     * Returns the contents of the text copied by this change operation.
     * @return the contents of the copied text, or the empty string
     */
    public String getCopiedText();
    
    /**
     * Returns the contents of the text cut or copied by this change operation.
     * @return the contents of the cut or copied text, or the empty string
     */
    public String getCutOrCopiedText();
    
    /**
     * Tests this document change operation cuts the text.
     * @return <code>true</code> if the change operation cuts the text, otherwise <code>false</code>
     */
    public boolean isCut();
    
    /**
     * Tests this document change operation copies the text.
     * @return <code>true</code> if the change operation copies the text, otherwise <code>false</code>
     */
    public boolean isCopy();
    
    /**
     * Tests this document change operation pastes the text.
     * @return <code>true</code> if the change operation pastes the text, otherwise <code>false</code>
     */
    public boolean isPaste();
    
    /**
     * Returns the string for printing, which does not contain a new line character at its end. 
     * @return the string for printing
     */
    public String toString();
}
