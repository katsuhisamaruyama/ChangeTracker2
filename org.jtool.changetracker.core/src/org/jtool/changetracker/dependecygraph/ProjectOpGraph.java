/*
 *  Copyright 2017
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.changetracker.dependecygraph;

import org.jtool.changetracker.dependencyanalyzer.JavaConstruct;
import org.jtool.changetracker.dependencyanalyzer.ParseableSnapshot;
import org.jtool.changetracker.operation.ICodeOperation;
import org.jtool.changetracker.repository.CTFile;
import org.jtool.changetracker.repository.CTProject;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;

/**
 * Stores information about an operation history graph for a project.
 * @author Katsuhisa Maruyama
 */
public class ProjectOpGraph {
    
    /**
     * The information of a project corresponding to this graph.
     */
    private CTProject projectInfo;
    
    /**
     * The collection of all operation history graphs for files within the project.
     */
    private Map<String, FileOpGraph> fileGraphs = new HashMap<String, FileOpGraph>();
    
    /**
     * The collection of edges that intertwines nodes existing in operation history graphs for different files.
     */
    private Set<OpGraphEdge> interEdges = new HashSet<OpGraphEdge>();
    
    /**
     * Creates an instance that stores information about an operation history graph for a project.
     * @param pinfo the information of the project
     */
    protected ProjectOpGraph(CTProject pinfo) {
        this.projectInfo = pinfo;
    }
    
    /**
     * Returns information of a project corresponding to this graph.
     * @return the project information.
     */
    public CTProject getProject() {
        return projectInfo;
    }
    
    /**
     * Clears this graph.
     */
    void clear() {
        for (FileOpGraph graph : fileGraphs.values()) {
            graph.clear();
        }
        interEdges.clear();
    }
    
    /**
     * Finds an operation history graph for a file.
     * @param finfo information about the file
     * @return the operation history graph for the file, or <code>null</code> if none
     */
    FileOpGraph get(CTFile finfo) {
        return fileGraphs.get(finfo.getQualifiedName());
    }
    
    /**
     * Adds an operation history graph for a file.
     * @param fgraph the operation history graph to be added
     */
    void add(FileOpGraph fgraph) {
        fileGraphs.put(fgraph.getFile().getQualifiedName(), fgraph);
    }
    
    /**
     * Removes an operation history graph for a file.
     * @param finfo information about the file to be removed
     */
    void remove(CTFile finfo) {
        fileGraphs.remove(finfo.getQualifiedName());
    }
    
    /**
     * Obtains all operation history graphs for files.
     * @return the collection of the operation history graphs
     */
    public List<FileOpGraph> getFileGraphs() {
        List<FileOpGraph> graphs = new ArrayList<FileOpGraph>();
        for (FileOpGraph fgraph : fileGraphs.values()) {
            graphs.add(fgraph);
        }
        return graphs;
    }
    
    /**
     * Adds an edge to this graph.
     * @param node the edge to be added
     */
    void add(OpGraphEdge edge) {
        interEdges.add(edge);
        edge.getSrcNode().addOutgoingEdge(edge);
        edge.getDstNode().addIncomingEdge(edge);
    }
    
    /**
     * Removes an edge from this graph.
     * @param node the edge to be removed
     */
    void remove(OpGraphEdge edge) {
        interEdges.remove(edge);
        edge.getSrcNode().removeOutgoingEdge(edge);
        edge.getDstNode().removeIncomingEdge(edge);
    }
    
    /**
     * Tests if this graph contains a node in this graph.
     * @param node the node to be checked
     * @return <code>true</code> if this graph contains the node, otherwise <code>false</code>
     */
    public boolean contains(OpGraphNode node) {
        for (OpGraphNode n : getNodes()) {
            if (n.equals(node)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Tests if this graph contains an edge in this graph.
     * @param edge the edge to be checked
     * @return <code>true</code> if this graph contains the edge, otherwise <code>false</code>
     */
    public boolean contains(OpGraphEdge edge) {
        for (OpGraphEdge e : getEdges()) {
            if (e.equals(edge)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Returns the number of operation history graphs for files.
     * @return the number of the operation history graphs
     */
    int size() {
        return fileGraphs.values().size();
    }
    
    /**
     * Removes all the edges from this graph.
     */
    void removeAllEdges() {
        for (OpGraphEdge edge : interEdges) {
            edge.getSrcNode().removeOutgoingEdge(edge);
            edge.getDstNode().removeIncomingEdge(edge);
        }
        interEdges.clear();
    }
    
    /**
     * Returns all nodes in this graph.
     * @return the collection of the nodes
     */
    public Set<OpGraphNode> getNodes() {
        Set<OpGraphNode> nodes = new HashSet<OpGraphNode>();
        for (FileOpGraph fgraph : fileGraphs.values()) {
            nodes.addAll(fgraph.getNodes());
        }
        return nodes;
    }
    
    /**
     * Returns all edges in this graph.
     * @return the collection of the edges
     */
    public Set<OpGraphEdge> getEdges() {
        Set<OpGraphEdge> edges = new HashSet<OpGraphEdge>();
        for (FileOpGraph fgraph : fileGraphs.values()) {
            edges.addAll(fgraph.getEdges());
        }
        edges.addAll(interEdges);
        return edges;
    }
    
    /**
     * Collects change operation nodes in this graph.
     * @return the collection of the change operation nodes
     */
    List<OperationNode> getOperationNodes() {
        List<OperationNode> ns = new ArrayList<OperationNode>();
        for (FileOpGraph fgraph : fileGraphs.values()) {
            ns.addAll(fgraph.getOperationNodes());
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
        for (FileOpGraph fgraph : fileGraphs.values()) {
            ns.addAll(fgraph.getJavaConstructNodes());
        }
        OpGraphNode.<JavaMemberNode>sortNodes(ns);
        return ns;
    }
    
    /**
     * Obtains all change operations in this graph.
     * @return the collection of the change operations
     */
    public List<ICodeOperation> getOperations() {
        List<ICodeOperation> operations = new ArrayList<ICodeOperation>();
        for (FileOpGraph fgraph : fileGraphs.values()) {
            operations.addAll(fgraph.getOperations());
        }
        return operations;
    }
    
    /**
     * Obtains a node corresponding to a change operation in this graph.
     * @param op the code change operation
     * @return the found operation node, or <code>null</code> if none
     */
    public OperationNode getOperationNode(ICodeOperation op) {
        for (FileOpGraph fgraph : fileGraphs.values()) {
            OperationNode node = fgraph.getOperationNode(op);
            if (node != null) {
                return node;
            }
        }
        return null;
    }
    
    /**
     * Obtains a node corresponding to a Java construct in this graph.
     * @param con the Java construct
     * @param the found node, or <code>null</code> if none
     */
    public JavaMemberNode getJavaConstructNode(JavaConstruct con) {
        for (FileOpGraph fgraph : fileGraphs.values()) {
            JavaMemberNode node = fgraph.getJavaConstructNode(con);
            if (node != null) {
                return node;
            }
        }
        return null;
    }
    
    /**
     * Returns the snapshots of files appearing in this graph.
     * @return the collection of the snapshots
     */
    public List<ParseableSnapshot> getSnapshots() {
        List<ParseableSnapshot> snapshots = new ArrayList<ParseableSnapshot>();
        for (FileOpGraph fgraph : fileGraphs.values()) {
            snapshots.addAll(fgraph.getSnapshots());
        }
        return snapshots;
    }
    
    /**
     * Returns the string for printing the nodes in this graph.
     * @return the string on the nodes
     */
    String toStringOnNodes() {
        List<OpGraphNode> ns = new ArrayList<OpGraphNode>(getNodes());
        OpGraphNode.sortNodes(ns);
        
        StringBuilder buf = new StringBuilder();
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
        List<OpGraphEdge> es = new ArrayList<OpGraphEdge>(getEdges());
        OpGraphEdge.<OpGraphEdge>sortEdges(es);
        return toStringOnEdges("All", es);
    }
    
    /**
     * Returns the string for printing the edges in this graph.
     * @return the string on the edges
     */
    String toStringOnEdges(OpGraphEdge.Sort sort) {
        List<OpGraphEdge> es = new ArrayList<OpGraphEdge>();
        for (OpGraphEdge edge : getEdges()) {
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
        buf.append("-- Graph: " + projectInfo.getQualifiedName());
        buf.append(" F=" + fileGraphs.size());
        buf.append(" N=" + getNodes().size());
        buf.append("(" + "O:" + getOperationNodes().size() + ")");
        buf.append("(" + "J:" + getJavaConstructNodes().size() + ")");
        buf.append(" E=" + getEdges().size());
        buf.append("(" + getEdgeInfo(OpGraphEdge.Sort.BACKWARD_EDITING) + ")");
        buf.append("(" + getEdgeInfo(OpGraphEdge.Sort.FORWARD_EDITING) + ")");
        buf.append("(" + getEdgeInfo(OpGraphEdge.Sort.NO_CHANGE) + ")");
        buf.append("(" + getEdgeInfo(OpGraphEdge.Sort.CCP_EDITING) + ")");
        buf.append("(" + getEdgeInfo(OpGraphEdge.Sort.CCP_EDITING) + ")");
        buf.append("(" + getEdgeInfo(OpGraphEdge.Sort.CCP_EDITING) + ")");
        buf.append("(" + "I:" +interEdges.size() + ")" );
        buf.append(" SN=" + getSnapshots().size() + "\n");
        for (FileOpGraph fgraph : fileGraphs.values()) {
            buf.append("  " + fgraph.getFile().getQualifiedName());
            buf.append(fgraph.toSummaryString() + "\n");
        }
        return buf.toString();
    }
    
    /**
     * Returns the edge information.
     * @param sort the sort of the edge
     * @return the string for printing
     */
    private String getEdgeInfo(OpGraphEdge.Sort sort) {
        return sort.toString() + ":" + OpGraphEdge.<OpGraphEdge>extractEdges(getEdges(), sort).size();
    }
}
