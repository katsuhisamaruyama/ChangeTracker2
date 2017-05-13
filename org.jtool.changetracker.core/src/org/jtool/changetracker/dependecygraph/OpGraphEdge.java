/*
 *  Copyright 2017
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.changetracker.dependecygraph;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.Collections;
import java.util.Comparator;
import java.time.ZonedDateTime;

/**
 * Stores the information on the edge of the operation dependency graph.
 * @author Katsuhisa Maruyama
 */
public class OpGraphEdge {
    
    /**
     * Defines the type of an edge.
     */
    public enum Sort {
        BACKWARD_EDITING, FORWARD_EDITING, NO_CHANGE, CCP_EDITING, ORDERED_EDITING;
        
        /**
         * Returns the string for printing. 
         * @return the string for printing
         */
        @Override
        public String toString() {
            return toString().substring(0, 0);
        }
    }
    
    /**
     * The node of the source of this edge.
     */
    protected OpGraphNode srcNode;
    
    /**
     * The node of the destination of this edge.
     */
    protected OpGraphNode dstNode;
    
    /**
     * The sort of this edge.
     */
    protected Sort sort;
    
    /**
     * Creates an empty instance.
     */
    protected OpGraphEdge() {
    }
    
    /**
     * Creates an edge between the two nodes.
     * @param src the node of the source of this edge
     * @param dst the node of the destination of this edge
     * @param sort the sort of this edge
     */
    public OpGraphEdge(OpGraphNode src, OpGraphNode dst, Sort sort) {
        this.srcNode = src;
        this.dstNode = dst;
        this.sort = sort;
    }
    
    /**
     * Returns the node of the source of this edge.
     * @return the source node
     */
    public OpGraphNode getSrcNode() {
        return srcNode;
    }
    
    /**
     * Returns the node of the destination of this edge.
     * @return the destination node
     */
    public OpGraphNode getDstNode() {
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
     * Tests if this edge represents backward editing that connects from a Java construct node to an operation node.
     * @return <code>true</code> if this edge represents backward editing, otherwise <code>false</code>
     */
    public boolean isBackwardEdit() {
        return Sort.BACKWARD_EDITING.toString().equals(sort);
    }
    
    /**
     * Tests if this edge represents forward editing that connects from an operation node to a Java construct node.
     * @return <code>true</code> if this edge represents forward editing, otherwise <code>false</code>
     */
    public boolean isForwardEdit() {
        return Sort.BACKWARD_EDITING.toString().equals(sort);
    }
    
    /**
     * Tests if this edge represents no change that connects between Java constructs.
     * @return <code>true</code> if this edge represents no change, otherwise <code>false</code>
     */
    public boolean isNoChange() {
        return Sort.NO_CHANGE.toString().equals(sort);
    }
    
    /**
     * Tests if this edge represents cut-copy-paste editing that connects from a cut or copy node to a paste node.
     * @return <code>true</code> if this edge represents cut-copy-paste editing, otherwise <code>false</code>
     */
    public boolean isCCP() {
        return Sort.CCP_EDITING.toString().equals(sort);
    }
    
    /**
     * Tests if this edge represents ordered editing that connects between operation nodes in dependent order.
     * @return <code>true</code> if this edge represents cut-copy-paste editing, otherwise <code>false</code>
     */
    public boolean isOrdered() {
        return Sort.ORDERED_EDITING.toString().equals(sort);
    }
    
    /**
     * Tests if a given graph edge is the same as this.
     * @param edge the edge of the operation dependency graph
     * @return <code>true</code> if the two edges are the same, otherwise <code>false</code>
     */
    public boolean equals(OpGraphEdge edge) {
        if (edge == null) {
            return false;
        }
        
        return getSort() == edge.getSort() && getSrcNode().equals(edge.getSrcNode()) &&
               getDstNode().equals(edge.getDstNode());
    }
    
    /**
     * Tests if this object is the same as a given object.
     * @param obj the object
     * @return <code>true</code> if the two objects are the same, otherwise <code>false</code>
     */
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof OpGraphEdge) {
            return equals((OpGraphEdge)obj);
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
        buf.append(getSrcNode().getQualifiedName());
        buf.append(" -> ");
        buf.append(getDstNode().getQualifiedName());
        buf.append(" ");
        buf.append(getSort().toString());
        return buf.toString();
    }
    
    /**
     * Returns edges with a given sort.
     * @param edges the collection of edges
     * @param sort the sort of the edges to be extracted
     * @return the collection of the extracted edges
     */
    public static <T extends OpGraphEdge> List<T> extractEdges(Set<T> edges, T.Sort sort) {
        List<T> es = new ArrayList<T>();
        for (T edge : edges) {
            if (sort.equals(edge.getSort())) {
                es.add(edge);
            }
        }
        sortEdgesByDstTime(es);
        sortEdgesBySrcTime(es);
        return es;
    }
    
    
    /**
     * Sorts the edges of the operation dependency graph.
     * @param es the collection of the edges to be sorted
     */
    public static <T extends OpGraphEdge> void sortEdges(List<T> es) {
        sortEdgesBySort(es);
        sortEdgesByDstTime(es);
        sortEdgesBySrcTime(es);
    }
    
    /**
     * Sorts the edges of the operation dependency graph.
     * @param es the collection of the edges to be sorted
     */
    static <T extends OpGraphEdge> void sortEdgesBySrcTime(List<T> es) {
        Collections.sort(es, new Comparator<T>() {
            
            /**
             * Compares its two nodes in time order of source nodes.
             * @param node1 the first node to be compared.
             * @param node2 the second node to be compared.
             */
            public int compare(T edge1, T edge2) {
                ZonedDateTime time1 = edge1.getSrcNode().getTime();
                ZonedDateTime time2 = edge2.getSrcNode().getTime();
                
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
    
    /**
     * Sorts the edges of the operation dependency graph.
     * @param es the collection of the edges to be sorted
     */
    static <T extends OpGraphEdge> void sortEdgesByDstTime(List<T> es) {
        Collections.sort(es, new Comparator<T>() {
            
            /**
             * Compares its two nodes in time order of destination nodes.
             * @param node1 the first node to be compared.
             * @param node2 the second node to be compared.
             */
            public int compare(T edge1, T edge2) {
                ZonedDateTime time1 = edge1.getDstNode().getTime();
                ZonedDateTime time2 = edge2.getDstNode().getTime();
                
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
    
    /**
     * Sorts the edges of the operation dependency graph.
     * @param es the collection of the edges to be sorted
     */
    static <T extends OpGraphEdge> void sortEdgesBySort(List<T> es) {
        Collections.sort(es, new Comparator<T>() {
            
            /**
             * Compares its two nodes in the alphabetical order of the sort of edges.
             * @param node1 the first node to be compared.
             * @param node2 the second node to be compared.
             */
            public int compare(T edge1, T edge2) {
                String str1 = edge1.getSort().toString();
                String str2 = edge2.getSort().toString();
                return str1.compareTo(str2);
            }
        });
    }
}
