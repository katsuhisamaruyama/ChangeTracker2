/*
 *  Copyright 2016
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.changetracker.operation;

import java.time.ZonedDateTime;

/**
 * Stores information on a copy operation.
 * @author Katsuhisa Maruyama
 */
public class CopyOperation extends ChangeOperation implements ICodeOperation {
    
    /**
     * The action of a copy operation.
     */
    public enum Action {
        COPY;
    }
    
    /**
     * The leftmost offset of the text changed by this document change operation.
     */
    protected int start;
    
    /**
     * The contents of the text copied by this document change operation.
     */
    protected String copiedText;
    
    /**
     * Creates an instance storing information on this copy operation.
     * @param time the time when the copy operation was performed
     * @param branch the branch name of a resource on which the copy operation was performed
     * @param path the path name of a resource on which the copy operation was performed
     * @param author the author's name
     */
    public CopyOperation(ZonedDateTime time, String path, String branch, String author) {
        super(time, Type.COPY, path, branch, Action.COPY.toString(), author);
        this.start = -1;
        this.copiedText = "";
    }
    
    /**
     * Creates an instance storing information on this copy operation.
     * @param time the time when the copy operation was performed
     * @param branch the branch name of a resource on which the copy operation was performed
     * @param path the path name of a resource on which the copy operation was performed
     * @param action the action of the the copy operation
     */
    public CopyOperation(ZonedDateTime time, String path, String branch) {
        this(time, path, branch, ChangeOperation.getUserName());
    }
    
    /**
     * Returns the leftmost offset of the text changed by this copy operation.
     * @return the leftmost offset of the copied text
     */
    @Override
    public int getStart() {
        return start;
    }
    
    /**
     * Sets the leftmost offset of the text changed by this copy operation.
     * @return the leftmost offset of the copied text
     */
    public void setStart(int start) {
        this.start = start;
    }
    
    /**
     * Returns the contents of the text inserted by this document change operation.
     * @return the empty string
     */
    @Override
    public String getInsertedText() {
        return "";
    }
    
    /**
     * Returns the contents of the text deleted by this document change operation.
     * @return the empty string
     */
    @Override
    public String getDeletedText() {
        return "";
    }
    
    /**
     * Returns the contents of the text copied by this copy operation.
     * @return the contents of the copied text, or the empty string
     */
    @Override
    public String getCopiedText() {
        return copiedText;
    }
    
    /**
     * Sets the contents of the text inserted by this copy operation.
     * @param text the contents of the copied text
     */
    public void setCopiedText(String text) {
        assert text != null;
        copiedText = text;
    }
    
    /**
     * Returns the contents of the text cut or copied by this change operation.
     * @return the contents of the cut or copied text, or the empty string
     */
    @Override
    public String getCutOrCopiedText() {
        return getCopiedText();
    }
    
    /**
     * Tests this document change operation cuts the text.
     * @return always <code>false</code>
     */
    @Override
    public boolean isCut() {
        return false;
    }
    
    /**
     * Tests this document change operation copies the text.
     * @return always <code>false</code>
     */
    @Override
    public boolean isCopy() {
        return true;
    }
    
    /**
     * Tests this document change operation pastes the text.
     * @return always <code>false</code>
     */
    @Override
    public boolean isPaste() {
        return false;
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
        buf.append(" copy=[" + getShortText(copiedText) + "]");
        return buf.toString();
    }
}
