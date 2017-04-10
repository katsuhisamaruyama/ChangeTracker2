/*
 *  Copyright 2017
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.changetracker.operation;

import java.time.ZonedDateTime;

/**
 * Stores information about a copy operation.
 * @author Katsuhisa Maruyama
 */
public class CopyOperation extends CodeOperation implements ICodeOperation {
    
    /**
     * The contents of the text copied by this document change operation.
     */
    protected String copiedText = "";
    
    /**
     * Creates an instance storing information about this copy operation.
     * @param time the time when the copy operation was performed
     * @param path the path of a file on which the copy operation was performed
     * @param branch the branch of a file on which the copy operation was performed
     * @param author the author's name
     */
    public CopyOperation(ZonedDateTime time, String path, String branch, String author) {
        super(time, Type.COPY, path, branch, Action.COPY.toString(), author);
    }
    
    /**
     * Creates an instance storing information about this copy operation.
     * @param time the time when the copy operation was performed
     * @param branch the branch of a file on which the copy operation was performed
     * @param path the path of a file on which the copy operation was performed
     */
    public CopyOperation(ZonedDateTime time, String path, String branch) {
        super(time, Type.COPY, path, branch, Action.COPY.toString());
    }
    
    /**
     * Returns the contents of the text inserted by this copy operation.
     * @return the empty string
     */
    @Override
    public String getInsertedText() {
        return "";
    }
    
    /**
     * Returns the contents of the text deleted by this copy operation.
     * @return the empty string
     */
    @Override
    public String getDeletedText() {
        return "";
    }
    
    /**
     * Returns the contents of the text copied by this copy operation.
     * @return the contents of the copied text
     */
    @Override
    public String getCopiedText() {
        return copiedText;
    }
    
    /**
     * Sets the contents of the text copied by this copy operation.
     * @param text the contents of the copied text
     */
    public void setCopiedText(String text) {
        assert text != null;
        copiedText = text;
    }
    
    /**
     * Returns the contents of the text cut or copied by this change operation.
     * @return the contents of the copied text, or the empty string
     */
    @Override
    public String getCutOrCopiedText() {
        return getCopiedText();
    }
    
    /**
     * Returns the string for printing.
     * @return the string for printing
     */
    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder();
        buf.append(super.toString());
        buf.append(" copy=[" + getShortText(copiedText) + "]");
        return buf.toString();
    }
}
