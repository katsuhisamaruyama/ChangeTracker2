/*
 *  Copyright 2017
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.changetracker.dependencyanalyzer;

import org.jtool.changetracker.operation.IChangeOperation;
import org.jtool.changetracker.operation.CodeOperation;
import org.jtool.changetracker.repository.CTFile;
import org.eclipse.jdt.core.dom.CompilationUnit;
import java.util.List;
import java.util.ArrayList;

/**
 * Detects dependency edges of an operation history graph.
 * @author Katsuhisa Maruyama
 */
public class DependencyDetector {
    
    /**
     * Generates a parse-able snapshot. 
     * @param finfo information about a file that has the snapshot
     * @param index the index number of the change operation that generates the snapshot
     * @return the generated parse-able snapshot, or <code>null</code> the generation fails
     */
    public static ParseableSnapshot parse(CTFile finfo, int index) {
        ParseableSnapshot psn = finfo.getLastSnapshot();
        String code;
        if (psn != null) {
            code = finfo.getCode(psn.getCode(), psn.getIndex(), index);
        } else {
            code = finfo.getCode(index);
        }
        return parse(finfo, index, code);
    }
    
    /**
     * Generates a parse-able snapshot. 
     * @param finfo information about a file that has the snapshot
     * @param index the index number of the change operation that generates the snapshot
     * @param code the contents of the snapshot
     * @return the generated parse-able snapshot, or <code>null</code> the generation fails
     */
    public static ParseableSnapshot parse(CTFile finfo, int index, String code) {
        CTParser parser = new CTParser();
        CompilationUnit cu = parser.parse(code);
        if (cu != null) {
            List<JavaConstruct> cons = parser.getJavaConstructs(cu);
            ParseableSnapshot sn = new ParseableSnapshot(finfo, index, code, cons);
            for (JavaConstruct c : cons) {
                c.setSnapshot(sn);
            }
            return sn;
        }
        return null;
    }
    
    /**
     * Detects backward edges between a Java construct and a code change operation.
     * @param sn the previous snapshot of the file containing the Java construct
     * @param ops change operations that might have backward edges
     */
    public static void detectBackwardChangeEdges(ParseableSnapshot sn, List<CodeOperation> ops) {
        if (sn == null) {
            return;
        }
        
        List<CodeOperation> aops = new ArrayList<CodeOperation>(ops);
        for (int idx = 0; idx < ops.size(); idx++) {
            CodeOperation op = ops.get(idx);
            aops.remove(0);
            
            List<JavaConstruct> cons = new ArrayList<JavaConstruct>();
            for (JavaConstruct con : sn.getJavaConstructs()) {
                int len = getLengthOfDeletedOrCopiedText(op);
                for (int offset = op.getStart(); offset < op.getStart() + len; offset++) {
                    int aoffset = adjustBackwardOffset(offset, aops);
                    if (con.inRangeForDeletion(aoffset) && !cons.contains(con)) {
                        cons.add(con);
                    }
                }
                if (op.getInsertedText().length() != 0) {
                    int aoffset = adjustBackwardOffset(op.getStart(), aops);
                    if (con.inRangeForInsertion(aoffset) && !cons.contains(con)) {
                        cons.add(con);
                    }
                }
            }
            op.setBackwardJavaConstructs(cons);
        }
    }
    
    /**
     * Detects forward edges between a Java construct and a code change operation.
     * @param sn the previous snapshot of the file containing the Java construct
     * @param ops change operations that might have forward edges
     */
    public static void detectForwardChangeEdges(ParseableSnapshot sn, List<CodeOperation> ops) {
        if (sn == null) {
            return;
        }
        
        List<CodeOperation> aops = new ArrayList<CodeOperation>(ops);
        for (int idx = 0; idx < ops.size(); idx++) {
            CodeOperation op = ops.get(idx);
            aops.remove(0);
            
            List<JavaConstruct> cons = new ArrayList<JavaConstruct>();
            for (JavaConstruct con : sn.getJavaConstructs()) {
                int len = op.getInsertedText().length();
                for (int offset = op.getStart(); offset < op.getStart() + len; offset++) {
                    int aoffset = adjustForwardOffset(offset, aops);
                    if (con.inRangeForInsertion(aoffset) && !cons.contains(con)) {
                        cons.add(con);
                    }
                }
                if (op.getDeletedText().length() != 0) {
                    int aoffset = adjustBackwardOffset(op.getStart(), aops);
                    if (con.inRangeForDeletion(aoffset) && !cons.contains(con)) {
                        cons.add(con);
                    }
                }
            }
            op.setForwardJavaConstructs(cons);
        }
    }
    
    /**
     * Obtains the length of deleted or copied text of a change operation.
     * @param op the change operation
     * @return the length of the text
     */
    private static int getLengthOfDeletedOrCopiedText(CodeOperation op) {
        if (op.isCopy()) {
            return op.getCopiedText().length();
        } else {
            return op.getDeletedText().length();
        }
    }
    
    /**
     * Adjusts the offset value of based on the related operations.
     * @param base the original value of the offset of the character to be adjusted
     * @param ops the operations related to the adjustment
     * @return the offset value after the adjustment
     */
    public static int adjustBackwardOffset(int base, List<CodeOperation> ops) {
        int offset = base;
        for (int idx = ops.size() - 1; idx >= 0; idx--) {
            CodeOperation op = ops.get(idx);
            int start = op.getStart();
            if (start <= offset) {
                offset = offset - op.getInsertedText().length() + op.getDeletedText().length();
                if (offset < start) {
                    offset = start;
                }
            }
        }
        return offset;
    }
    
    /**
     * Adjusts the offset value based on its related operations.
     * @param base the original value of the offset of the character to be adjusted
     * @param ops the operations related to the adjustment
     * @return the offset value after the adjustment
     */
    public static int adjustForwardOffset(int base, List<CodeOperation> ops) {
        int offset = base;
        for (int idx = 0; idx < ops.size(); idx++) {
            CodeOperation op = ops.get(idx);
            int start = op.getStart();
            if (start <= offset) {
                offset = offset + op.getInsertedText().length() - op.getDeletedText().length();
                if (offset < start) {
                    offset = start;
                }
            }
        }
        return offset;
    }
    
    /**
     * Obtains the collection of code change operations from change operations.
     * @param ops the collection of change operations
     * @return the collection of code change operations
     */
    public static List<CodeOperation> getCodeOperations(List<IChangeOperation> ops) {
        List<CodeOperation> retOps = new ArrayList<CodeOperation>();
        for (int idx = 0; idx < ops.size(); idx++) {
            IChangeOperation op = ops.get(idx);
            if (op.isDocumentOrCopy()) {
                retOps.add((CodeOperation)op);
            }
        }
        return retOps;
    }
}
