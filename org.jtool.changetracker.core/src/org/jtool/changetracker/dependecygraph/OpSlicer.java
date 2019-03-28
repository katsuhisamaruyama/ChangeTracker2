/*
 *  Copyright 2017-2019
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.changetracker.dependecygraph;

import java.util.List;
import java.util.ArrayList;

/**
 * Slices an operation history graph, starting from a Java construct node.
 * @author Katsuhisa Maruyama
 */
public class OpSlicer {
    
    /**
     * Obtains change operation nodes in a backward slice on a Java construct node of interest.
     * @param jcnode the Java construct node
     * @return the collection of the change operation nodes in the backward slice
     */
    static List<OperationNode> getOperationNodesInBackwardSlice(JavaMemberNode jcnode) {
        List<OpGraphNode> nodes = new ArrayList<OpGraphNode>();
        if (jcnode != null) {
            collectReachableNodesTo(nodes, jcnode);
            collectNodesWithoutForwardEdges(nodes);
        }
        
        List<OperationNode> ns = new ArrayList<OperationNode>();
        for (OpGraphNode n : nodes) {
            if (n.isOperation()) {
                ns.add((OperationNode)n);
            }
        }
        
        OpGraphNode.sortNodes(ns);
        return ns;
    }
    
    /**
     * Collects all nodes that reach a specified node of an operation history graph.
     * @param nodes the collection of the reachable nodes, which is to be returned
     * @param node the specified node 
     */
    private static void collectReachableNodesTo(List<OpGraphNode> nodes, OpGraphNode node) {
        if (nodes.contains(node)) {
            return;
        }
        nodes.add(node);
        
        for (OpGraphEdge edge : node.getIncomingEdges()) {
            if (!edge.isOrdered()) {
                collectReachableNodesTo(nodes, edge.getSrcNode());
            }
        }
    }
    
    /**
     * Collects operation nodes that are connected to nodes in the slice, which have no forward edit edges. 
     * @param nodes the collection of the nodes including the added nodes, which is to be returned
     */
    private static void collectNodesWithoutForwardEdges(List<OpGraphNode> nodes) {
        List<OpGraphNode> ns = new ArrayList<OpGraphNode>();
        for (OpGraphNode n : nodes) {
            for (OpGraphEdge e : n.getOutgoingEdges()) {
                if (e.getSort() == OpGraphEdge.Sort.BACKWARD_EDITING) {
                    if (!nodes.contains(e.getDstNode())) {
                        ns.add(e.getDstNode());
                    }
                }
            }
        }
        nodes.addAll(ns);
    }
    
    /**
     * Obtains change operation nodes in a forward slice on a Java construct node of interest.
     * @param jcnode the Java construct node
     * @return the collection of the change operation nodes in the forward slice
     */
    static List<OperationNode> getOperationNodesInForwardSlice(JavaMemberNode jcnode) {
        List<OpGraphNode> nodes = new ArrayList<OpGraphNode>();
        if (jcnode != null) {
            collectReachableNodesFrom(nodes, jcnode);
            collectNodesWithoutBackwardEdges(nodes);
        }
        
        List<OperationNode> ns = new ArrayList<OperationNode>();
        for (OpGraphNode n : nodes) {
            if (n.isOperation()) {
                ns.add((OperationNode)n);
            }
        }
        
        OpGraphNode.sortNodes(ns);
        return ns;
    }
    
    /**
     * Collects all nodes that a specified node of an operation history graph reaches.
     * @param nodes the collection of the reachable nodes, which is to be returned
     * @param node the specified node 
     */
    private static void collectReachableNodesFrom(List<OpGraphNode> nodes, OpGraphNode node) {
        if (nodes.contains(node)) {
            return;
        }
        nodes.add(node);
        
        for (OpGraphEdge edge : node.getOutgoingEdges()) {
            if (!edge.isOrdered()) {
                collectReachableNodesFrom(nodes, edge.getDstNode());
            }
        }
    }
    
    /**
     * Collects operation nodes that are connected to nodes in the slice, which have no backward edit edges. 
     * @param nodes the collection of the nodes including the added nodes, which is to be returned
     */
    private static void collectNodesWithoutBackwardEdges(List<OpGraphNode> nodes) {
        List<OpGraphNode> ns = new ArrayList<OpGraphNode>();
        for (OpGraphNode n : nodes) {
            for (OpGraphEdge e : n.getOutgoingEdges()) {
                if (e.getSort() == OpGraphEdge.Sort.BACKWARD_EDITING) {
                    if (!nodes.contains(e.getDstNode())) {
                        ns.add(e.getDstNode());
                    }
                }
            }
        }
        nodes.addAll(ns);
    }
}
