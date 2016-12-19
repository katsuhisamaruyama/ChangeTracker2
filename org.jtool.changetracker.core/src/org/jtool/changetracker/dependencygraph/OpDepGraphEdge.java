/*
 *  Copyright 2016
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.changetracker.dependencygraph;

/**
 * Stores the information on the edge of the operation dependency graph.
 * @author Katsuhisa Maruyama
 */
public class OpDepGraphEdge {
    
    /**
     * Defines the type of an edge.
     */
    public enum Sort {
        NORMAL, CPP;
    }
    
    /**
     * The node of the source of this edge.
     */
    protected OpDepGraphNode srcNode;
    
    /**
     * The node of the destination of this edge.
     */
    protected OpDepGraphNode dstNode;
    
    /**
     * The sort of this edge.
     */
    protected Sort sort;
    
    /**
     * Creates an empty instance.
     */
    protected OpDepGraphEdge() {
    }
    
    /**
     * Creates an edge between the two nodes.
     * @param src the node of the source of this edge
     * @param dst the node of the destination of this edge
     * @param sort the sort of this edge
     */
    public OpDepGraphEdge(OpDepGraphNode src, OpDepGraphNode dst, Sort sort) {
        this.srcNode = src;
        this.dstNode = dst;
        this.sort = sort;
    }
    
    /**
     * Creates an edge between the two nodes.
     * @param src the node of the source of this edge
     * @param dst the node of the destination of this edge
     */
    public OpDepGraphEdge(OpDepGraphNode src, OpDepGraphNode dst) {
        this(src, dst, Sort.NORMAL);
    }
    
    /**
     * Returns the node of the source of this edge.
     * @return the source node
     */
    public OpDepGraphNode getSrcNode() {
        return srcNode;
    }
    
    /**
     * Returns the node of the destination of this edge.
     * @return the destination node
     */
    public OpDepGraphNode getDstNode() {
        return dstNode;
    }
    
    /**
     * Returns the sort of this edge.
     * @return the sort of this edge
     */
    public Sort getSort() {
        return sort;
    }
    
    /**
     * Tests if a given graph edge is the same as this.
     * @param edge the edge of the operation dependency graph
     * @return <code>true</code> if the two edges are the same, otherwise <code>false</code>
     */
    public boolean equals(OpDepGraphEdge edge) {
        if (edge == null) {
            return false;
        }
        
        return this.getSort() == edge.getSort() && this.getSrcNode().equals(edge.getSrcNode()) && this.getDstNode().equals(edge.getDstNode());
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
     * Returns the string for printing, which does not contain a new line character at its end. 
     * @return the string for printing
     */
    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder();
        buf.append(getSrcNode().toSimpleString());
        buf.append(" -> ");
        buf.append(getDstNode().toSimpleString());
        buf.append(" ");
        buf.append(getSort());
        
        return buf.toString();
    }
}
