/*
 *  Copyright 2016
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.changetracker.dependencygraph;

import org.jtool.changetracker.operation.ICodeOperation;
import java.util.List;
import java.util.ArrayList;

/**
 * Manages information about a node corresponding to a text copy operation.
 * @author Katsuhisa Maruyama
 */
public class OpDepGraphCopyNode extends OpDepGraphNode {
    
    /**
     * Creates a copy operation node for a text copy operation..
     * @param id the identification number of this node
     * @param operation the text addition operation
     */
    public OpDepGraphCopyNode(int id, ICodeOperation operation) {
        super(id, operation);
        
        reset();
    }
    
    /**
     * Tests if node indicates the text copy.
     * @return always <code>true</code>
     */
    public boolean isCopyNode() {
        return true;
    }
    
    /**
     * Returns the length of the text affected by the text copy operation corresponding to this node.
     * @return the length of the removed text
     */
    @Override
    public int getLength() {
        return operation.getCopiedText().length();
    }
    
    /**
     * Tests if this node depends on a given node.
     * @param node the node on which this node might depend on
     * @return <code>true</code> if this node depends on a given node, otherwise <code>false</code>
     */
    @Override
    public boolean dependsOn(OpDepGraphNode node) {
        if (node.isAddNode()) {
            int offset_i = node.getOffset();
            int length_i = node.getLength();
            
            List<Integer> adjustedOffsetValues2 = new ArrayList<Integer>(adjustedOffsetValues);
            for (Integer offset : adjustedOffsetValues2) {
                int offset_j = offset.intValue();
                if (offset_i <= offset_j && offset_j < offset_i + length_i) {
                    adjustedOffsetValues.remove(offset);
                }
            }
            
            return adjustedOffsetValues.size() < adjustedOffsetValues2.size();
            
        } else if (node.isRemoveNode()) {
            int offset_i = node.getOffset();
            int offset_j = adjustedOffsetValues.get(0).intValue();
            int length_j = getLength();
            
            return offset_j < offset_i && offset_i < offset_j + length_j;
        }
        
        return false;
    }
}
