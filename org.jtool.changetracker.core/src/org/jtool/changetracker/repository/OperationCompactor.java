/*
 *  Copyright 2017
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.changetracker.repository;

import org.jtool.changetracker.operation.IChangeOperation;
import org.jtool.changetracker.operation.DocumentOperation;
import org.jtool.changetracker.operation.FileOperation;
import java.util.List;
import java.util.ArrayList;

/**
 * Compacts change operations.
 * @author Katsuhisa Maruyama
 */
class OperationCompactor {
    
    /**
     * Compacts change operations.
     * @param ops the change operations
     * @return the collection of change operations after the compaction
     */
    static List<IChangeOperation> compact(List<IChangeOperation> ops) {
        List<IChangeOperation> retops = new ArrayList<IChangeOperation>(ops);
        retops = remove(retops);
        retops = merge(retops);
        retops = eliminateUnnecessaryActivate(retops);
        retops = eliminateUnnecessaryOpenClose(retops);
        retops = eliminateUnnecessarySave(retops);
        return retops;
    }
    
    /**
     * Removes change operations that have the same inserted and deleted text.
     * @param ops the change operations that the removal applies to
     * @return the collection of change operations after the removal
     */
    private static List<IChangeOperation> remove(List<IChangeOperation> ops) {
        for (int idx = 0; idx < ops.size(); idx++) {
            IChangeOperation op = ops.get(idx);
            if (op.isDocument()) {
                DocumentOperation dop = (DocumentOperation)op;
                if (dop.getInsertedText().equals(dop.getDeletedText())) {
                    ops.remove(idx);
                    idx--;
                }
            }
        }
        return ops;
    }
    
    /**
     * Merges change operations that contain the multi-byte text that was inserted and immediately deleted.
     * These change operations appear in the history due to Kana-Kanji conversion etc.
     * @param ops the change operations that the merge applies to
     * @return the collection of the operations after the merge
     */
    private static List<IChangeOperation> merge(List<IChangeOperation> ops) {
        for (int idx = 0; idx < ops.size() - 1; idx++) {
            IChangeOperation op1 = ops.get(idx);
            IChangeOperation op2 = ops.get(idx + 1);
            
            if (!op1.isDocument() || !op2.isDocument()) {
                continue;
            }
            if (!op1.getPath().equals(op2.getPath()) || !op1.getAuthor().equals(op2.getAuthor())) {
                continue;
            }
            
            DocumentOperation dop1 = (DocumentOperation)op1;
            DocumentOperation dop2 = (DocumentOperation)op2;
            if (!dop1.isTyping() || !dop2.isTyping()) {
                continue;
            }
            
            String itext1 = dop1.getInsertedText();
            String itext2 = dop2.getInsertedText();
            String dtext1 = dop1.getDeletedText();
            String dtext2 = dop2.getDeletedText();
            if (itext1.length() == 0 || dtext2.length() == 0) {
                continue;
            }
            
            if (dop1.getStart() == dop2.getStart() && itext1.equals(dtext2)) {
                if (containsMultiByteCode(itext1)) {
                    ops.remove(idx + 1);
                    if (dtext1.length() == 0 && itext2.length() == 0) {
                        ops.remove(idx);
                    } else {
                        dop1.setInsertedText(itext2);
                    }
                    idx--;
                }
            }
        }
        return ops;
    }
    
    /**
     * Tests if a given string contains the multibyte code.
     * @param str the string to be checked
     * @return <code>true</code> if the string contains the multibyte code, otherwise <code>false</code>
     */
    private static boolean containsMultiByteCode(String str) {
        try {
            byte[] bytes = str.getBytes("UTF8");
            return str.length() != bytes.length;
        } catch (Exception ex) {
        }
        return false;
    }
    
    /**
     * Eliminates unnecessary file open-close operations.
     * @param ops the change operations that the elimination applies to
     * @return the collection of the code change operations after the elimination
     */
    private static List<IChangeOperation> eliminateUnnecessaryOpenClose(List<IChangeOperation> ops) {
        for (int idx = 0; idx < ops.size() - 1; idx++) {
            IChangeOperation op1 = ops.get(idx);
            if (op1.isFile()) {
                IChangeOperation op2 = ops.get(idx + 1);
                if (op2.isFile()) {
                    FileOperation fop1 = (FileOperation)op1;
                    FileOperation fop2 = (FileOperation)op2;
                    if (op1.getPath().equals(op2.getPath()) && fop1.isOpen() && fop2.isClose()) {
                        ops.remove(idx);
                        ops.remove(idx);
                        idx--;
                    }
                }
            }
        }
        return ops;
    }
    
    /**
     * Eliminates unnecessary file save operations.
     * @param ops the change operations that the elimination applies to
     * @return the collection of the code change operations after the elimination
     */
    private static List<IChangeOperation> eliminateUnnecessarySave(List<IChangeOperation> ops) {
        for (int idx = 0; idx < ops.size() - 1; idx++) {
            IChangeOperation op1 = ops.get(idx);
            if (op1.isFile()) {
                IChangeOperation op2 = ops.get(idx + 1);
                if (op2.isFile()) {
                    FileOperation fop1 = (FileOperation)op1;
                    FileOperation fop2 = (FileOperation)op2;
                    if (op1.getPath().equals(op2.getPath()) && fop1.isSave() && fop2.isSave()) {
                        ops.remove(idx);
                        idx--;
                    }
                }
            }
        }
        return ops;
    }
    
    /**
     * Eliminates unnecessary file activation operations.
     * @param ops the change operations that the elimination applies to
     * @return the collection of the code change operations after the elimination
     */
    private static List<IChangeOperation> eliminateUnnecessaryActivate(List<IChangeOperation> ops) {
        for (int idx = 1; idx < ops.size(); idx++) {
            IChangeOperation op1 = ops.get(idx - 1);
            if (op1.isFile()) {
                IChangeOperation op2 = ops.get(idx);
                if (op2.isFile()) {
                    FileOperation fop1 = (FileOperation)op1;
                    FileOperation fop2 = (FileOperation)op2;
                    if (op1.getPath().equals(op2.getPath())) {
                        if (fop1.isActivate()) {
                            ops.remove(idx - 1);
                            idx--;
                        } else if (fop2.isActivate()) {
                            ops.remove(idx);
                            idx--;
                        }
                    }
                }
            }
        }
        return ops;
    }
}
