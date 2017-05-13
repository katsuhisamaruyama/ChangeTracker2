/*
 *  Copyright 2017
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.changetracker.dependecygraph;

import org.jtool.changetracker.dependencyanalyzer.JavaConstruct;
import org.jtool.changetracker.dependencyanalyzer.ParseableSnapshot;
import org.jtool.changetracker.operation.ICodeOperation;
import org.jtool.changetracker.repository.ChangeTrackerFile;
import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;
import java.util.Map;
import java.util.HashMap;
import java.time.ZonedDateTime;

/**
 * Stores information about an operation history graph for a file.
 * @author Katsuhisa Maruyama
 */
public class FileOpGraph {
    
    /**
     * The information about a file corresponding to this graph.
     */
    private ChangeTrackerFile fileInfo;
    
    /**
     * The collection of nodes of this graph.
     */
    private Map<String, OpGraphNode> nodes = new HashMap<String, OpGraphNode>();
    
    /**
     * The collection of edges of this graph.
     */
    private Set<OpGraphEdge> edges = new HashSet<OpGraphEdge>();
    
    /**
     * The time when this graph information was generated or last modified.
     */
    private ZonedDateTime lastUpdatedTime;
    
    /**
     * Creates an instance that stores information about an operation history graph for a file.
     * @param finfo information of the file
     */
    protected FileOpGraph(ChangeTrackerFile finfo) {
        this.fileInfo = finfo;
    }
    
    /**
     * Returns information about a file corresponding to this graph.
     * @return the file information.
     */
    public ChangeTrackerFile getFile() {
        return fileInfo;
    }
    
    /**
     * Clears this dependency graph.
     */
    void clear() {
        fileInfo = null;
        nodes.clear();
        edges.clear();
    }
    
    /**
     * Returns the qualified name of this graph.
     * @return the qualified name
     */
    String getQualifiedName() {
        return fileInfo.getQualifiedName();
    }
    
    /**
     * Adds a node to this graph.
     * @param node the node to be added
     */
    void add(OpGraphNode node) {
        nodes.put(node.getQualifiedName(), node);
    }
    
    /**
     * Adds an edge to this graph.
     * @param node the edge to be added
     */
    void add(OpGraphEdge edge) {
        edges.add(edge);
        edge.getSrcNode().addOutgoingEdge(edge);
        edge.getDstNode().addIncomingEdge(edge);
    }
    
    /**
     * Returns all the nodes in this graph.
     * @return the collection of the nodes
     */
    public Set<OpGraphNode> getNodes() {
        Set<OpGraphNode> ns = new HashSet<OpGraphNode>(nodes.size());
        for (OpGraphNode node : nodes.values()) {
            ns.add(node);
        }
        return ns;
    }
    
    /**
     * Returns all the edges in this graph.
     * @return the collection of the edges
     */
    public Set<OpGraphEdge> getEdges() {
        return edges;
    }
    
    /**
     * Obtains a node corresponding to a change operation in this graph.
     * @param op the change operation
     * @return the found node, or <code>null</code> if none
     */
    public OperationNode getOperationNode(ICodeOperation op) {
        OpGraphNode node = nodes.get(op.getQualifiedName());
        if (node.isOperation()) {
            return (OperationNode)node;
        }
        return null;
    }
    
    /**
     * Obtains a node corresponding to a Java construct in this graph.
     * @param con the Java construct
     * @param the found node, or <code>null</code> if none
     */
    public JavaMemberNode getJavaConstructNode(JavaConstruct con) {
        OpGraphNode node = nodes.get(con.getQualifiedName());
        if (node.isJavaConstruct()) {
            return (JavaMemberNode)node;
        }
        return null;
    }
    
    /**
     * Tests if there is an edge that connects between two nodes in this graph.
     * @param src the source node of the edge
     * @param dst the destination node of the edge
     * @return <code>true</code> an edge was found, otherwise <code>false</code>
     */
    public boolean isConnected(OpGraphNode src, OpGraphNode dst) {
        for (OpGraphEdge edge : edges) {
            if (edge.getSrcNode().equals(src) && edge.getDstNode().equals(dst)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Obtains source nodes that are connected to a node in this graph.
     * @param dst the destination node
     * @return the collection of the source nodes
     */
    public List<OpGraphNode> getSrcNodes(OpGraphNode dst) {
        List<OpGraphNode> ns = new ArrayList<OpGraphNode>();
        for (OpGraphEdge edge : edges) {
            if (edge.getDstNode().equals(dst)) {
                ns.add(edge.getSrcNode());
            }
        }
        return ns;
    }
    
    /**
     * Obtains destination nodes that are connected from a node in this graph.
     * @param src the source node
     * @return the collection of the destination nodes
     */
    public List<OpGraphNode> getDstNodes(OpGraphNode src) {
        List<OpGraphNode> ns = new ArrayList<OpGraphNode>();
        for (OpGraphEdge edge : edges) {
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
    public boolean contains(OpGraphNode node) {
        for (OpGraphNode n : nodes.values()) {
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
    public boolean contains(OpGraphEdge edge) {
        for (OpGraphEdge e : edges) {
            if (e.equals(edge)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Obtains change operation nodes that were performed before a time.
     * @param time the time
     * @return the collection of the change operation nodes
     */
    public List<OperationNode> getOperationNodesBefore(ZonedDateTime time) {
        List<OperationNode> ns = new ArrayList<OperationNode>();
        for (OpGraphNode node : nodes.values()) {
            if (node.isOperation() && (node.getTime().isBefore(node.getTime()) || node.getTime().isEqual(time))) {
                ns.add((OperationNode)node);
            }
        }
        OpGraphNode.<OperationNode>sortNodes(ns);
        return ns;
    }
    
    /**
     * Obtains change operation nodes that were performed after a time.
     * @param time the time
     * @return the collection of the change operation nodes
     */
    public List<OperationNode> getOperationNodesAfter(ZonedDateTime time) {
        List<OperationNode> ns = new ArrayList<OperationNode>();
        for (OpGraphNode node : nodes.values()) {
            if (node.isOperation() && (node.getTime().isAfter(node.getTime()) || node.getTime().isEqual(time))) {
                ns.add((OperationNode)node);
            }
        }
        
        OpGraphNode.<OperationNode>sortNodes(ns);
        return ns;
    }
    
    /**
     * Obtains all change operations stored in this graph.
     * @return the collection of the change operations
     */
    public List<ICodeOperation> getOperations() {
        List<ICodeOperation> operations = new ArrayList<ICodeOperation>();
        for (OpGraphNode node : nodes.values()) {
            if (node.isOperation()) {
                OperationNode opnode = (OperationNode)node;
                operations.add(opnode.getOperation());
            }
        }
        return operations;
    }
    
    /**
     * Returns the snapshots of files that appear in this graph.
     * @return the collection of the snapshots
     */
    public List<ParseableSnapshot> getSnapshots() {
        return fileInfo.getSnapshots();
    }
    
    /**
     * Sets the time when this graph was last generated or modified.
     * @param time the last generated or modified time
     */
    void setLastUpdatedTime(ZonedDateTime time) {
        lastUpdatedTime = time;
    }
    
    /**
     * Returns the time when this graph was last generated or modified.
     * @return the last generated or modified time
     */
    ZonedDateTime getLastUpdatedTime() {
        return lastUpdatedTime;
    }
    
    /**
     * Collects the change operation nodes in this graph.
     * @return the collection of the change operation nodes
     */
    List<OperationNode> getOperationNodes() {
        List<OperationNode> ns = new ArrayList<OperationNode>();
        for (OpGraphNode node : nodes.values()) {
            if (node.isOperation()) {
                ns.add((OperationNode)node);
            }
        }
        OpGraphNode.<OperationNode>sortNodes(ns);
        return ns;
    }
    
    /**
     * Collects the Java construct nodes in this graph.
     * @return the collection of the Java construct nodes
     */
    List<JavaMemberNode> getJavaConstructNodes() {
        List<JavaMemberNode> ns = new ArrayList<JavaMemberNode>();
        for (OpGraphNode node : nodes.values()) {
            if (node.isJavaConstruct()) {
                ns.add((JavaMemberNode)node);
            }
        }
        return ns;
    }
    
    /**
     * Returns the string for printing the nodes in this graph.
     * @return the string on the nodes
     */
    String toStringOnNodes() {
        List<OpGraphNode> ns = new ArrayList<OpGraphNode>(nodes.values());
        OpGraphNode.sortNodes(ns);
        return toStringOnNodes("All", ns);
    }
    
    /**
     * Returns the string for printing the change operation nodes in this graph.
     * @return the string on the change operation nodes
     */
    String toStringOnOperationNodes() {
        List<OperationNode> ns = getOperationNodes();
        OpGraphNode.<OperationNode>sortNodes(ns);
        return toStringOnNodes("Op", ns);
    }
    
    /**
     * Returns the string for printing the Java construct nodes in this graph.
     * @return the string on the Java construct nodes
     */
    String toStringOnJavaConstructNodes() {
        List<JavaMemberNode> ns = getJavaConstructNodes();
        OpGraphNode.<JavaMemberNode>sortNodes(ns);
        return toStringOnNodes("Jc", ns);
    }
    
    /**
     * Returns the string for printing the nodes in this graph.
     * @param prefix the prefix string
     * @param the collection of the nodes
     * @return the string on the nodes
     */
    String toStringOnNodes(String prefix, List<? extends OpGraphNode> ns) {
        StringBuilder buf = new StringBuilder();
        buf.append(prefix);
        buf.append("Nodes(" + ns.size() + "):\n");
        for (OpGraphNode node : ns) {
            buf.append(node.toString());
            buf.append("\n");
        }
        return buf.toString();
    }
    
    /**
     * Returns the string for printing the edges in this graph.
     * @return the string on the edges
     */
    String toStringOnEdges() {
        List<OpGraphEdge> es = new ArrayList<OpGraphEdge>(edges);
        OpGraphEdge.<OpGraphEdge>sortEdges(es);
        return toStringOnEdges("All", es);
    }
    
    /**
     * Returns the string for printing the edges in this graph.
     * @return the string on the edges
     */
    String toStringOnEdges(OpGraphEdge.Sort sort) {
        List<OpGraphEdge> es = new ArrayList<OpGraphEdge>();
        for (OpGraphEdge edge : edges) {
            if (sort.equals(edge.getSort())) {
                es.add(edge);
            }
        }
        OpGraphEdge.<OpGraphEdge>sortEdges(es);
        return toStringOnEdges(sort.toString(), es);
    }
    
    /**
     * Returns the string for printing the edges in this graph.
     * @param prefix the prefix string
     * @param the collection of the edges
     * @return the string on the edges
     */
    String toStringOnEdges(String prefix, List<? extends OpGraphEdge> es) {
        StringBuilder buf = new StringBuilder();
        buf.append(prefix);
        buf.append("Edges(" + es.size() + "):\n");
        for (OpGraphEdge edge : es) {
            buf.append(edge.toString());
            buf.append("\n");
        }
        return buf.toString();
    }
    
    /**
     * Returns the string for printing.
     * @return the string for printing
     */
    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder();
        buf.append("----- Graph (from here) -----\n");
        buf.append(toStringOnNodes());
        buf.append(toStringOnEdges());
        buf.append("----- Graph (to here) -----\n");
        return buf.toString();
    }
    
    /**
     * Returns the string for printing.
     * @return the string for printing
     */
    public String toSummaryString() {
        StringBuilder buf = new StringBuilder();
        buf.append("-- Graph: " + fileInfo.getQualifiedName());
        buf.append(" N=" + nodes.size());
        buf.append("(" + "O:" + getOperationNodes().size() + ")");
        buf.append("(" + "J:" + getJavaConstructNodes().size() + ")");
        buf.append(" E=" + edges.size());
        buf.append("(" + getEdgeInfo(OpGraphEdge.Sort.BACKWARD_EDITING) + ")");
        buf.append("(" + getEdgeInfo(OpGraphEdge.Sort.FORWARD_EDITING) + ")");
        buf.append("(" + getEdgeInfo(OpGraphEdge.Sort.NO_CHANGE) + ")");
        buf.append("(" + getEdgeInfo(OpGraphEdge.Sort.CCP_EDITING) + ")");
        buf.append(" SN=" + getSnapshots().size());
        return buf.toString();
    }
    
    /**
     * Returns the edge information.
     * @param sort the sort of the edge
     * @return the string for printing
     */
    private String getEdgeInfo(OpGraphEdge.Sort sort) {
        return sort.toString() + ":" + OpGraphEdge.<OpGraphEdge>extractEdges(edges, sort).size();
    }
}
