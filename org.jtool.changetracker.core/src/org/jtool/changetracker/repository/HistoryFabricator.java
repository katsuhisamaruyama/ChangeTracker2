/*
 *  Copyright 2016
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
 * Fabricates code chamge operations stored in the history.
 * @author Katsuhisa Maruyama
 */
class HistoryFabricator {
    
    /**
     * Fabricates code change operations.
     * @param operations the code change operations to be fabricated
     * @return the collection of the fabricated code change operations
     */
    static List<IChangeOperation> fabricate(List<IChangeOperation> operations) {
        assert operations != null;
        
        List<IChangeOperation> retOps = new ArrayList<IChangeOperation>();
        for (IChangeOperation op : operations) {
            retOps.add(op);
        }
        
        retOps = remove(retOps);
        retOps = merge(retOps);
        retOps = eliminateSuccessiveOpenClose(retOps);
        
        return retOps;
    }
    
    /**
     * Removes code change operations that have the same inserted and deleted text.
     * @param operations
     * @return
     */
    private static List<IChangeOperation> remove(List<IChangeOperation> operations) {
        for (int idx = 0; idx < operations.size(); idx++) {
            IChangeOperation operation = operations.get(idx);
            if (operation.isDocument()) {
                DocumentOperation doperation = (DocumentOperation)operation;
                if (doperation.getInsertedText().equals(doperation.getDeletedText())) {
                    operations.remove(idx);
                    idx--;
                }
            }
        }
        return operations;
    }
    
    /**
     * Merges code change operations that contain a temporal text that was inserted and immediately deleted.
     * @param operations the code change operations to be merge 
     * @return the collection of the merged code change operations
     */
    private static List<IChangeOperation> merge(List<IChangeOperation> operations) {
        if (operations == null) {
            return null;
        }
        
        List<IChangeOperation> retOps = new ArrayList<IChangeOperation>();
        for (IChangeOperation op : operations) {
            retOps.add(op);
        }
        
        retOps = mergeOperationsWithTemporalText(retOps);
        return retOps;
    }
    
    /**
     * Merges operations that contain the temporal text that was inserted and immediately deleted.
     * @param ops the whole operations
     * @return the collection of the operations after the merge
     */
    private static List<IChangeOperation> mergeOperationsWithTemporalText(List<IChangeOperation> operations) {
        for (int idx = 0; idx < operations.size() - 1; idx++) {
            IChangeOperation operation1 = operations.get(idx);
            IChangeOperation operation2 = operations.get(idx + 1);
            
            if (!operation1.isDocument() || !operation2.isDocument()) {
                continue;
            }
            if (!operation1.getPath().equals(operation2.getPath()) || !operation1.getAuthor().equals(operation2.getAuthor())) {
                continue;
            }
            
            DocumentOperation doperation1 = (DocumentOperation)operation1;
            DocumentOperation doperation2 = (DocumentOperation)operation2;
            if (!doperation1.isEdit() || !doperation2.isEdit()) {
                continue;
            }
            
            String itext1 = doperation1.getInsertedText();
            String itext2 = doperation2.getInsertedText();
            String dtext1 = doperation1.getDeletedText();
            String dtext2 = doperation2.getDeletedText();
            if (itext1.length() == 0 || dtext2.length() == 0) {
                continue;
            }
            
            if (doperation1.getStart() == doperation2.getStart() && itext1.equals(dtext2)) {
                if (containsMultiByteCode(itext1)) {
                    operations.remove(idx + 1);
                    if (dtext1.length() == 0 && itext2.length() == 0) {
                        operations.remove(idx);
                    } else {
                        doperation1.setInsertedText(itext2);
                    }
                    idx--;
                }
            }
        }
        return operations;
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
     * Eliminates the repeated file save operation.
     * @param operations the code change operations to be eliminated
     * @return the collection of the code change operations after the elimination.
     */
    private static List<IChangeOperation> eliminateSuccessiveOpenClose(List<IChangeOperation> operations) {
        for (int idx = 0; idx < operations.size() - 1; idx++) {
            IChangeOperation operation1 = operations.get(idx);
            
            if (operation1.isFile()) {
                IChangeOperation operation2 = operations.get(idx + 1);
                if (operation2.isFile()) {
                    FileOperation foperation2 = (FileOperation)operation2;
                    if (operation1.getPath().equals(operation2.getPath()) && foperation2.isClose()) {
                        operations.remove(idx);
                        operations.remove(idx);
                        idx--;
                    }
                }
            }
        }
        return operations;
    }
}
