/*
 *  Copyright 2017
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.changetracker.operation;

import org.jtool.changetracker.dependencyanalyzer.DependencyDetector;
import org.jtool.changetracker.repository.CTPath;
import java.util.List;
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
     * @param pathinfo information about path of a resource on which the copy operation was performed
     * @param author the author's name
     */
    public CopyOperation(ZonedDateTime time, CTPath pathinfo, String author) {
        super(time, Type.COPY, pathinfo, Action.COPY.toString(), author);
    }
    
    /**
     * Creates an instance storing information about this copy operation.
     * @param time the time when the copy operation was performed
     * @param pathinfo information about path of a resource on which the copy operation was performed
     * @param path the path of a file on which the copy operation was performed
     */
    public CopyOperation(ZonedDateTime time, CTPath pathinfo) {
        super(time, Type.COPY, pathinfo,  Action.COPY.toString());
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
     * Tests if this copy operation depends on a given change operation.
     * @param op the change operation that might affect this copy operation
     * @return <code>true</code> if this copy operation depends on the given operation, otherwise <code>false</code>
     */
    @Override
    public boolean dependsOn(IChangeOperation op) {
        List<CodeOperation> ops = CodeOperation.getOperations(fileInfo, op.getTime(), getTime());
        if (isDocument()) {
            return dependsOnForCopy((DocumentOperation)op, ops);
        }
        return false;
    }
    
    /**
     * Tests if this copy operation depends on a given document operation.
     * @param op the document operation that might affect this copy operation
     * @param ops the collection of change operations for offset adjustment
     * @return <code>true</code> if this copy operation depends on, otherwise <code>false</code>
     */
    private boolean dependsOnForCopy(DocumentOperation op, List<CodeOperation> ops) {
        if (op.getInsertedText().length() > 0) {
            for (int o = getStart(); o <= getStart() + getCopiedText().length(); o++) {
                int offset = DependencyDetector.adjustBackwardOffset(o, ops);
                if (op.getStart() <= offset && offset < op.getStart() + op.getInsertedText().length()) {
                    return true;
                }
            }
            return false;
        }
        if (op.getDeletedText().length() > 0) {
            int soffset = DependencyDetector.adjustBackwardOffset(getStart(), ops);
            int eoffset = DependencyDetector.adjustBackwardOffset(getStart() + op.getCopiedText().length(), ops);
            if (soffset< op.getStart() && op.getStart() + op.getDeletedText().length() < eoffset) {
                return true;
            } else {
                return false;
            }
        }
        return false;
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
