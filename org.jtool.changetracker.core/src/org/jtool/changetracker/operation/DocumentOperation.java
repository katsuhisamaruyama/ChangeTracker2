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
     * @param pathinfo information about path of a resource on which the document operation was performed
     * @param action the action of the document operation
     * @param author the author's name
     */
    public DocumentOperation(ZonedDateTime time, CTPath pathinfo, String action, String author) {
        super(time, Type.DOCUMENT, pathinfo, action, author);
    }
    
    /**
     * Creates an instance storing information about this document operation.
     * @param time the time when the document operation was performed
     * @param pathinfo information about path of a resource on which the document operation was performed
     * @param action the action of the document change operation
     */
    public DocumentOperation(ZonedDateTime time, CTPath pathinfo, String action) {
        super(time, Type.DOCUMENT, pathinfo, action);
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
     * Obtains change operations that affect this document operation.
     * @return the collection of the affecting change operations
     */
    public List<ICodeOperation> getAffectingOperations() {
        List<ICodeOperation> retops = super.getAffectingOperations();
        ICodeOperation cc = getCutOrCopyOperationForPaste();
        if (cc != null) {
            retops.add(cc);
        }
        ChangeOperation.sort(retops);
        return retops;
    }
    
    /**
     * Obtains a cut or copy operation corresponding to this change operation.
     * @return the cut or copy operation, or <code>null</code> if it was not found or thus change operation does not represent a paste action.
     */
    public ICodeOperation getCutOrCopyOperationForPaste() {
        if (isPaste()) {
            List<IChangeOperation> ops = fileInfo.getProject().getOperations();
            for (int idx = ops.size() - 1; idx >= 0; idx--) {
                if (ops.get(idx).isDocument()) {
                    ICodeOperation cop = (ICodeOperation)ops.get(idx);
                    if ((cop.isCopy() || cop.isCut()) && cop.getTime().isBefore(getTime())) {
                        String ccText = getCutOrCopyText(cop).trim();
                        String pasteText = getInsertedText().trim();
                        if (ccText.equals(pasteText)) {
                            return cop;
                        }
                    }
                }
            }
        }
        return null;
    }
    
    /**
     * Returns text that a change operation cuts or copies.
     * @param op the change operation corresponding to the cut or copy action
     * @return the contents of the cut or copied text, or empty string if no cut or copy action
     */
    private static String getCutOrCopyText(ICodeOperation op) {
        if (op.isDocument()) {
            return ((ICodeOperation)op).getDeletedText();
        } else if (op.isCopy()) {
            return ((ICodeOperation)op).getCopiedText();
        }
        return "";
    }
    
    /**
     * Tests if this document operation depends on a given change operation.
     * @param op the change operation that might affect this document operation
     * @return <code>true</code> if this document operation depends on the given operation, otherwise <code>false</code>
     */
    @Override
    public boolean dependsOn(IChangeOperation op) {
        if (isPaste()) {
            if (op.isDocument() && (((ICodeOperation)op).isCopy() || ((ICodeOperation)op).isCut())) {
                ICodeOperation cop = getCutOrCopyOperationForPaste();
                return cop != null && cop.equals(op);
            }
        }
        
        if (isDocument()) {
            List<CodeOperation> ops = CodeOperation.getOperations(fileInfo, op.getTime(), getTime());
            if (op.isDocument()) {
                return dependsOnForInsertion((DocumentOperation)op, ops) || dependsOnForDeletion((DocumentOperation)op, ops);
            }
        }
        return false;
    }
    
    /**
     * Tests if this document operation with its inserted text depends on a given document operation.
     * @param op the document operation that might affect this document operation
     * @param ops the collection of change operations for offset adjustment
     * @return <code>true</code> if this document operation depends on, otherwise <code>false</code>
     */
    private boolean dependsOnForInsertion(DocumentOperation op, List<CodeOperation> ops) {
        if (getInsertedText().length() == 0) {
            return false;
        }
        
        if (op.getInsertedText().length() > 0) {
            int offset = DependencyDetector.adjustBackwardOffset(getStart(), ops);
            if (op.getStart() < offset && offset < op.getStart() + op.getInsertedText().length()) {
                return true;
            } else {
                return false;
            }
        }
        return false;
    }
    
    /**
     * Tests if this document operation with its deleted text depends on a given document operation.
     * @param op the document operation that might affect this document operation
     * @param ops the collection of change operations for offset adjustment
     * @return <code>true</code> if this document operation depends on, otherwise <code>false</code>
     */
    private boolean dependsOnForDeletion(DocumentOperation op, List<CodeOperation> ops) {
        if (getDeletedText().length() == 0) {
            return false;
        }
        
        if (op.getInsertedText().length() > 0) {
            for (int o = getStart(); o <= getStart() + getDeletedText().length(); o++) {
                int offset = DependencyDetector.adjustBackwardOffset(o, ops);
                if (op.getStart() <= offset && offset < op.getStart() + op.getInsertedText().length()) {
                    return true;
                }
            }
            return false;
        }
        if (op.getDeletedText().length() > 0) {
            int soffset = DependencyDetector.adjustBackwardOffset(getStart(), ops);
            int eoffset = DependencyDetector.adjustBackwardOffset(getStart() + op.getDeletedText().length(), ops);
            if (soffset< op.getStart() && op.getStart() + op.getDeletedText().length() < eoffset) {
                return true;
            } else {
                return false;
            }
        }
        return false;
    }
    
    /**
     * Tests if this object is the same as a given object.
     * @param obj the object
     * @return <code>true</code> if the two objects are the same, otherwise <code>false</code>
     */
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof CopyOperation) {
            return equals((CopyOperation)obj);
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
        buf.append(" ins=[" + getShortText(insertedText) + "]");
        buf.append(" del=[" + getShortText(deletedText) + "]");
        return buf.toString();
    }
}
