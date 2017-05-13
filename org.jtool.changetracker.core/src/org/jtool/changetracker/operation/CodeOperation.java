/*
 *  Copyright 2017
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.changetracker.operation;

import org.jtool.changetracker.dependencyanalyzer.JavaConstruct;
import org.jtool.changetracker.repository.ChangeTrackerFile;
import org.jtool.changetracker.repository.ChangeTrackerPath;

import java.util.List;
import java.util.ArrayList;
import java.time.ZonedDateTime;

/**
 * Defines an interface that accesses information about document and copy operations.
 * @author Katsuhisa Maruyama
 */
public abstract class CodeOperation extends ChangeOperation implements ICodeOperation {
    
    /**
     * The leftmost offset of the text affected by this change operation.
     */
    protected int start = -1;
    
    /**
     * The collection of Java constructs that this change operation backward affects.
     */
    protected List<JavaConstruct> backwardJavaConstructs;
    
    /**
     * The collection of Java constructs that this change operation forward affects.
     */
    protected List<JavaConstruct> forwardJavaConstructs;
    
    /**
     * Creates an instance storing information about this code change operation.
     * @param time the time when the code change operation was performed
     * @param type the type of the code change operation
     * @param pathinfo information about path of a resource on which the code change operation was performed
     * @param action the action of the code change operation
     * @param author the author's name
     */
    protected CodeOperation(ZonedDateTime time, Type type, ChangeTrackerPath pathinfo, String action, String author) {
        super(time, type, pathinfo, action, author);
    }
    
    /**
     * Creates an instance storing information about this code change operation.
     * @param time the time when the code change operation was performed
     * @param type the type of the code change operation
     * @param pathinfo information about path of a resource on which the code change operation was performed
     * @param action the action of the code change operation
     */
    protected CodeOperation(ZonedDateTime time, Type type, ChangeTrackerPath pathinfo, String action) {
        super(time, type, pathinfo, action);
    }
    
    /**
     * Sets the leftmost offset of the text affected by this change operation.
     * @param start the leftmost offset of the affected text
     */
    public void setStart(int start) {
        this.start = start;
    }
    
    /**
     * Returns the leftmost offset of the text affected by this change operation.
     * @return the leftmost offset of the affected text
     */
    @Override
    public int getStart() {
        return start;
    }
    
    /**
     * Returns Java constructs that this change operation backward affects.
     * @return the collection of the affected Java constructs
     */
    @Override
    public List<JavaConstruct> getBackwardJavaConstructs() {
        return backwardJavaConstructs;
    }
    
    /**
     * Sets Java constructs that this change operation backward affects.
     * @param cons the collection of the affected Java constructs
     */
    public void setBackwardJavaConstructs(List<JavaConstruct> cons) {
        backwardJavaConstructs = cons;
    }
    
    /**
     * Returns Java constructs that this change operation forward affects.
     * @return the collection of the affected Java constructs
     */
    @Override
    public List<JavaConstruct> getForwardJavaConstructs() {
        return backwardJavaConstructs;
    }
    
    /**
     * Sets Java constructs that this change operation forward affects.
     * @param cons the collection of the affected Java constructs
     */
    public void setForwardJavaConstructs(List<JavaConstruct> cons) {
        forwardJavaConstructs = cons;
    }
    
    /**
     * Tests this change operation represents either cut, copy, or paste action.
     * @return <code>true</code> if the change operation represents the cut, copy, or paste, otherwise <code>false</code>
     */
    @Override
    public boolean isCutCopyPaste() {
        return (isCut() || isCopy() || isPaste());
    }
    
    /**
     * Tests this change operation represents user's typing.
     * @return <code>true</code> if the change operation represents the typing, otherwise <code>false</code>
     */
    public boolean isTyping() {
        return action.equals(Action.TYPING.toString());
    }
    
    /**
     * Tests this change operation represents a cut action.
     * @return <code>true</code> if the change operation represents the cut action, otherwise <code>false</code>
     */
    @Override
    public boolean isCut() {
        return action.equals(Action.CUT.toString());
    }
    
    /**
     * Tests this change operation represents a copy action.
     * @return <code>true</code> if the change operation represents the copy action, otherwise <code>false</code>
     */
    @Override
    public boolean isCopy() {
        return action.equals(Action.COPY.toString());
    }
    
    /**
     * Tests this change operation represents a paste action.
     * @return <code>true</code> if the change operation represents the paste action, otherwise <code>false</code>
     */
    @Override
    public boolean isPaste() {
        return action.equals(Action.PASTE.toString());
    }
    
    /**
     * Tests this change operation represents a undo action.
     * @return <code>true</code> if the change operation represents the undo action, otherwise <code>false</code>
     */
    @Override
    public boolean isUndo() {
        return action.equals(Action.UNDO.toString());
    }
    
    /**
     * Tests this change operation represents a redo action.
     * @return <code>true</code> if the change operation represents the redo action, otherwise <code>false</code>
     */
    @Override
    public boolean isRedo() {
        return action.equals(Action.REDO.toString());
    }
    
    /**
     * Tests this change operation represents a resource update action.
     * @return <code>true</code> if the change operation represents the resource update action, otherwise <code>false</code>
     */
    @Override
    public boolean isContentChange() {
        return action.equals(Action.CONTENT_CHANGE.toString());
    }
    
    /**
     * Tests this change operation represents a refactoring execution.
     * @return <code>true</code> if the change operation represents the refactoring, otherwise <code>false</code>
     */
    @Override
    public boolean isRefactoringExec() {
        return action.equals(Action.REFACTORING.toString());
    }
    
    /**
     * Tests this change operation represents a refactoring undo execution.
     * @return <code>true</code> if the change operation represents the refactoring, otherwise <code>false</code>
     */
    @Override
    public boolean isRefactoringUndo() {
        return action.equals(Action.REFACTORING_UNDO.toString());
    }
    
    /**
     * Tests this change operation represents a refactoring redo execution.
     * @return <code>true</code> if the change operation represents the refactoring, otherwise <code>false</code>
     */
    @Override
    public boolean isRefactoringRedo() {
        return action.equals(Action.REFACTORING_REDO.toString());
    }
    
    /**
     * Tests this change operation represents a refactoring.
     * @return <code>true</code> if the change operation represents the refactoring, otherwise <code>false</code>
     */
    @Override
    public boolean isRefactoring() {
        return action.equals(Action.REFACTORING.toString()) ||
               action.equals(Action.REFACTORING_UNDO.toString()) ||
               action.equals(Action.REFACTORING_REDO.toString());
    }
    
    /**
     * Obtains change operations that affect this change operation.
     * @return the collection of the affecting change operations
     */
    @Override
    public List<ICodeOperation> getAffectingOperations() {
        List<ICodeOperation> retops = new ArrayList<ICodeOperation>();
        List<IChangeOperation> ops = fileInfo.getOperations();
        for (int idx = 0; idx < ops.size(); idx++) {
            if (ops.get(idx).isDocumentOrCopy()) {
                ICodeOperation cop = (ICodeOperation)ops.get(idx);
                if (cop.getTime().isBefore(getTime()) && dependsOn(cop)) {
                    retops.add(cop);
                }
            } else {
                break;
            }
        }
        return retops;
    }
    
    /**
     * Collects all code change operations that appear between a time period.
     * @param start the starting time of the time period
     * @param end the ending time of the time period
     * @return the collection of the code change operations, not including a change operation performed at the starting time.
     */
    public static List<CodeOperation> getOperations(ChangeTrackerFile finfo, ZonedDateTime start, ZonedDateTime end) {
        List<IChangeOperation> ops = finfo.getOperations(start, end);
        List<CodeOperation> retops = new ArrayList<CodeOperation>();
        if (ops.size() < 1) {
            return retops;
        }
        ops.remove(0);
        for (int idx = 1; idx < ops.size(); idx++) {
            IChangeOperation op = ops.get(idx);
            if (op.isDocumentOrCopy()) {
                retops.add((CodeOperation)op);
            }
        }
        return retops;
    }
    
    /**
     * Returns the string for printing.
     * @return the string for printing
     */
    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder();
        buf.append(super.toString());
        buf.append(" offset=" + start);
        return buf.toString();
    }
}
