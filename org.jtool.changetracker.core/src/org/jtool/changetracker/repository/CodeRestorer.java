/*
 *  Copyright 2018
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.changetracker.repository;

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
     * @return the resulting code after the application
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
     * @return the resulting code after the application
     */
    public static String applyOperations(OperationHistory history, String code, int from, int to) {
        if (from == to) {
            return code;
        }
        
        List<IChangeOperation> ops = history.getOperations();
        if (from < to) {
            for (int idx = from + 1; idx <= to && code != null; idx++) {
                IChangeOperation op = ops.get(idx);
                if (op.isDocument()) {
                    code = applyOperationForward(code, (DocumentOperation)op);
                }
            }
        } else {
            for (int idx = from; idx > to && code != null; idx--) {
                IChangeOperation op = ops.get(idx);
                if (op.isDocument()) {
                    code = applyOperationBackward(code, (DocumentOperation)op);
                }
            }
        }
        return code;
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
}
