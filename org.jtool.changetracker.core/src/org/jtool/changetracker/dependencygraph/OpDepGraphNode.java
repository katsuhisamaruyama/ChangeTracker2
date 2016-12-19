/*
 *  Copyright 2016
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.changetracker.dependencygraph;

import org.jtool.changetracker.operation.ICodeOperation;
import java.time.ZonedDateTime;
import java.util.Set;
import java.util.HashSet;
import java.util.List;
import java.util.ArrayList;

/**
 * Stores information about a node of the operation dependency graph.
 * @author Katsuhisa Maruyama
 */
public abstract class OpDepGraphNode {
    
    /**
     * The identification number of this node for each file.
     */
    protected int id = -1;
    
    /**
     * The code change operation corresponding to this node.
     */
    protected ICodeOperation operation;
    
    /**
     * The collection of edges incoming to this node.
     */
    private HashSet<OpDepGraphEdge> incomingEdges;
    
    /**
     * The collection of edges outgoing from this node.
     */
    private HashSet<OpDepGraphEdge> outgoingEdges;
    
    /**
     * The collection of the offsets after adjustment.
     */
    protected List<Integer> adjustedOffsetValues;
    
    /**
     * Creates an empty instance.
     */
    protected OpDepGraphNode() {
    }
    
    /**
     * Creates an operation node for a text operation.
     * @param id the identification number of this node
     * @param operation the text operation
     */
    public OpDepGraphNode(int id, ICodeOperation operation) {
        this.id = id;
        this.operation = operation;
        
        this.incomingEdges = new HashSet<OpDepGraphEdge>();
        this.outgoingEdges = new HashSet<OpDepGraphEdge>();
        this.adjustedOffsetValues = new ArrayList<Integer>();
    }
    
    /**
     * Resets the adjustment for offsets.
     */
    public void reset() {
        adjustedOffsetValues.clear();
        
        if (getLength() > 0) {
            for (int o = getOffset(); o < getOffset() + getLength(); o++) {
                adjustedOffsetValues.add(new Integer(o));
            }
        }
    }
    
    /**
     * Returns the identification number of this node for each file.
     * @return the identification number of this node
     */
    public int getId() {
        return id;
    }
    
    /**
     * Returns the code change operation corresponding to this operation node.
     * @return the corresponding code change operation
     */
    public ICodeOperation getOperation() {
        return operation;
    }
    
    /**
     * Returns the time when the code change operation corresponding to this node was performed.
     * @return the time of the code change operation
     */
    public ZonedDateTime getTime() {
        return operation.getTime();
    }
    
    /**
     * Returns the leftmost offset value of the text affected by the code change operation corresponding to this node.
     * @return the offset value of the affected text
     */
    public int getOffset() {
        return operation.getStart();
    }
    
    /**
     * Returns the length of the text affected by the code change operation corresponding to this node.
     * @return always <code>0</code> by default
     */
    public int getLength() {
        return 0;
    }
    
    /**
     * Returns the length of the text, which is used for adjustment.
     * @return the positive value for the text addition,
     *         the negative value for the text removal operation, or
     *         <code>0</code> for the text copy operation
     */
    public int getAdjustedLength() {
        if (isAddNode()) {
            return -1 * getLength();
            
        } else if (isRemoveNode()) {
            return getLength();
            
        }
        return 0;
    }
    
    /**
     * Adds an edge incoming to this node.
     * @param edge the incoming edge to be added
     */
    void addIncomingEdge(OpDepGraphEdge edge) {
        incomingEdges.add(edge);
    }
    
    /**
     * Adds an edge outgoing to this node.
     * @param edge the outgoing edge to be added
     */
    void addOutgoingEdge(OpDepGraphEdge edge) {
        outgoingEdges.add(edge);
    }
    
    /**
     * Removes an edge incoming from this node.
     * @param edge the incoming edge to be removed
     */
    void removeIncomingEdge(OpDepGraphEdge edge) {
        incomingEdges.remove(edge);
    }
    
    /**
     * Removes an edge outgoing from this node.
     * @param edge the outgoing edge to be removed
     */
    void removeOutgoingEdge(OpDepGraphEdge edge) {
        outgoingEdges.remove(edge);
    }
    
    /**
     * Returns the edges incoming to this node.
     * @return the set of incoming edges
     */
    public Set<OpDepGraphEdge> getIncomingEdges() {
        return incomingEdges;
    }
    
    /**
     * Returns the edges outgoing from this node.
     * @return the set of incoming edges
     */
    public Set<OpDepGraphEdge> getOutgoingEdges() {
        return outgoingEdges;
    }
    
    /**
     * Returns the nodes of the source of this node.
     * @return the set of the source nodes
     */
    public Set<OpDepGraphNode> getSrcNodes() {
        Set<OpDepGraphNode> nodes = new HashSet<OpDepGraphNode>();
        for (OpDepGraphEdge edge : incomingEdges) {
            nodes.add(edge.getSrcNode());
        }
        return nodes;
    }
    
    /**
     * Returns the nodes of the destination of this node.
     * @return the set of the destination nodes
     */
    public Set<OpDepGraphNode> getDstNodes() {
        Set<OpDepGraphNode> nodes = new HashSet<OpDepGraphNode>();
        for (OpDepGraphEdge edge : outgoingEdges) {
            nodes.add(edge.getDstNode());
        }
        return nodes;
    }
    
    /**
     * Returns the offset values after adjustment.
     * @return the collection of the adjusted offsets
     */
    public List<Integer> getAdjustedOffsetValues() {
        return adjustedOffsetValues;
    }
    
    /**
     * Clears the offset values after adjustment.
     */
    public void clearAdjustedOffsetValues() {
        adjustedOffsetValues.clear();
    }
    
    
    /**
     * Tests if this node indicates addition.
     * @return always <code>false</code>
     */
    public boolean isAddNode() {
        return false;
    }
    
    /**
     * Tests if node indicates removal.
     * @return always <code>false</code>
     */
    public boolean isRemoveNode() {
        return false;
    }
    
    /**
     * Tests if node indicates copy.
     * @return always <code>false</code>
     */
    public boolean isCopyNode() {
        return false;
    }
    
    /**
     * Tests if this node depends on a given node.
     * @param node the node on which this node might depend on
     * @return <code>true</code> if this node depends on a given node, otherwise <code>false</code>
     */
    public abstract boolean dependsOn(OpDepGraphNode node);
    
    /**
     * Adjusts offset values of this operation without its non-interference to the next operation.
     * @param node the node corresponding to the next operation
     */
    public void adjustOffsetValuesForward(OpDepGraphNode node) {
        if (adjustedOffsetValues.size() == 0) {
            return;
        }
        
        int offset_j = node.getOffset();
        List<Integer> adjustedOffsets2 = new ArrayList<Integer>(adjustedOffsetValues);
        for (Integer offset : adjustedOffsets2) {
            int offset_i = offset.intValue();
            if (isAddNode()) {
                if (offset_i > offset_j) {
                    int adj = adjustedOffsets2.indexOf(offset);
                    adjustedOffsetValues.remove(adj);
                    int aoffset_i = offset_i - node.getAdjustedLength();
                    adjustedOffsetValues.add(adj, new Integer(aoffset_i));
                }
                
            } else if (isRemoveNode() || isCopyNode()) {
                if (offset_i >= offset_j) {
                    int adj = adjustedOffsets2.indexOf(offset);
                    adjustedOffsetValues.remove(adj);
                    int aoffset_i = offset_i - node.getAdjustedLength();
                    adjustedOffsetValues.add(adj, new Integer(aoffset_i));
                }
            }
        }
    }
    
    /**
     * Adjusts offsets of this operation without its non-interference to the previous operation.
     * @param node the node corresponding to the previous operation
     */
    public void adjustOffsetValuesBackward(OpDepGraphNode node) {
        if (adjustedOffsetValues.size() == 0) {
            return;
        }
        
        int offset_i = node.getOffset();
        List<Integer> adjustedOffsets2 = new ArrayList<Integer>(adjustedOffsetValues);
        for (Integer offset : adjustedOffsets2) {
            int offset_j = offset.intValue();
            
            if (isAddNode()) {
                if (offset_i < offset_j) {
                    int adj = adjustedOffsets2.indexOf(offset);
                    adjustedOffsetValues.remove(adj);
                    int aoffset_j = offset_j + node.getAdjustedLength();
                    adjustedOffsetValues.add(adj, new Integer(aoffset_j));
                }
                
            } else if (isRemoveNode() || isCopyNode()) {
                if (offset_i <= offset_j) {
                    int adj = adjustedOffsets2.indexOf(offset);
                    adjustedOffsetValues.remove(adj);
                    int aoffset_j = offset_j + node.getAdjustedLength();
                    adjustedOffsetValues.add(adj, new Integer(aoffset_j));
                }
            }
        }
    }
    
    /**
     * Tests if a given graph node is the same as this.
     * @param node the node of the operation dependency graph
     * @return <code>true</code> if the two nodes are the same, otherwise <code>false</code>
     */
    public boolean equals(OpDepGraphNode node) {
        if (node == null) {
            return false;
        }
        
        return this.id == node.getId() && this.operation.getPath().equals(node.operation.getPath());
    }
    
    /**
     * Returns a hash code value for this object.
     * @return always <code>0</code> that means all objects have the same hash code
     */
    @Override
    public int hashCode() {
        return 0;
    }
    
    /**
     * Returns the string for printing. 
     * @return the string for printing
     */
    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder();
        
        buf.append(String.valueOf(getId()) + " ");
        buf.append(operation.getTime() + " ");
        buf.append("path:[" + operation.getPath() + "] ");
        
        if (isAddNode()) {
            int start = operation.getStart();
            int end = start + operation.getInsertedText().length() - 1;
            buf.append("a[" + start + "," + end + "]");
            
        } else if (isRemoveNode()) {
            int start = operation.getStart();
            int end = start + operation.getDeletedText().length() - 1;
            buf.append("r[" + start + "," + end + "]");
            
        } else if (isCopyNode()) {
            int start = operation.getStart();
            int end = start + operation.getCopiedText().length() - 1;
            buf.append("c[" + start + "," + end + "]");
        }
        
        return buf.toString();
    }
    
    /**
     * Returns the string for printing. 
     * @return the string for printing
     */
    public String toSimpleString() {
        StringBuilder buf = new StringBuilder();
        
        buf.append(String.valueOf(getId()) + " ");
        buf.append(operation.getTime() + " ");
        buf.append("path:[" + operation.getPath() + "] ");
        
        return buf.toString();
    }
    
    /**
     * Returns the string for presenting offset values.
     * @return the string of the offset values
     */
    public String getOffsetValues() {
        StringBuilder buf = new StringBuilder();
        buf.append("{");
        for (Integer offset : adjustedOffsetValues) {
            buf.append(" ");
            buf.append(offset.intValue());
        }
        buf.append(" }");
        return buf.toString();
    }
}
