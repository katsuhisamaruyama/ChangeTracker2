/*
 *  Copyright 2016
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.changetracker.repository;

import org.jtool.changetracker.operation.DocumentOperation;
import org.jtool.changetracker.operation.IChangeOperation;
import java.util.List;

/**
 * Applies an operation into code.
 * @author Katsuhisa Maruyama
 */
class CodeRestorer {
    
    /**
     * Applies operations within the time range defined by the specified two indexes.
     * @param operations the code change operations
     * @param code the code which the code change operation will be applied to
     * @param from the index at the start point of the time range
     * @param to the index at the end point of the time range
     * @return the resulting code after the application
     */
    static String applyOperations(List<IChangeOperation> operations, String code, int from, int to) {
        if (from == to) {
            return code;
        }
        
        if (from < to) {
            assert from >= 0;
            assert to < operations.size();
            for (int idx = from + 1; idx <= to && code != null; idx++) {
                IChangeOperation op = operations.get(idx);
                code = applyOperationForward(operations, code, op);
            }
            
        } else {
            assert to >= 0;
            assert from < operations.size();
            for (int idx = from; idx > to && code != null; idx--) {
                IChangeOperation op = operations.get(idx);
                code = applyOperationBackward(operations, code, op);
            }
        }
        
        return code;
    }
    
    /**
     * Applies forward a code change operation to a given code.
     * @param operations the code change operations
     * @param code the code which the code change operation will be applied to
     * @param operation the code change operation to be applied
     * @return the resulting code after the application
     */
    static String applyOperationForward(List<IChangeOperation> operations, String code, IChangeOperation operation) {
        if (operation.isDocument()) {
            return applyOperationForward(operations, code, (DocumentOperation)operation);
        }
        return code;
    }
    
    /**
     * Applies forward a document operation to a given code.
     * @param operations the code change operations
     * @param code the code which the document operation will be applied to
     * @param operation the document operation to be applied
     * @return the resulting code after the application
     */
    private static String applyOperationForward(List<IChangeOperation> operations, String code, DocumentOperation operation) {
        StringBuilder postCode = new StringBuilder(code);
        
        if (hasDeletionMismatch(postCode, operation)) {
            return null;
        }
        
        int start = operation.getStart();
        int end = start + operation.getDeletedText().length();
        String itext = operation.getInsertedText();
        postCode.replace(start, end, itext);
        return postCode.toString();
    }
    
    /**
     * Tests if the deletion occurs any mismatch of code.
     * @param code the code before the deletion
     * @param operation the operation to be applied
     * @return <code>true</code> if a mismatch exists, otherwise <code>false</code>
     */
    private static boolean hasDeletionMismatch(StringBuilder code, DocumentOperation operation) {
        String dtext = operation.getDeletedText();
        int start = operation.getStart();
        int end = start + dtext.length();
        
        if (dtext.length() > 0) {
            String rtext = code.substring(start, end);
            if (rtext != null && rtext.compareTo(dtext) != 0) {
                
                for (int i = 0; i < rtext.length(); i++) {
                    if (rtext.charAt(i) == dtext.charAt(i)) {
                        System.err.println(((int)rtext.charAt(i)) + " == " + ((int)dtext.charAt(i)));
                    } else {
                        System.err.println(((int)rtext.charAt(i)) + " != " + ((int)dtext.charAt(i)));
                    }
                }
                return true;
            }
        }
        return false;
    }
    
    /**
     * Applies backward a code change operation to a given code.
     * @param operations the code change operations
     * @param code the code which the code change operation will be applied to
     * @param operation the code change operation to be applied
     * @return the resulting code after the application
     */
    static String applyOperationBackward(List<IChangeOperation> operations, String code, IChangeOperation operation) {
        if (operation.isDocument()) {
            return applyOperationBackward(operations, code, (DocumentOperation)operation);
        }
        return code;
    }
    
    /**
     * Applies backward a document operation to a given code.
     * @param operations the code change operations
     * @param code the code which the document operation will be applied to
     * @param operation the document operation to be applied
     * @return the resulting code after the application
     */
    private static String applyOperationBackward(List<IChangeOperation> operations, String code, DocumentOperation operation) {
        StringBuilder postCode = new StringBuilder(code);
        
        if (hasInsertionMismatch(postCode, operation)) {
            return null;
        }
        
        int start = operation.getStart();
        int end = start + operation.getDeletedText().length();
        String dtext = operation.getDeletedText();
        postCode.replace(start, end, dtext);
        return postCode.toString();
    }
    
    /**
     * Tests if the insertion occurs any mismatch of code.
     * @param code the code before the insertion
     * @param operation the operation to be applied
     * @return <code>true</code> if a mismatch exists, otherwise <code>false</code>
     */
    private static boolean hasInsertionMismatch(StringBuilder code, DocumentOperation operation) {
        String itext = operation.getDeletedText();
        int start = operation.getStart();
        int end = start + itext.length();
        
        if (itext.length() > 0) {
            String rtext = code.substring(start, end);
            if (rtext != null && rtext.compareTo(itext) != 0) {
                
                for (int i = 0; i < rtext.length(); i++) {
                    if (rtext.charAt(i) == itext.charAt(i)) {
                        System.err.println(((int)rtext.charAt(i)) + " == " + ((int)itext.charAt(i)));
                    } else {
                        System.err.println(((int)rtext.charAt(i)) + " != " + ((int)itext.charAt(i)));
                    }
                }
                return true;
            }
        }
        return false;
    }
}
