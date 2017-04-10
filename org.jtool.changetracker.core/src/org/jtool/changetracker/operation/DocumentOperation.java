/*
 *  Copyright 2017
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.changetracker.operation;

import java.time.ZonedDateTime;

/**
 * Stores information about a document operation.
 * @author Katsuhisa Maruyama
 */
public class DocumentOperation extends CodeOperation implements ICodeOperation {
    
    /**
     * The contents of the text inserted by this document operation.
     */
    protected String insertedText = "";
    
    /**
     * The contents of the text deleted by this document operation.
     */
    protected String deletedText = "";
    
    /**
     * Creates an instance storing information about this document operation.
     * @param time the time when the document operation was performed
     * @param path the path of a resource on which the document operation was performed
     * @param branch the branch of a resource on which the document operation was performed
     * @param action the action of the document operation
     * @param author the author's name
     */
    public DocumentOperation(ZonedDateTime time, String path, String branch, String action, String author) {
        super(time, Type.DOCUMENT, path, branch, action, author);
    }
    
    /**
     * Creates an instance storing information about this document operation.
     * @param time the time when the document operation was performed
     * @param path the path of a resource on which the document operation was performed
     * @param branch the branch of a resource on which the document operation was performed
     * @param action the action of the document change operation
     */
    public DocumentOperation(ZonedDateTime time, String path, String branch, String action) {
        super(time, Type.DOCUMENT, path, branch, action);
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
     * Returns the contents of the text copied by this document operation.
     * @return the empty string
     */
    @Override
    public String getCopiedText() {
        return "";
    }
    
    /**
     * Returns the contents of the text cut or copied by this document operation.
     * @return the contents of the cut text, or the empty string
     */
    @Override
    public String getCutOrCopiedText() {
        return getDeletedText();
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
        buf.append(" ins=[" + getShortText(insertedText) + "]");
        buf.append(" del=[" + getShortText(deletedText) + "]");
        if (isCompounded()) {
            buf.append(" compound=" + compoundId);
        }
        return buf.toString();
    }
}
