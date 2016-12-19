/*
 *  Copyright 2014
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.changetracker.dependencygraph;

import org.jtool.changetracker.operation.ICodeOperation;
import org.jtool.changetracker.repository.FileInfo;
import org.jtool.changetracker.repository.ProjectInfo;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;

/**
 * Stores information about the operation dependency graph for a project.
 * @author Katsuhisa Maruyama
 */
public class ProjectOpDepGraph {
    
    /**
     * The information of the project corresponding to this graph.
     */
    private ProjectInfo projectInfo;
    
    /**
     * The collection of all operation dependency graphs for files with in the project.
     */
    private HashMap<String, FileOpDepGraph> fileGraphs;
    
    /**
     * The collection of edges that intertwines multiple operation dependency graphs for files.
     */
    private Set<OpDepGraphEdge> interEdges;
    
    /**
     * Creates an instance that stores information about the operation dependency graph for a project.
     * @param pinfo the information of the project
     */
    protected ProjectOpDepGraph(ProjectInfo pinfo) {
        this.projectInfo = pinfo;
        this.fileGraphs = new HashMap<String, FileOpDepGraph>();
        this.interEdges = new HashSet<OpDepGraphEdge>();
    }
    
    /**
     * Returns the information of the project corresponding to this graph.
     * @return the project information.
     */
    public ProjectInfo getProjectInfo() {
        return projectInfo;
    }
    
    /**
     * Clears this operation dependency graph.
     */
    void clear() {
        for (FileOpDepGraph graph : fileGraphs.values()) {
            graph.clear();
        }
        interEdges.clear();
    }
    
    /**
     * Returns the key for an operation dependence graph for each project.
     * @param pinfo the information about the project
     * @return the key string
     */
    static String getKey(ProjectInfo pinfo) {
        return pinfo.getQualifiedName() + "!" + pinfo.getFromTime().toInstant().toEpochMilli();
    }
    
    /**
     * Stores the operation dependency graph for a file.
     * @param fgraph the operation dependency graph to be stored
     */
    void add(FileOpDepGraph fgraph) {
        fileGraphs.put(FileOpDepGraph.getKey(fgraph.getFileInfo()), fgraph);
    }
    
    /**
     * Finds the operation dependency graph corresponding to a file.
     * @param finfo the information about the file to be found
     * @return the operation dependency graph for the file, or <code>null</code> if none
     */
    FileOpDepGraph get(FileInfo finfo) {
        return fileGraphs.get(FileOpDepGraph.getKey(finfo));
    }
    
    /**
     * Removes the operation dependency graph corresponding to a file.
     * @param finfo the information on the file to be removed
     */
    void remove(FileInfo finfo) {
        fileGraphs.remove(FileOpDepGraph.getKey(finfo));
    }
    
    /**
     * Obtains the operation dependency graphs corresponding to all the files.
     * @return the collection of the operation dependency graphs for the files
     */
    public List<FileOpDepGraph> getFileGraphs() {
        List<FileOpDepGraph> graphs = new ArrayList<FileOpDepGraph>();
        for (FileOpDepGraph fgraph : fileGraphs.values()) {
            graphs.add(fgraph);
        }
        return graphs;
    }
    
    /**
     * Adds an edge to this graph.
     * @param node the edge to be added
     */
    void add(OpDepGraphEdge edge) {
        interEdges.add(edge);
        edge.getSrcNode().addOutgoingEdge(edge);
        edge.getDstNode().addIncomingEdge(edge);
    }
    
    /**
     * Removes an edge from this graph.
     * @param node the edge to be removed
     */
    void remove(OpDepGraphEdge edge) {
        interEdges.remove(edge);
        edge.getSrcNode().removeOutgoingEdge(edge);
        edge.getDstNode().removeIncomingEdge(edge);
    }
    
    /**
     * Tests if this graph contains a node.
     * @param node the node to be checked
     * @return <code>true</code> if this graph contains the node, otherwise <code>false</code>
     */
    public boolean contains(OpDepGraphNode node) {
        for (OpDepGraphNode n : getAllNodes()) {
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
        for (OpDepGraphEdge e : getAllEdges()) {
            if (e.equals(edge)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Returns the number of operation dependence graphs for files.
     * @return the number of the file operation dependence graphs
     */
    int size() {
        return fileGraphs.values().size();
    }
    
    /**
     * Removes all the edges from this graph.
     */
    void removeAllEdges() {
        for (OpDepGraphEdge edge : interEdges) {
            edge.getSrcNode().removeOutgoingEdge(edge);
            edge.getDstNode().removeIncomingEdge(edge);
        }
        interEdges.clear();
    }
    
    /**
     * Returns all the nodes in this graph.
     * @return the collection of the nodes
     */
    Set<OpDepGraphNode> getAllNodes() {
        Set<OpDepGraphNode> nodes = new HashSet<OpDepGraphNode>();
        for (FileOpDepGraph fgraph : fileGraphs.values()) {
            nodes.addAll(fgraph.getAllNodes());
        }
        return nodes;
    }
    
    /**
     * Returns all the edges in this graph.
     * @return the collection of the edges
     */
    Set<OpDepGraphEdge> getAllEdges() {
        Set<OpDepGraphEdge> edges = new HashSet<OpDepGraphEdge>();
        for (FileOpDepGraph fgraph : fileGraphs.values()) {
            edges.addAll(fgraph.getAllEdges());
        }
        edges.addAll(interEdges);
        return edges;
    }
    
    /**
     * Obtains all the operations stored in this graph.
     * @return the collection of the operations
     */
    public List<ICodeOperation> getOperations() {
        List<ICodeOperation> operations = new ArrayList<ICodeOperation>();
        for (OpDepGraphNode node : getAllNodes()) {
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
        for (OpDepGraphNode node : getAllNodes()) {
            if (node.getTime().isEqual(operation.getTime())) {
                return node;
            }
        }
        return null;
    }
    
    /**
     * Stores the string for printing nodes into a buffer. 
     * @param buf the string buffer
     */
    private void printNodes(StringBuilder buf) {
        List<OpDepGraphNode> ns = new ArrayList<OpDepGraphNode>();
        for (OpDepGraphNode node : getAllNodes()) {
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
        for (OpDepGraphEdge edge : getAllEdges()) {
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
        
        buf.append("-- Graph " + getProjectInfo().getName());
        buf.append(" N=" + getAllNodes().size());
        buf.append(" E=" + getAllEdges().size());
        buf.append("(" + ProjectOpDepGraph.extractEdges(getAllEdges(), OpDepGraphEdge.Sort.NORMAL).size() + ")");
        buf.append("(" + ProjectOpDepGraph.extractEdges(getAllEdges(), OpDepGraphEdge.Sort.CPP).size() + ")");
        
        return buf.toString();
    }
    
    /**
     * Returns edges with a given sort.
     * @param edges the collection of edges
     * @param sort the sort of the edges to be extracted
     * @return the collection of the extracted edges
     */
    public static List<OpDepGraphEdge> extractEdges(Set<OpDepGraphEdge> edges, OpDepGraphEdge.Sort sort) {
        List<OpDepGraphEdge> es = new ArrayList<OpDepGraphEdge>();
        for (OpDepGraphEdge edge : edges) {
            if (sort == edge.getSort()) {
                es.add(edge);
            }
        }
        sortEdgesByDstId(es);
        sortEdgesBySrcId(es);
        
        return es;
    }
    
    /**
     * Sorts the nodes of the operation dependency graph.
     * @param ns the collection of the nodes to be sorted
     */
    public static void sortNodes(List<OpDepGraphNode> ns) {
        Collections.sort(ns, new Comparator<OpDepGraphNode>() {
            
            /**
             * Compares its two nodes in the order corresponding to identification numbers of nodes.
             * @param node1 the first node to be compared.
             * @param node2 the second node to be compared.
             */
            public int compare(OpDepGraphNode node1, OpDepGraphNode node2) {
                int id1 = node1.getId();
                int id2 = node2.getId();
                
                if (id2 > id1) {
                    return -1;
                } else if (id2 == id1) {
                    return 0;
                }else{
                    return 1;
                }
            }
        });
    }
    
    /**
     * Sorts the edges of the operation dependency graph.
     * @param es the collection of the edges to be sorted
     */
    public static void sortEdgesBySrcId(List<OpDepGraphEdge> es) {
        Collections.sort(es, new Comparator<OpDepGraphEdge>() {
            
            /**
             * Compares its two nodes in the order corresponding to identification numbers of source nodes.
             * @param node1 the first edge to be compared.
             * @param node2 the second edge to be compared.
             */
            public int compare(OpDepGraphEdge edge1, OpDepGraphEdge edge2) {
                int id1 = edge1.getSrcNode().getId();
                int id2 = edge2.getSrcNode().getId();
                
                if (id2 == id1) {
                    return 0;
                } else if (id2 > id1) {
                    return -1;
                } else {
                    return 1;
                }
            }
        });
    }
    
    /**
     * Sorts the edges of the operation dependency graph.
     * @param es the collection of the edges to be sorted
     */
    public static void sortEdgesByDstId(List<OpDepGraphEdge> es) {
        Collections.sort(es, new Comparator<OpDepGraphEdge>() {
            
            /**
             * Compares its two nodes in the order corresponding to identification numbers of destination nodes.
             * @param node1 the first edge to be compared.
             * @param node2 the second edge to be compared.
             */
            public int compare(OpDepGraphEdge edge1, OpDepGraphEdge edge2) {
                int id1 = edge1.getDstNode().getId();
                int id2 = edge2.getDstNode().getId();
                
                if (id2 == id1) {
                    return 0;
                } else if (id2 > id1) {
                    return -1;
                } else {
                    return 1;
                }
            }
        });
    }
}
