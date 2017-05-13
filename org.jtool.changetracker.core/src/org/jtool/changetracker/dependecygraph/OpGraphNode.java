/*
 *  Copyright 2017
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.changetracker.dependecygraph;

import java.util.List;
import java.util.Set;
import java.util.HashSet;
import java.util.Collections;
import java.util.Comparator;
import java.time.ZonedDateTime;

/**
 * Stores information about a node of the operation dependency graph.
 * @author Katsuhisa Maruyama
 */
public abstract class OpGraphNode {
    
    /**
     * The operation history graph for a file that contains this node.
     */
    protected FileOpGraph fileGraph;
    
    /**
     * The collection of edges incoming to this node.
     */
    protected HashSet<OpGraphEdge> incomingEdges = new HashSet<OpGraphEdge>();
    
    /**
     * The collection of edges outgoing from this node.
     */
    protected HashSet<OpGraphEdge> outgoingEdges = new HashSet<OpGraphEdge>();
    
    /**
     * Creates a node of an operation dependence graph.
     * @param fgraph an operation history graph for a file that contains this node
     */
    protected OpGraphNode(FileOpGraph fgraph) {
        fileGraph = fgraph;
    }
    
    /**
     * Returns an operation history graph for a file that contains this node.
     * @return the operation history graph for the file
     */
    public FileOpGraph getFileGraph() {
        return fileGraph;
    }
    
    /**
     * Tests if this is a change operation node.
     * @return <code>true</code> if this is a change operation node, otherwise <code>false</code>
     */
    public boolean isOperation() {
        return false;
    }
    
    /**
     * Tests if this is a Java class member node.
     * @return always <code>true</code>
     */
    public boolean isJavaConstruct() {
        return false;
    }
    
    /**
     * Returns the qualified name for this node.
     * @return the qualified name
     */
    public abstract String getQualifiedName();
    
    /**
     * Returns the time for this node.
     * @return the time
     */
    public abstract ZonedDateTime getTime();
    
    /**
     * Returns the index number for this node.
     * @return the index number
     */
    public abstract int getIndex();
    
    /**
     * Returns the leftmost offset value within the code related to this node.
     * @return the offset value
     */
    public abstract int getStart();
    
    /**
     * Adds an edge incoming to this node.
     * @param edge the incoming edge to be added
     */
    void addIncomingEdge(OpGraphEdge edge) {
        incomingEdges.add(edge);
    }
    
    /**
     * Adds an edge outgoing to this node.
     * @param edge the outgoing edge to be added
     */
    void addOutgoingEdge(OpGraphEdge edge) {
        outgoingEdges.add(edge);
    }
    
    /**
     * Removes an edge incoming from this node.
     * @param edge the incoming edge to be removed
     */
    void removeIncomingEdge(OpGraphEdge edge) {
        incomingEdges.remove(edge);
    }
    
    /**
     * Removes an edge outgoing from this node.
     * @param edge the outgoing edge to be removed
     */
    void removeOutgoingEdge(OpGraphEdge edge) {
        outgoingEdges.remove(edge);
    }
    
    /**
     * Returns the edges incoming to this node.
     * @return the set of incoming edges
     */
    public Set<OpGraphEdge> getIncomingEdges() {
        return incomingEdges;
    }
    
    /**
     * Returns the edges outgoing from this node.
     * @return the set of incoming edges
     */
    public Set<OpGraphEdge> getOutgoingEdges() {
        return outgoingEdges;
    }
    
    /**
     * Returns the nodes of the source of this node.
     * @return the set of the source nodes
     */
    public Set<OpGraphNode> getSrcNodes() {
        Set<OpGraphNode> nodes = new HashSet<OpGraphNode>();
        for (OpGraphEdge edge : incomingEdges) {
            nodes.add(edge.getSrcNode());
        }
        return nodes;
    }
    
    /**
     * Returns the nodes of the destination of this node.
     * @return the set of the destination nodes
     */
    public Set<OpGraphNode> getDstNodes() {
        Set<OpGraphNode> nodes = new HashSet<OpGraphNode>();
        for (OpGraphEdge edge : outgoingEdges) {
            nodes.add(edge.getDstNode());
        }
        return nodes;
    }
    
    /**
     * Tests if a given node is the same as this.
     * @param node the node of the operation dependency graph
     * @return <code>true</code> if the two nodes are the same, otherwise <code>false</code>
     */
    public boolean equals(OpGraphNode node) {
        if (node == null) {
            return false;
        }
        
        return getQualifiedName().equals(node.getQualifiedName());
    }
    
    /**
     * Tests if this object is the same as a given object.
     * @param obj the object
     * @return <code>true</code> if the two objects are the same, otherwise <code>false</code>
     */
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof OpGraphNode) {
            return equals((OpGraphNode)obj);
        }
        return false;
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
        buf.append("qname:[" + getQualifiedName() + "] ");
        buf.append("time:[" + getTime() + "] ");
        return buf.toString();
    }
    
    /**
     * Sorts the nodes of the operation dependency graph.
     * @param ns the collection of the nodes to be sorted
     */
    public static <T extends OpGraphNode> void sortNodes(List<T> ns) {
        Collections.sort(ns, new Comparator<T>() {
            
            /**
             * Compares its two nodes in time order.
             * @param node1 the first node to be compared.
             * @param node2 the second node to be compared.
             */
            public int compare(T node1, T node2) {
                ZonedDateTime time1 = node1.getTime();
                ZonedDateTime time2 = node2.getTime();
                
                if (time1.isAfter(time2)) {
                    return 1;
                } else if (time1.isBefore(time2)) {
                    return -1;
                } else {
                    return 0;
                }
            }
        });
    }
}
