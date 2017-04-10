/*
 *  Copyright 2017
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
     * The action of a document operation.
     */
    public enum Action {
        EDIT, CUT, COPY, PASTE, UNDO, REDO, CONTENT_CHANGE,
        REFACTOING, REFACTOING_UNDO, REFACTOING_REDO,
        QUICK_ASSIST, CONTENT_ASSIST;
    }
    
    /**
     * Returns the time when this change operation was performed.
     * @return the time of the change operation
     */
    public ZonedDateTime getTime();
    
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
     * Tests this change operation edits the text.
     * @return <code>true</code> if the change operation edits the text, otherwise <code>false</code>
     */
    public boolean isGeneralEdit();
    
    /**
     * Tests this change operation cuts the text.
     * @return <code>true</code> if the change operation cuts the text, otherwise <code>false</code>
     */
    public boolean isCut();
    
    /**
     * Tests this change operation copies the text.
     * @return <code>true</code> if the change operation copies the text, otherwise <code>false</code>
     */
    public boolean isCopy();
    
    /**
     * Tests this change operation pastes the text.
     * @return <code>true</code> if the change operation pastes the text, otherwise <code>false</code>
     */
    public boolean isPaste();
    
    /**
     * Returns the string for printing, which does not contain a new line character at its end. 
     * @return the string for printing
     */
    public String toString();
}
