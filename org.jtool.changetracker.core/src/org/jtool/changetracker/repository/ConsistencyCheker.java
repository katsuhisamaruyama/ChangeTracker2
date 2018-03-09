/*
 *  Copyright 2018
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.changetracker.repository;

import org.jtool.changetracker.operation.IChangeOperation;
import org.jtool.changetracker.operation.CopyOperation;
import org.jtool.changetracker.operation.DocumentOperation;
import org.jtool.changetracker.operation.FileOperation;
import org.jtool.changetracker.convert.DiffGenerator;
import org.jtool.changetracker.core.CTConsole;
import java.util.List;

/**
 * Checks change operations.
 * @author Katsuhisa Maruyama
 */
public class ConsistencyCheker {
    
    /**
     * Checks if change operations are consistent with restored code.
     * @param history the history that stores the change operations
     * @return <code>true</code> if all the change operations are consistent with the restored code, otherwise <code>false</code>
     */
    public static boolean run(OperationHistory history) {
        List<IChangeOperation> ops = history.getOperations();
        
        for (int idx = 0; idx < ops.size(); idx++) {
            IChangeOperation op = ops.get(idx);
            if (op.isFile()) {
                int fidx = history.getRestorationIndexBefore(idx - 1);
                if (fidx != -1) {
                    FileOperation fop = (FileOperation)ops.get(fidx);
                    String fromCode = fop.getCode();
                    String toCode = ((FileOperation)op).getCode();
                    String predCode = applyOperationsWithConsistencyCheck(history, fromCode, fidx, idx - 1);
                    String nextCode = applyOperationsWithConsistencyCheck(history, toCode, idx, fidx);
                    
                    if (toCode == null || predCode == null || !toCode.equals(predCode)) {
                        CTConsole.println("Inconsistent with change operations: " +
                                fop.getFormatedTime() + " (" + fop.getTimeAsLong() + ") - " +
                                op.getFormatedTime() + " (" + op.getTimeAsLong() + ") by forward restoration");
                        
                        IChangeOperation predOp = ops.get(idx - 1);
                        CTPath pathinfo = new CTPath(predOp);
                        DiffGenerator.generate(predOp.getTime(), pathinfo, predCode, toCode);
                        System.err.println("PRECODE = " + predCode);
                        System.err.println("TOCODE  = " + toCode);
                        
                        /*
                        List<DiffOperation> diffops = DiffOperationGenerator.generate(predOp.getTime(), pathinfo, predCode, toCode);
                        for (int didx = 0; didx < diffops.size(); didx++) {
                            DiffOperation dop = diffops.get(didx);
                            System.err.println("  " + dop.toString());
                        }*/
                        
                        return false;
                    }
                    if (!fromCode.equals(nextCode)) {
                        CTConsole.println("Inconsistent with change operations: " +
                                fop.getFormatedTime() + " (" + fop.getTimeAsLong() + ") - " +
                                op.getFormatedTime() + " (" + op.getTimeAsLong() + ") by backward restoration");
                        return false;
                    }
                }
                
            } else if (op.isDocument() || op.isCopy()) {
                String code = getCodeWithConsistencyCheck(history, idx);
                if (code == null) {
                    CTConsole.println("Failure of restration: " + op.getTimeAsLong() + " " + op.toString());
                    return false;
                }
            }
        }
        return true;
    }
    
    /**
     * Obtains the contents of source code restored at the time when a specified change operation was performed and checks the code.
     * @param index the index of the code change operation at the restoration point
     * @return the contents of the restored source code, <code>null</code> if the restoration fails
     */
    private static String getCodeWithConsistencyCheck(OperationHistory history, int index) {
        int findex = history.getRestorationIndex(index);
        if (findex == -1) {
            return null;
        }
        
        FileOperation fop  = (FileOperation)history.getOperations().get(findex);
        return applyOperationsWithConsistencyCheck(history, fop.getCode(), findex, index);
    }
    
    /**
     * Applies change operations within the time range with their consistency check.
     * @param history the history that contains the change operations
     * @param code the code that the change operations will be applied to
     * @param from the index of the first change operation within the time range
     * @param to the index at the last change operation within the time range
     * @return the resulting code after the application, or <code>null</code> if any consistency was found
     */
    public static String applyOperationsWithConsistencyCheck(OperationHistory history, String code, int from, int to) {
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
                    boolean result = consistentForward(code, (DocumentOperation)op);
                    if (!result) {
                         return null;
                    }
                    code = CodeRestorer.applyOperationForward(code, (DocumentOperation)op);
                    
                } else if (op.isCopy()) {
                    boolean result = consistentCopy(code, (CopyOperation)op);
                    if (!result) {
                        return null;
                    }
                }
            }
        } else {
            for (int idx = from; idx > to && code != null; idx--) {
                IChangeOperation op = ops.get(idx);
                
                if (op.isDocument()) {
                    boolean result = consistentBackward(code, (DocumentOperation)op);
                    if (!result) {
                        return null;
                    }
                    code = CodeRestorer.applyOperationBackward(code, (DocumentOperation)op);
                    
                } else if (op.isCopy()) {
                    boolean result = consistentCopy(code, (CopyOperation)op);
                    if (!result) {
                        return null;
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
        
        int start = op.getStart();
        String itext = op.getInsertedText();
        String dtext = op.getDeletedText();
        
        if (itext.length() > 0) {
            if (start > code.length()) {
                CTConsole.println("F: Cannot insert text: " + op.getTimeAsLong() + " " + op.toString());
                return false;
            }
        }
        
        if (dtext.length() > 0) {
            int end = start + dtext.length();
            if (start > code.length() || end > code.length()) {
                CTConsole.println("F: Cannot delete text: " + op.getTimeAsLong() + " " + op.toString());
                return false;
            }
            
            String text = code.substring(start, end);
            if (!text.equals(dtext)) {
                CTConsole.println("F: Cannot find deleted text: " + op.getTimeAsLong() + " " + op.toString());
                System.err.println(code);
                return false;
            }
        }
        return true;
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
        
        int start = op.getStart();
        String itext = op.getInsertedText();
        String dtext = op.getDeletedText();
        if (dtext.length() > 0) {
            if (start > code.length()) {
                CTConsole.println("B: Cannot delete text: " + op.getTimeAsLong() + " " + op.toString());
                return false;
            }
        }
        
        if (itext.length() > 0) {
            int end = start + itext.length();
            if (start > code.length() || end > code.length()) {
                CTConsole.println("B: Cannot insert text: " + op.getTimeAsLong() + " " + op.toString());
                return false;
            }
            
            String text = code.substring(start, end);
            if (!text.equals(itext)) {
                CTConsole.println("B: Cannot find inserted text: " + op.getTimeAsLong() + " " +op.toString());
                System.err.println(code);
                return false;
            }
        }
        return true;
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
            CTConsole.println("Cannot copy text: " + op.getTimeAsLong() + " " + op.toString());
            return false;
        }
        
        if (ctext.length() > 0) {
            String text = code.substring(start, end);
            if (!text.equals(ctext)) {
                CTConsole.println("Cannot find copied text: " + op.getTimeAsLong() + " " + op.toString());
                System.err.println(code);
                return false;
            }
        }
        return true;
    }
}
