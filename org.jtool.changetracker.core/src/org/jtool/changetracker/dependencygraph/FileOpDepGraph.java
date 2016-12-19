/*
 *  Copyright 2016
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.changetracker.dependencygraph;

import org.jtool.changetracker.operation.ICodeOperation;
import org.jtool.changetracker.repository.FileInfo;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;

/**
 * Stores information about the operation dependency graph for a file.
 * @author Katsuhisa Maruyama
 */
public class FileOpDepGraph {
    
    /**
     * The information about a file corresponding to this graph.
     */
    private FileInfo fileInfo;
    
    /**
     * The collection of nodes of this graph.
     */
    private Set<OpDepGraphNode> nodes;
    
    /**
     * The collection of edges of this graph.
     */
    private Set<OpDepGraphEdge> edges;
    
    /**
     * The time when this graph information was generated or last modified.
     */
    private ZonedDateTime lastUpdatedTime;
    
    /**
     * Creates an instance that stores information about the operation dependency graph for a file.
     * @param finfo the information of the file
     */
    protected FileOpDepGraph(FileInfo finfo) {
        this.fileInfo = finfo;
        this.nodes = new HashSet<OpDepGraphNode>();
        this.edges = new HashSet<OpDepGraphEdge>();
    }
    
    /**
     * Returns information about the file corresponding to this graph.
     * @return the file information.
     */
    public FileInfo getFileInfo() {
        return fileInfo;
    }
    
    /**
     * Clears this operation dependency graph.
     */
    void clear() {
        fileInfo = null;
        nodes.clear();
        edges.clear();
    }
    
    /**
     * Returns the key for an operation dependence graph for each file.
     * @param finfo the information about the file
     * @return the key string
     */
    static String getKey(FileInfo finfo) {
        return finfo.getQualifiedName() + "!" + finfo.getFromTime().toInstant().toEpochMilli();
    }
    
    /**
     * Adds a node to this graph.
     * @param node the node to be added
     */
    void add(OpDepGraphNode node) {
        nodes.add(node);
    }
    
    /**
     * Adds an edge to this graph.
     * @param node the edge to be added
     */
    void add(OpDepGraphEdge edge) {
        edges.add(edge);
        edge.getSrcNode().addOutgoingEdge(edge);
        edge.getDstNode().addIncomingEdge(edge);
    }
    
    /**
     * Returns all the nodes in this graph.
     * @return the collection of the nodes
     */
    public Set<OpDepGraphNode> getAllNodes() {
        return nodes;
    }
    
    /**
     * Returns all the edges in this graph.
     * @return the collection of the edges
     */
    public Set<OpDepGraphEdge> getAllEdges() {
        return edges;
    }
    
    /**
     * Obtains an operation node by its identification number.
     * @param id the identification number of the node
     * @return the found operation node, or <code>null</code> if none
     */
    public OpDepGraphNode getNode(int id) {
        for (OpDepGraphNode node : nodes) {
            if (node.getId() == id) {
                return node;
            }
        }
        return null;
    }
    
    /**
     * Tests if there is an edge that connects between two nodes.
     * @param src the source node of the edge
     * @param dst the destination node of the edge
     * @return <code>true</code> an edge was found, otherwise <code>false</code>
     */
    public boolean connect(OpDepGraphNode src, OpDepGraphNode dst) {
        for (OpDepGraphEdge edge : edges) {
            if (edge.getSrcNode().equals(src) && edge.getDstNode().equals(dst)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Obtains nodes that is the source of a node.
     * @param dst the destination node
     * @return the collection of the source nodes
     */
    public List<OpDepGraphNode> getSrcNodes(OpDepGraphNode dst) {
        List<OpDepGraphNode> ns = new ArrayList<OpDepGraphNode>();
        for (OpDepGraphEdge edge : edges) {
            if (edge.getDstNode().equals(dst)) {
                ns.add(edge.getSrcNode());
            }
        }
        return ns;
    }
    
    /**
     * Obtains nodes that is the destination of a node.
     * @param src the source node
     * @return the collection of the destination nodes
     */
    public List<OpDepGraphNode> getDstNodes(OpDepGraphNode src) {
        List<OpDepGraphNode> ns = new ArrayList<OpDepGraphNode>();
        for (OpDepGraphEdge edge : edges) {
            if (edge.getSrcNode().equals(src)) {
                ns.add(edge.getDstNode());
            }
        }
        return ns;
    }
    
    /**
     * Tests if this graph contains a node.
     * @param node the node to be checked
     * @return <code>true</code> if this graph contains the node, otherwise <code>false</code>
     */
    public boolean contains(OpDepGraphNode node) {
        for (OpDepGraphNode n : nodes) {
            if (n.equals(node)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Tests if this graph contains an edge.
     * @param edge the edge to be checked
     * @return <code>true</code> if this graph contains the edge, otherwise <code>false</code>
     */
    public boolean contains(OpDepGraphEdge edge) {
        for (OpDepGraphEdge e : edges) {
            if (e.equals(edge)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Obtains operation nodes performed before a time.
     * @param time the time
     * @return the collection of the operation nodes
     */
    public List<OpDepGraphNode> getNodesBefore(ZonedDateTime time) {
        List<OpDepGraphNode> ns = new ArrayList<OpDepGraphNode>();
        for (OpDepGraphNode node : nodes) {
            if (node.getTime().isBefore(node.getTime()) || node.getTime().isEqual(time)) {
                ns.add(node);
            }
        }
        
        ProjectOpDepGraph.sortNodes(ns);
        return ns;
    }
    
    /**
     * Obtains operation nodes performed after a time.
     * @param time the time
     * @return the collection of the operation nodes
     */
    public List<OpDepGraphNode> getNodesAfter(ZonedDateTime time) {
        List<OpDepGraphNode> ns = new ArrayList<OpDepGraphNode>();
        for (OpDepGraphNode node : nodes) {
            if (node.getTime().isAfter(node.getTime()) || node.getTime().isEqual(time)) {
                ns.add(node);
            }
        }
        
        ProjectOpDepGraph.sortNodes(ns);
        return ns;
    }
    
    /**
     * Obtains all the operations stored in this graph.
     * @return the collection of the operations
     */
    public List<ICodeOperation> getOperations() {
        List<ICodeOperation> operations = new ArrayList<ICodeOperation>();
        for (OpDepGraphNode node : nodes) {
            operations.add(node.getOperation());
        }
        return operations;
    }
    
    /**
     * Obtains the operation node corresponding to a code change operation.
     * @param operation the code change operation
     * @return the found operation node, or <code>null</code> if node
     */
    public OpDepGraphNode getOperationNode(ICodeOperation operation) {
        for (OpDepGraphNode node : nodes) {
            if (node.getTime().isEqual(operation.getTime())) {
                return node;
            }
        }
        return null;
    }
    
    /**
     * Sets the time when this graph information was generated or last modified.
     * @param time the last generated or modified time
     */
    void setLastUpdatedTime(ZonedDateTime time) {
        lastUpdatedTime = time;
    }
    
    /**
     * Returns the time when this graph information was generated or last modified.
     * @return the last generated or modified time
     */
    ZonedDateTime getLastUpdatedTime() {
        return lastUpdatedTime;
    }
    
    /**
     * Stores the string for printing nodes into a buffer. 
     * @param buf the string buffer
     */
    private void printNodes(StringBuilder buf) {
        List<OpDepGraphNode> ns = new ArrayList<OpDepGraphNode>();
        for (OpDepGraphNode node : nodes) {
            ns.add(node);
        }
        ProjectOpDepGraph.sortNodes(ns);
        
        buf.append("Nodes(" + ns.size() + "):\n");
        for (OpDepGraphNode node : ns) {
            buf.append(node.toString());
            buf.append("\n");
        }
    }
    
    /**
     * Stores the string for printing edges into a buffer.
     * @param buf the string buffer
     */
    private void printEdges(StringBuilder buf) {
        List<OpDepGraphEdge> es = new ArrayList<OpDepGraphEdge>();
        for (OpDepGraphEdge edge : edges) {
            es.add(edge);
        }
        ProjectOpDepGraph.sortEdgesByDstId(es);
        ProjectOpDepGraph.sortEdgesBySrcId(es);
        
        buf.append("Edges(" + es.size() + "):\n");
        for (OpDepGraphEdge edge : es) {
            buf.append(edge.toString());
            buf.append("\n");
        }
    }
    
    /**
     * Returns the string for printing.
     * @return the string for printing
     */
    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder();
        
        buf.append("----- Graph (from here) -----\n");
        printNodes(buf);
        printEdges(buf);
        buf.append("----- Graph (to here) -----\n");
        
        return buf.toString();
    }
    
    /**
     * Returns the string for printing.
     * @return the string for printing
     */
    public String toStringSimple() {
        StringBuilder buf = new StringBuilder();
        
        buf.append("-- Graph " + getFileInfo().getName());
        buf.append(" N=" + nodes.size());
        buf.append(" E=" + edges.size());
        buf.append("(" + ProjectOpDepGraph.extractEdges(edges, OpDepGraphEdge.Sort.NORMAL).size() + ")");
        buf.append("(" + ProjectOpDepGraph.extractEdges(edges, OpDepGraphEdge.Sort.CPP).size() + ")");
        
        return buf.toString();
    }
}
