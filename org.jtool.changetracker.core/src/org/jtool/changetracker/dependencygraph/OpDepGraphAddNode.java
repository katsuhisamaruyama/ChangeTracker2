/*
 *  Copyright 2016
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.changetracker.dependencygraph;

import org.jtool.changetracker.operation.ICodeOperation;

/**
 * Manages information about a node corresponding to a text addition operation.
 * @author Katsuhisa Maruyama
 */
public class OpDepGraphAddNode extends OpDepGraphNode {
    
    /**
     * Creates an addition operation node for a text addition operation.
     * @param id the identification number of this node
     * @param operation the text addition operation
     */
    public OpDepGraphAddNode(int id, ICodeOperation operation) {
        super(id, operation);
        
        reset();
    }
    
    /**
     * Tests if this node indicates the text addition.
     * @return always <code>true</code>
     */
    @Override
    public boolean isAddNode() {
        return true;
    }
    
    /**
     * Returns the length of the text affected by the text addition operation corresponding to this node.
     * @return the length of the added text
     */
    public int getLength() {
        return operation.getInsertedText().length();
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
            int offset_j = adjustedOffsetValues.get(0).intValue();
            
            if (offset_i < offset_j && offset_j < offset_i + length_i) {
                adjustedOffsetValues.clear();
                return true;
                
            } else {
                return false;
            }
        }
        
        return false;
    }
}
