/*
 *  Copyright 2017
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.changetracker.dependecygraph;

import org.jtool.changetracker.operation.ICodeOperation;
import java.util.List;
import java.util.ArrayList;
import java.time.ZonedDateTime;

/**
 * Stores information about a change operation node of the operation dependency graph.
 * @author Katsuhisa Maruyama
 */
public class OperationNode extends OpGraphNode {
    
    /**
     * The change operation corresponding to this node.
     */
    protected ICodeOperation operation;
    
    /**
     * The collection of the offsets after adjustment.
     */
    protected List<Integer> adjustedOffsetValues = new ArrayList<Integer>();
    
    /**
     * Creates a change operation node of an operation dependence graph.
     * @param fgraph an operation history graph for a file that contains this node
     * @param op the change operation
     */
    public OperationNode(FileOpGraph fgraph, ICodeOperation op) {
        super(fgraph);
        this.operation = op;
    }
    
    /**
     * Tests if this is a change operation node.
     * @return always <code>true</code>
     */
    @Override
    public boolean isOperation() {
        return true;
    }
    
    /**
     * Tests if this change operation node indicates the text changes.
     * @return <code>true</code> if this change operation node indicates the text changes, otherwise <code>false</code>
     */
    public boolean isDocNode() {
        return getCopiedTextLength() == 0;
    }
    
    /**
     * Tests if this change operation node indicates the text copy.
     * @return <code>true</code> if this change operation node indicates the text copy, otherwise <code>false</code>
     */
    public boolean isCopyNode() {
        return getCopiedTextLength() != 0;
    }
    
    /**
     * Returns the change operation corresponding to this node.
     * @return the change operation
     */
    public ICodeOperation getOperation() {
        return operation;
    }
    
    /**
     * Returns the qualified name for the Java element.
     * @return the qualified name
     */
    @Override
    public String getQualifiedName() {
        return operation.getQualifiedName();
    }
    
    /**
     * Returns the time for the change operation.
     * @return the time
     */
    @Override
    public ZonedDateTime getTime() {
        return operation.getTime();
    }
    
    /**
     * Returns the index number for the change operation.
     * @return the index number
     */
    @Override
    public int getIndex() {
        return fileGraph.getFile().getOperationIndexAt(getTime());
    }
    
    /**
     * Returns the leftmost offset value of the text affected by the code change operation.
     * @return the offset value
     */
    @Override
    public int getStart() {
        return operation.getStart();
    }
    
    /**
     * Returns the inserted text of the change operation.
     * @return the inserted text
     */
    public String getInsertedText() {
        return operation.getInsertedText();
    }
    
    /**
     * Returns the length of the inserted text of the change operation.
     * @return the length of the inserted text
     */
    public int getInsertedTextLength() {
        return getInsertedText().length();
    }
    
    /**
     * Returns the deleted text of the change operation.
     * @return the deleted text
     */
    public String getDeletedText() {
        return operation.getDeletedText();
    }
    
    /**
     * Returns the length of the deleted text of the change operation.
     * @return the length of the deleted text
     */
    public int getDeletedTextLength() {
        return getDeletedText().length();
    }
    
    /**
     * Returns the copied text of the change operation.
     * @return the copied text
     */
    public String getCopiedText() {
        return operation.getCopiedText();
    }
    
    /**
     * Returns the length of the copied text of the change operation.
     * @return the length of the copied text
     */
    public int getCopiedTextLength() {
        return getCopiedText().length();
    }
    
    /**
     * Returns the string for printing.
     * @return the string for printing
     */
    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder();
        buf.append("O ");
        buf.append(operation.toString());
        return buf.toString();
    }
}
