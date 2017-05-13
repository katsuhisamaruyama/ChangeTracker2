/*
 *  Copyright 2017
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.changetracker.dependecygraph;

import org.jtool.changetracker.dependencyanalyzer.DependencyDetector;
import org.jtool.changetracker.operation.ICodeOperation;
import org.jtool.changetracker.operation.CodeOperation;
import java.util.List;
import java.util.ArrayList;

/**
 * Stores information on a code snippet.
 * @author Katsuhisa Maruyama
 */
public class OpSlicer2 {
    
    /**
     * Obtains change operation nodes in a backward slice on a code snippet of interest.
     * @param fgraph an operation history graph for a file containing the code snippet
     * @param snip the code snippet
     * @return the collection of the change operation nodes in the backward slice
     */
    static List<OperationNode> getOperationNodesInBackwardSlice(FileOpGraph fgraph, CodeSnippet snip) {
        List<OperationNode> ns = new ArrayList<OperationNode>();
        for (OperationNode node : getBackwardOperationNodes(fgraph, snip)) {
            collectReachableNodesTo(ns, node);
        }
        
        OpGraphNode.sortNodes(ns);
        return ns;
    }
    
    /**
     * Collects all nodes that reach a specified node of an operation history graph.
     * @param nodes the collection of the reachable nodes, which is to be returned
     * @param node the specified node 
     */
    private static void collectReachableNodesTo(List<OperationNode> nodes, OperationNode node) {
        if (nodes.contains(node)) {
            return;
        }
        nodes.add(node);
        
        for (OpGraphEdge edge : node.getIncomingEdges()) {
            if (edge.isOrdered() || edge.isCCP()) {
                collectReachableNodesTo(nodes, (OperationNode)edge.getSrcNode());
            }
        }
    }
    
    /**
     * Obtains change operation nodes that affect a code snippet.
     * @param fgraph the operation graph for a file containing the code snippet
     * @param snip the code snippet
     * @return the collection of the change operation nodes
     */
    static List<OperationNode> getBackwardOperationNodes(FileOpGraph fgraph, CodeSnippet snip) {
        List<OperationNode> ns = new ArrayList<OperationNode>();
        List<OperationNode> nodes = fgraph.getOperationNodesBefore(snip.getTime());
        for (OperationNode node : nodes) {
            List<CodeOperation> ops = CodeOperation.getOperations(fgraph.getFile(), node.getTime(), snip.getTime());
            for (int o = snip.getStart(); o < snip.getStart() + snip.getLength(); o++) {
                int offset = DependencyDetector.adjustBackwardOffset(o, ops);
                if (isIn(offset, node)) {
                    ns.add(node);
                    break;
                }
            }
        }
        return ns;
    }
    
    /**
     * Obtains change operation nodes in a forward slice on a code snippet of interest.
     * @param fgraph an operation history graph for a file containing the code snippet
     * @param snippet the code snippet
     * @return the collection of the change operation nodes in the backward slice
     */
    static List<OperationNode> getOperationNodesInForwardSlice(FileOpGraph fgraph, CodeSnippet snip) {
        List<OperationNode> ns = new ArrayList<OperationNode>();
        for (OperationNode node : getForwardOperationNodes(fgraph, snip)) {
            collectReachableNodesFrom(ns, node);
        }
        
        OpGraphNode.sortNodes(ns);
        return ns;
    }
    
    /**
     * Collects all nodes that a specified node of an operation history graph reaches.
     * @param nodes the collection of the reachable nodes, which is to be returned
     * @param node the specified node 
     */
    private static void collectReachableNodesFrom(List<OperationNode> nodes, OperationNode node) {
        if (nodes.contains(node)) {
            return;
        }
        nodes.add(node);
        
        for (OpGraphEdge edge : node.getOutgoingEdges()) {
            if (edge.isOrdered() || edge.isCCP()) {
                collectReachableNodesFrom(nodes, (OperationNode)edge.getDstNode());
            }
        }
    }
    
    /**
     * Obtains change operation nodes that a code snippet affects.
     * @param fgraph the operation graph for a file containing the code snippet
     * @param snippet the code snippet
     * @return the collection of the change operation nodes
     */
    static List<OperationNode> getForwardOperationNodes(FileOpGraph fgraph, CodeSnippet snip) {
        List<OperationNode> ns = new ArrayList<OperationNode>();
        List<OperationNode> nodes = fgraph.getOperationNodesAfter(snip.getTime());
        for (OperationNode node : nodes) {
            List<CodeOperation> ops = CodeOperation.getOperations(fgraph.getFile(), node.getTime(), snip.getTime());
            for (int o = snip.getStart(); o < snip.getStart() + snip.getLength(); o++) {
                int offset = DependencyDetector.adjustForwardOffset(o, ops);
                if (isIn(offset, node)) {
                    ns.add(node);
                    break;
                }
            }
        }
        return ns;
    }
    
    /**
     * Tests if the offset value is related to a change operation.
     * @param offset the offset value to be checked
     * @param node the node for the change operation
     * @return <code>true</code> if the offset value is related to a change operation, otherwise <code<false</code>
     */
    private static boolean isIn(int offset, OperationNode node) {
        ICodeOperation op = node.getOperation();
        if (op.getStart() < offset && offset < op.getStart() + op.getInsertedText().length()) {
            return true;
        }
        if (op.getStart() <= offset && offset < op.getStart() + op.getDeletedText().length()) {
            return true;
        }
        if (op.getStart() <= offset && offset < op.getStart() + op.getCopiedText().length()) {
            return true;
        }
        return false;
    }
}
