/*
 *  Copyright 2017
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.changetracker.dependecygraph;

import org.jtool.changetracker.operation.ICodeOperation;
import org.jtool.changetracker.dependencyanalyzer.JavaConstruct;
import org.jtool.changetracker.repository.CTFile;
import java.util.List;
import java.util.ArrayList;

/**
 * Stores information on an operation history slice.
 * @author Katsuhisa Maruyama
 */
public class OpSlice {
    
    /**
     * The operation history graph for the whole project.
     */
    private OpGraphForProject projectGraph;
    
    /**
     * The operation history graph for a file that contains a code fragment for a slice criterion.
     */
    private OpGraphForFile fileGraph;
    
    /**
     * A code snippet corresponding to a code fragment for a slice criterion.
     */
    private CodeSnippet snippet;
    
    /**
     * A Java construct node corresponding to a code fragment for a slice criterion.
     */
    private JavaMemberNode jconNode = null;
    
    /**
     * The collection of change operations nodes within this operation history slice.
     */
    protected List<OperationNode> nodes;
    
    /**
     * The title of this operation history slice.
     */
    protected String title;
    
    /**
     * Prohibits the creation of an instance.
     */
    @SuppressWarnings("unused")
    private OpSlice() {
    }
    
    /**
     * Creates an instance that stores an operation history slice.
     * @param pgraph the operation history graph for the whole project
     * @param con a Java construct for a slice criterion
     */
    public OpSlice(OpGraphForProject pgraph, JavaConstruct con) {
        this.projectGraph = pgraph;
        this.fileGraph = projectGraph.get(con.getFile());
        this.snippet = new CodeSnippet(con);
        this.jconNode = fileGraph.getJavaConstructNode(con);
    }
    
    /**
     * Creates an instance that stores an operation history slice.
     * @param pgraph the operation history graph for the whole project
     * @param jcnode a Java construct node for a slice criterion
     */
    public OpSlice(OpGraphForProject pgraph, JavaMemberNode jcnode) {
        this.projectGraph = pgraph;
        this.fileGraph = projectGraph.get(jcnode.getJavaConstruct().getFile());
        this.snippet = new CodeSnippet(jcnode.getJavaConstruct());
        this.jconNode = jcnode;
    }
    
    /**
     * Creates an instance that stores an operation history slice.
     * @param pgraph the operation history graph for the whole project
     * @param finfo information about a file containing a code snippet
     * @param snip the code snippet for a slice criterion
     */
    public OpSlice(OpGraphForProject pgraph, CTFile finfo, CodeSnippet snip) {
        this.projectGraph = pgraph;
        this.fileGraph = projectGraph.get(finfo);
        this.snippet = snip;
    }
    
    /**
     * Makes a backward slice on the slice criterion of interest.
     * @param jcnode the Java construct node
     */
    public void createBackwardSlice() {
        if (jconNode != null) {
            nodes = OpSlicer.getOperationNodesInBackwardSlice(jconNode);
            title = jconNode.getQualifiedName() + " on snapshot of " + jconNode.getIndex();
        } else if (fileGraph != null && snippet != null) {
            nodes = OpSlicerOnCodeSnippet.getOperationNodesInBackwardSlice(fileGraph, snippet);
            title = snippet.getQualifiedName() + " on snapshot of " + snippet.getIndex();
        } else {
            title = "Cannot create an operation slice";
        }
    }
    
    /**
     * Makes a forward slice on the slice criterion of interest.
     * @param jcnode the Java construct node
     */
    public void createForwardSlice(JavaMemberNode jcnode) {
        if (jconNode != null) {
            nodes = OpSlicer.getOperationNodesInForwardSlice(jconNode);
            title = jconNode.getQualifiedName() + " on snapshot of " + jconNode.getIndex();
        } else if (fileGraph != null && snippet != null) {
            nodes = OpSlicerOnCodeSnippet.getOperationNodesInForwardSlice(fileGraph, snippet);
            title = snippet.getQualifiedName() + " on snapshot of " + snippet.getIndex();
        } else {
            title = "Cannot create an operation slice";
        }
    }
    
    /**
     * Returns the title of this operation history slice.
     * @return the title of the slice
     */
    public String getTile() {
        return title;
    }
    
    /**
     * Returns change operation nodes within this operation history slice
     * @return the collection of the change operation nodes
     */
    public List<OperationNode> getOperationNodes() {
        return nodes;
    }
    
    /**
     * Tests if this slice contains a given change operation node.
     * @param node the node to be checked
     * @return <code>true</code> if the slice contains the given change operation node, otherwise <code>false</code>
     */
    public boolean contain(OperationNode node) {
        for (OperationNode n : nodes) {
            if (n.equals(node)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Returns the size of this slice.
     * @return the slice size
     */
    public int size() {
        return nodes.size();
    }
    
    /**
     * Returns change operations within this slice
     * @return the collection of the change operations
     */
    public List<ICodeOperation> getOperations() {
        List<ICodeOperation> ops = new ArrayList<ICodeOperation>();
        for (OperationNode node : nodes) {
            ops.add(node.getOperation());
        }
        return ops;
    }
    
    /**
     * Tests if this slice contains a given change operation.
     * @param operation the operation to be checked
     * @return <code>true</code> if the slice contains contains the given change operation, otherwise <code>false</code>
     */
    public boolean contain(ICodeOperation operation) {
        for (ICodeOperation op : getOperations()) {
            if (op.equals(operation)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Returns the string for printing, which does not contain a new line character at its end.
     * @return the string for printing
     */
    public String toString() {
        StringBuilder buf = new StringBuilder();
        buf.append(title);
        buf.append("(" + nodes.size() + ")=\n");
        for (OperationNode node : nodes) {
            buf.append(node.getQualifiedName() + "\n");
        }
        return buf.toString();
    }
}
