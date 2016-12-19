/*
 *  Copyright 2016
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.changetracker.operation;

import java.time.ZonedDateTime;

/**
 * Stores information on a document operation.
 * @author Katsuhisa Maruyama
 */
public class DocumentOperation extends ChangeOperation implements ICodeOperation {
    
    /**
     * The action of a document operation.
     */
    public enum Action {
        EDIT, CUT, PASTE, UNDO, REDO, DIFF;
    }
    
    /**
     * The leftmost offset of the text changed by this document operation.
     */
    protected int start;
    
    /**
     * The contents of the text inserted by this document operation.
     */
    protected String insertedText;
    
    /**
     * The contents of the text deleted by this document operation.
     */
    protected String deletedText;
    
    /**
     * Creates an instance storing information on this document operation.
     * @param time the time when the document operation was performed
     * @param path the path name of a resource on which the document operation was performed
     * @param branch the branch name of a resource on which the document operation was performed
     * @param action the action of the document operation
     * @param author the author's name
     */
    public DocumentOperation(ZonedDateTime time, String path, String branch, String action, String author) {
        super(time, Type.DOCUMENT, path, branch, action, author);
        this.start = -1;
        this.insertedText = "";
        this.deletedText = "";
        this.bundleId = 0;
    }
    
    /**
     * Creates an instance storing information on this document operation.
     * @param time the time when the document operation was performed
     * @param path the path name of a resource on which the document operation was performed
     * @param branch the branch name of a resource on which the document operation was performed
     * @param action the action of the document change operation
     */
    public DocumentOperation(ZonedDateTime time, String path, String branch, String action) {
        this(time, path, branch, action, ChangeOperation.getUserName());
    }
    
    /**
     * Sets the action of this document operation.
     * @param action the action of the document operation
     */
    public void setAction(String action) {
        this.action = action;
    }
    
    /**
     * Returns the leftmost offset of the text changed by this document operation.
     * @return the leftmost offset of the changed text
     */
    @Override
    public int getStart() {
        return start;
    }
    
    /**
     * Sets the leftmost offset of the text changed by this document operation.
     * @param start the leftmost offset of the inserted and/or deleted text
     */
    public void setStart(int start) {
        this.start = start;
    }
    
    /**
     * Returns the contents of the text inserted by this document operation.
     * @return the contents of the inserted text
     */
    @Override
    public String getInsertedText() {
        return insertedText;
    }
    
    /**
     * Sets the contents of the text inserted by this document operation.
     * @param text the contents of the inserted text
     */
    public void setInsertedText(String text) {
        assert text != null;
        insertedText = text;
    }
    
    /**
     * Returns the contents of the text deleted by this document operation.
     * @return the contents of the deleted text
     */
    @Override
    public String getDeletedText() {
        return deletedText;
    }
    
    /**
     * Sets the contents of the text deleted by this document operation.
     * @param text the contents of the deleted text
     */
    public void setDeletedText(String text) {
        assert text != null;
        deletedText = text;
    }
    
    /**
     * Returns the contents of the text copied by this copy operation.
     * @return the empty string
     */
    @Override
    public String getCopiedText() {
        return "";
    }
    
    /**
     * Returns the contents of the text cut or copied by this change operation.
     * @return the contents of the cut or copied text, or the empty string
     */
    @Override
    public String getCutOrCopiedText() {
        return getDeletedText();
    }
    
    /**
     * Tests this document change operation edits the text.
     * @return <code>true</code> if the change operation edits the text, otherwise <code>false</code>
     */
    public boolean isEdit() {
        return action.equals(Action.EDIT.toString());
    }
    
    /**
     * Tests this document change operation cuts the text.
     * @return <code>true</code> if the change operation cuts the text, otherwise <code>false</code>
     */
    @Override
    public boolean isCut() {
        return action.equals(Action.CUT.toString());
    }
    
    /**
     * Tests this document change operation copies the text.
     * @return always <code>false</code>
     */
    @Override
    public boolean isCopy() {
        return false;
    }
    
    /**
     * Tests this document change operation pastes the text.
     * @return <code>true</code> if the change operation pastes the text, otherwise <code>false</code>
     */
    @Override
    public boolean isPaste() {
        return action.equals(Action.PASTE.toString());
    }
    
    /**
     * Tests this document change operation undoes the text.
     * @return <code>true</code> if the change operation undoes the text, otherwise <code>false</code>
     */
    public boolean isUndo() {
        return action.equals(Action.UNDO.toString());
    }
    
    /**
     * Tests this document change operation redoes the text.
     * @return <code>true</code> if the change operation redoes the text, otherwise <code>false</code>
     */
    public boolean isRedo() {
        return action.equals(Action.REDO.toString());
    }
    
    /**
     * Tests this document change operation automatically changes the text.
     * @return <code>true</code> if the change operation automatically changes the text, otherwise <code>false</code>
     */
    public boolean isDiff() {
        return action.equals(Action.DIFF.toString());
    }
    
    /**
     * Tests this command operation refactors any code.
     * @return <code>true</code> if the command operation refactors any code, otherwise <code>false</code>
     */
    public boolean isRefactoring() {
        return action.equals(CommonAction.REFACTOING.toString()) ||
               action.equals(CommonAction.REFACTOING_UNDO.toString()) ||
               action.equals(CommonAction.REFACTOING_REDO.toString());
    }
    
    /**
     * Tests this command operation completes any code.
     * @return <code>true</code> if the command operation completes any code, otherwise <code>false</code>
     */
    public boolean isCodeComplete() {
        return action.equals(CommonAction.QUICK_ASSIST.toString()) ||
               action.equals(CommonAction.CONTENT_ASSIST.toString());
    }
    
    /**
     * Tests if this document change operation only inserts any text.
     * @return <code>true</code> if the inserted text is not empty but the deleted text is empty, otherwise <code>false</code>
     */
    public boolean isInsertion() {
        return insertedText.length() != 0 && deletedText.length() == 0;
    }
    
    /**
     * Tests if this document change operation only deletes any text.
     * @return <code>true</code> if the deleted text is not empty but the inserted text is empty, otherwise <code>false</code>
     */
    public boolean isDeletion() {
        return insertedText.length() == 0 && deletedText.length() != 0;
    }
    
    /**
     * Tests if this document change operation replaces any text with another one.
     * @return <code>true</code> if the inserted and deleted texts are not empty, otherwise <code>false</code>
     */
    public boolean isReplace() {
        return insertedText.length() != 0 && deletedText.length() != 0;
    }
    
    /**
     * Returns the string for printing, which does not contain a new line character at its end.
     * @return the string for printing
     */
    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder();
        buf.append(super.toString());
        
        buf.append(" offset=" + start);
        buf.append(" ins=[" + getShortText(insertedText) + "]");
        buf.append(" del=[" + getShortText(deletedText) + "]");
        if (canBeBundled()) {
            buf.append(" bundle=" + bundleId);
        }
        return buf.toString();
    }
}
