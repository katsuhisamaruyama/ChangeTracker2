/*
 *  Copyright 2017
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.changetracker.operation;

import java.time.ZonedDateTime;
import java.util.List;

import org.jtool.changetracker.dependencyanalyzer.JavaConstruct;

/**
 * Defines an interface that accesses information about document and copy operations.
 * @author Katsuhisa Maruyama
 */
public interface ICodeOperation extends IChangeOperation {
    
    /**
     * The action of a document operation.
     */
    public enum Action {
        TYPING, CUT, COPY, PASTE, UNDO, REDO, CONTENT_CHANGE,
        REFACTORING, REFACTORING_UNDO, REFACTORING_REDO;
    }
    
    /**
     * Returns the qualified name of this change operation.
     * @return the qualified name
     */
    public String getQualifiedName();
    
    /**
     * Returns the time when this change operation was performed.
     * @return the time of the change operation
     */
    public ZonedDateTime getTime();
    
    /**
     * Returns the time when this change operation was performed.
     * @return the <code>long</code> value that represents the time of the change operation
     */
    public long getTimeAsLong();
    
    /**
     * Returns the path of the file on which this change operation was performed.
     * @return the path of the change operation
     */
    public String getPath();
    
    /**
     * The branch of the file on which this macro was performed.
     * @return the branch of the change operation
     */
    public String getBranch();
    
    
    /**
     * Returns the name of a project containing a resource on which this change operation was performed.
     * @return the project name
     */
    public String getProjectName();
    
    /**
     * Returns the name of a package containing a resource on which this change operation was performed.
     * @return the package name
     */
    public String getPackageName();
    
    /**
     * Returns the action of this change operation.
     * @return the action of the change operation
     */
    public String getAction();
    
    /**
     * Returns the author name of this change operation.
     * @return the author's name
     */
    public String getAuthor();
    
    /**
     * Returns the description of this change operation.
     * @return the description
     */
    public String getDescription();
    
    /**
     * Returns the leftmost offset of the text affected by this change operation.
     * @return the leftmost offset of the affected text
     */
    public int getStart();
    
    /**
     * Returns the contents of the text inserted by this change operation.
     * @return the contents of the inserted text, or the empty string
     */
    public String getInsertedText();
    
    /**
     * Returns the contents of the text deleted by this change operation.
     * @return the contents of the deleted text, or the empty string
     */
    public String getDeletedText();
    
    /**
     * Returns the contents of the text copied by this change operation.
     * @return the contents of the copied text, or the empty string
     */
    public String getCopiedText();
    
    /**
     * Returns Java constructs that this change operation backward affects.
     * @return the collection of the affected Java constructs
     */
    public List<JavaConstruct> getBackwardJavaConstructs();
    
    /**
     * Returns Java constructs that this change operation forward affects.
     * @return the collection of the affected Java constructs
     */
    public List<JavaConstruct> getForwardJavaConstructs();
    
    /**
     * Tests this change operation represents either cut, copy, or paste action.
     * @return <code>true</code> if the change operation represents the cut, copy, or paste, otherwise <code>false</code>
     */
    public boolean isCutCopyPaste();
    
    /**
     * Tests this change operation represents user's typing.
     * @return <code>true</code> if the change operation represents the typing, otherwise <code>false</code>
     */
    public boolean isTyping();
    
    /**
     * Tests this change operation represents a cut action.
     * @return <code>true</code> if the change operation represents the cut action, otherwise <code>false</code>
     */
    public boolean isCut();
    
    /**
     * Tests this change operation represents a copy action.
     * @return <code>true</code> if the change operation represents the copy action, otherwise <code>false</code>
     */
    public boolean isCopy();
    
    /**
     * Tests this change operation represents a paste action.
     * @return <code>true</code> if the change operation represents the paste action, otherwise <code>false</code>
     */
    public boolean isPaste();
    
    /**
     * Tests this change operation represents a undo action.
     * @return <code>true</code> if the change operation represents the undo action, otherwise <code>false</code>
     */
    public boolean isUndo();
    
    /**
     * Tests this change operation represents a redo action.
     * @return <code>true</code> if the change operation represents the redo action, otherwise <code>false</code>
     */
    public boolean isRedo();
    
    /**
     * Tests this change operation represents a resource update action.
     * @return <code>true</code> if the change operation represents the resource update action, otherwise <code>false</code>
     */
    public boolean isContentChange();
    
    /**
     * Tests this change operation represents a refactoring execution.
     * @return <code>true</code> if the change operation represents the refactoring, otherwise <code>false</code>
     */
    public boolean isRefactoringExec();
    /**
     * Tests this change operation represents a refactoring undo execution.
     * @return <code>true</code> if the change operation represents the refactoring, otherwise <code>false</code>
     */
    public boolean isRefactoringUndo();
    /**
     * Tests this change operation represents a refactoring redo execution.
     * @return <code>true</code> if the change operation represents the refactoring, otherwise <code>false</code>
     */
    public boolean isRefactoringRedo();
    
    /**
     * Tests this change operation represents a refactoring.
     * @return <code>true</code> if the change operation represents the refactoring, otherwise <code>false</code>
     */
    public boolean isRefactoring();
    
    /**
     * Returns the string for printing.
     * @return the string for printing
     */
    public String toString();
}
