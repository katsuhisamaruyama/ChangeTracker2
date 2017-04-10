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
public abstract class CodeOperation extends ChangeOperation implements ICodeOperation {
    
    /**
     * The leftmost offset of the text affected by this change operation.
     */
    protected int start = -1;
    
    /**
     * Creates an instance storing information about this change operation.
     * @param time the time when the change operation was performed
     * @param type the type of the change operation
     * @param path the path of a file on which the change operation was performed
     * @param branch the branch of a file on which the change operation was performed
     * @param action the action of the change operation
     * @param author the author's name
     */
    protected CodeOperation(ZonedDateTime time, Type type, String path, String branch, String action, String author) {
        super(time, type, path, branch, action, author);
    }
    
    /**
     * Creates an instance storing information about this change operation.
     * @param time the time when the change operation was performed
     * @param type the type of the change operation
     * @param path the path of a file on which the change operation was performed
     * @param branch the branch of a file on which the change operation was performed
     * @param action the action of the change operation
     */
    protected CodeOperation(ZonedDateTime time, Type type, String path, String branch, String action) {
        super(time, type, path, branch, action);
    }
    
    /**
     * Returns the leftmost offset of the text affected by this change operation.
     * @return the leftmost offset of the affected text
     */
    @Override
    public int getStart() {
        return start;
    }
    
    /**
     * Sets the leftmost offset of the text affected by this change operation.
     * @param start the leftmost offset of the affected text
     */
    public void setStart(int start) {
        this.start = start;
    }
    /**
     * Tests this change operation cuts the text.
     * @return <code>true</code> if the change operation cuts the text, otherwise <code>false</code>
     */
    @Override
    public boolean isCut() {
        return action.equals(Action.CUT.toString());
    }
    
    /**
     * Tests this change operation edits the text in several ways.
     * @return <code>true</code> if the change operation edits the text, otherwise <code>false</code>
     */
    public boolean isGeneralEdit() {
        return (isEdit() || isUndo() || isRedo() || isContentChange() ||  isRefactoring()  || isCodeComplete());
    }
        
    /**
     * Tests this change operation edits the text.
     * @return <code>true</code> if the change operation edits the text, otherwise <code>false</code>
     */
    public boolean isEdit() {
        return action.equals(Action.EDIT.toString());
    }
    
    /**
     * Tests this change operation copies the text.
     * @return <code>true</code> if the change operation copies the text, otherwise <code>false</code>
     */
    @Override
    public boolean isCopy() {
        return action.equals(Action.COPY.toString());
    }
    
    /**
     * Tests this change operation pastes the text.
     * @return <code>true</code> if the change operation pastes the text, otherwise <code>false</code>
     */
    @Override
    public boolean isPaste() {
        return action.equals(Action.PASTE.toString());
    }
    
    /**
     * Tests this change operation undoes the affect of the text.
     * @return <code>true</code> if the change operation undoes the text, otherwise <code>false</code>
     */
    public boolean isUndo() {
        return action.equals(Action.UNDO.toString());
    }
    
    /**
     * Tests this document change operation redoes the affect of the text.
     * @return <code>true</code> if the change operation redoes the text, otherwise <code>false</code>
     */
    public boolean isRedo() {
        return action.equals(Action.REDO.toString());
    }
    
    /**
     * Tests this document change operation automatically changes the text.
     * @return <code>true</code> if the change operation automatically changes the text, otherwise <code>false</code>
     */
    public boolean isContentChange() {
        return action.equals(Action.CONTENT_CHANGE.toString());
    }
    
    /**
     * Tests this change operation refactors the code.
     * @return <code>true</code> if the change operation refactors the code, otherwise <code>false</code>
     */
    public boolean isRefactoring() {
        return action.equals(Action.REFACTOING.toString()) ||
               action.equals(Action.REFACTOING_UNDO.toString()) ||
               action.equals(Action.REFACTOING_REDO.toString());
    }
    
    /**
     * Tests this change operation completes the code.
     * @return <code>true</code> if the change operation completes the code, otherwise <code>false</code>
     */
    public boolean isCodeComplete() {
        return action.equals(Action.QUICK_ASSIST.toString()) ||
               action.equals(Action.CONTENT_ASSIST.toString());
    }
    
    /**
     * Returns the string for printing.
     * @return the string for printing
     */
    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder();
        buf.append(super.toString());
        buf.append(" offset=" + start);
        return buf.toString();
    }
}
