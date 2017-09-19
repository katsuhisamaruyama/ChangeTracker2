/*
 *  Copyright 2017
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.changetracker.dependecygraph;

import org.jtool.changetracker.dependencyanalyzer.JavaConstruct;
import java.time.ZonedDateTime;

/**
 * Stores information about a Java class member node of the operation dependency graph.
 * @author Katsuhisa Maruyama
 */
public class JavaMemberNode extends OpGraphNode {
    
    /**
     * The Java construct corresponding to this node.
     */
    protected JavaConstruct construct;
    
    /**
     * Creates a Java class member node of an operation dependence graph.
     * @param fgraph an operation history graph for a file that contains this node
     * @param con the Java construct
     */
    public JavaMemberNode(OpGraphForFile fgraph, JavaConstruct con) {
        super(fgraph);
        this.construct = con;
    }
    
    /**
     * Tests if this is a Java class member node.
     * @return always <code>true</code>
     */
    @Override
    public boolean isJavaConstruct() {
        return true;
    }
    
    /**
     * Returns the Java construct corresponding to this node.
     * @return the Java construct
     */
    public JavaConstruct getJavaConstruct() {
        return construct;
    }
    
    /**
     * Returns the name of the Java construct.
     * @return the name
     */
    public String getName() {
        return construct.getName();
    }
    
    /**
     * Returns the qualified name of the Java construct.
     * @return the qualified name
     */
    @Override
    public String getQualifiedName() {
        return construct.getQualifiedName();
    }
    
    /**
     * Returns the time for the Java construct.
     * @return the time
     */
    @Override
    public ZonedDateTime getTime() {
        return construct.getTime();
    }
    
    /**
     * Returns the index number for the Java construct.
     * @return the index number
     */
    @Override
    public int getIndex() {
        return fileGraph.getFile().getOperationIndexAt(getTime());
    }
    
    /**
     * Returns the leftmost offset value of the Java construct.
     * @return the offset value
     */
    @Override
    public int getStart() {
        return construct.getStart();
    }
    
    /**
     * Returns the end point of the Java construct on the source code.
     * @return the offset value of the end point.
     */
    public int getEnd() {
        return construct.getEnd();
    }
    
    /**
     * Returns the string for printing. 
     * @return the string for printing
     */
    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder();
        buf.append("J ");
        buf.append(construct.toString());
        return buf.toString();
    }
}
