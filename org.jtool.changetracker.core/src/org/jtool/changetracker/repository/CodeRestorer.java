/*
 *  Copyright 2017
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.changetracker.repository;

import org.jtool.changetracker.core.CTConsole;
import org.jtool.changetracker.operation.CopyOperation;
import org.jtool.changetracker.operation.DocumentOperation;
import org.jtool.changetracker.operation.IChangeOperation;
import java.util.List;

/**
 * Applies an change operation into code.
 * @author Katsuhisa Maruyama
 */
public class CodeRestorer {
    
    /**
     * Applies change operations within the time range.
     * @param finfo information about the file related to the change operations
     * @param code the code that the change operations will be applied to
     * @param from the index of the first change operation within the time range
     * @param to the index at the last change operation within the time range
     * @return the resulting code after the application, or <code>null</code> if the application failed
     */
    public static String applyOperations(CTFile file, String code, int from, int to) {
        return applyOperations(file.getOperationHistory(), code, from, to);
    }
    /**
     * Applies change operations within the time range.
     * @param history the history that contains the change operations
     * @param code the code that the change operations will be applied to
     * @param from the index of the first change operation within the time range
     * @param to the index at the last change operation within the time range
     * @return the resulting code after the application, or <code>null</code> if the application failed
     */
    public static String applyOperations(OperationHistory history, String code, int from, int to) {
        if (from == to) {
            return code;
        }
        
        List<IChangeOperation> ops = history.getOperations();
        if (from < to) {
            assert from >= 0;
            assert to < ops.size();
            for (int idx = from + 1; idx <= to && code != null; idx++) {
                IChangeOperation op = ops.get(idx);
                
                if (op.isDocument()) {
                    if (idx > history.getIndexOfAlreadyChecked()) {
                        boolean result = consistentForward(code, (DocumentOperation)op);
                        if (!result) {
                            return null;
                        }
                    }
                    code = applyOperationForward(code, (DocumentOperation)op);
                    
                } else if (op.isCopy()) {
                    if (idx > history.getIndexOfAlreadyChecked()) {
                        boolean result = consistentCopy(code, (CopyOperation)op);
                        if (!result) {
                            return null;
                        }
                    }
                }
            }
        } else {
            assert to >= 0;
            assert from < ops.size();
            for (int idx = from; idx > to && code != null; idx--) {
                IChangeOperation op = ops.get(idx);
                if (op.isDocument()) {
                    if (idx > history.getIndexOfAlreadyChecked()) {
                        boolean result = consistentBackward(code, (DocumentOperation)op);
                        if (!result) {
                            return null;
                        }
                    }
                    code = applyOperationBackward(code, (DocumentOperation)op);
                    
                } else if (op.isCopy()) {
                    if (idx > history.getIndexOfAlreadyChecked()) {
                        boolean result = consistentCopy(code, (CopyOperation)op);
                        if (!result) {
                            return null;
                        }
                    }
                }
            }
        }
        return code;
    }
    
    /**
     * Tests if a successive change operation was consistently applied to the source code.
     * @param code the source code before the application
     * @param op the operation to be applied
     * @return <code>true</code> if the application is consistent, otherwise <code>false</code>
     */
    public static boolean consistentForward(String code, DocumentOperation op) {
        if (code == null) {
            return false;
        }
        
        String dtext = op.getDeletedText();
        int start = op.getStart();
        int end = start + dtext.length();
        
        if (start > code.length() || end > code.length()) {
            CTConsole.println("Cannot insert/delete text: " + op.toString());
            return false;
        }
        
        if (dtext.length() > 0) {
            String text = code.substring(start, end);
            if (!text.equals(dtext)) {
                CTConsole.println("Cannot find deleted text: " + op.toString());
                return false;
            }
        }
        return true;
    }
    
    /**
     * Applies forward a change operation to a given code.
     * @param code the code which the code change operation will be applied to
     * @param op the change operation to be applied
     * @return the resulting code after the application
     */
    public static String applyOperationForward(String code, DocumentOperation op) {
        int start = op.getStart();
        int end = start + op.getDeletedText().length();
        StringBuilder postCode = new StringBuilder(code);
        postCode.replace(start, end, op.getInsertedText());
        return postCode.toString();
    }
    
    /**
     * Tests if a precedent change operation was consistently applied to the source code.
     * @param code the source code after the application
     * @param op the operation to be applied
     * @return <code>true</code> if the application is consistent, otherwise <code>false</code>
     */
    public static boolean consistentBackward(String code, DocumentOperation op) {
        if (code == null) {
            return false;
        }
        
        String itext = op.getInsertedText();
        int start = op.getStart();
        int end = start + itext.length();
        
        if (start > code.length() || end > code.length()) {
            CTConsole.println("Cannot insert/delete text: " + op.toString());
            return false;
        }
        
        if (itext.length() > 0) {
            String text = code.substring(start, end);
            if (!text.equals(itext)) {
                CTConsole.println("Cannot find inserted text: " + op.toString());
                return false;
            }
        }
        return true;
    }
    
    /**
     * Applies backward a change operation to a given code.
     * @param code the code which the change operation will be applied to
     * @param op the change operation to be applied
     * @return the resulting code after the application
     */
    public static String applyOperationBackward(String code, DocumentOperation op) {
        int start = op.getStart();
        int end = start + op.getInsertedText().length();
        StringBuilder postCode = new StringBuilder(code);
        postCode.replace(start, end, op.getDeletedText());
        return postCode.toString();
    }
    
    /**
     * Tests if a copy operation was consistently applied to the source code.
     * @param code the source code to be checked
     * @param op the copy operation
     * @return <code>true</code> if the application is consistent, otherwise <code>false</code>
     */
    public static boolean consistentCopy(String code, CopyOperation op) {
        if (code == null) {
            return false;
        }
        
        String ctext = op.getCopiedText();
        int start = op.getStart();
        int end = start + ctext.length();
        
        if (start > code.length() || end > code.length()) {
            CTConsole.println("Cannot copy text: " + op.toString());
            return false;
        }
        
        if (ctext.length() > 0) {
            String text = code.substring(start, end);
            if (!text.equals(ctext)) {
                CTConsole.println("Cannot find copied text: " + op.toString());
                return false;
            }
        }
        return true;
    }
}
