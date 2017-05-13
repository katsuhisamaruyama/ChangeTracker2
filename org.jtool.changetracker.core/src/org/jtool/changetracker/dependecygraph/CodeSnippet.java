/*
 *  Copyright 2017
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.changetracker.dependecygraph;

import org.jtool.changetracker.dependencyanalyzer.CodeRange;
import org.jtool.changetracker.dependencyanalyzer.JavaConstruct;
import org.jtool.changetracker.operation.ChangeOperation;
import org.jtool.changetracker.repository.ChangeTrackerFile;
import java.time.ZonedDateTime;

/**
 * Stores information on a code snippet on a (parseable or non-parseable) snapshot.
 * @author Katsuhisa Maruyama
 */
public class CodeSnippet {
    
    /**
     * The time of a snapshot containing this code snippet.
     */
    private ZonedDateTime time;
    
    /**
     * The code range of the source code for this code snippet.
     */
    private CodeRange codeRange;
    
    /**
     * The information about file that contains this code snippet.
     */
    private ChangeTrackerFile fileInfo;
    
    /**
     * Creates an instance that stores information about a code snippet.
     * @param time the time of a snapshot containing this code snippet
     * @param start the start point of this code snippet on the source code
     * @param text the contents of this code snippet
     * @param finfo information about file that contains this code snippet
     */
    public CodeSnippet(ZonedDateTime time, int start, int end, ChangeTrackerFile finfo) {
        this.time = time;
        this.codeRange = new CodeRange(start, end);
        this.fileInfo = finfo;
    }
    
    /**
     * Creates an instance that stores information about a code snippet.
     * @param construct a Java construct that produces the code snippet
     */
    public CodeSnippet(JavaConstruct construct) {
        this(construct.getTime(), construct.getStart(), construct.getEnd(), construct.getFile());
    }
    
    /**
     * Returns the time of the snapshot containing this code snippet.
     * @return the time of the snapshot
     */
    public ZonedDateTime getTime() {
        return time;
    }
    
    /**
     * Returns the index number of the snapshot containing this code snippet.
     * @return the index number of the snapshot
     */
    public int getIndex() {
        return fileInfo.getOperationIndexAt(time);
    }
    
    /**
     * Returns the code range of this code snippet.
     * @return the code range of the code snippet
     */
    public CodeRange getCodeRange() {
        return codeRange;
    }
    
    /**
     * Returns the start point of this code snippet on the source code.
     * @return the offset value of the start point
     */
    public int getStart() {
        return codeRange.getStart();
    }
    
    /**
     * Returns the end point of this code snippet on the source code.
     * @return the offset value of the end point.
     */
    public int getEnd() {
        return codeRange.getEnd();
    }
    
    /**
     * Returns the length of the text enclosed in this code snippet.
     * @return the length of the text
     */
    public int getLength() {
        return codeRange.getLength();
    }
    
    /**
     * Returns the qualified name of this code snippet.
     * @return the qualified name
     */
    public String getQualifiedName() {
        return ChangeOperation.getFormatedTime(time) + "+" + fileInfo.getQualifiedName() + "+" + codeRange.toString();
    }
    
    /**
     * Returns the text of this code snippet
     * @return the text of the code snippet
     */
    public String getText() {
        String code = fileInfo.getCode(getIndex());
        if (code != null && getEnd() < code.length()) {
            return code.substring(getStart(), getEnd() + 1);
        }
        return "";
    }
    
    /**
     * Returns information about file that contains this code snippet
     * @return the file information
     */
    public ChangeTrackerFile getFile() {
        return fileInfo;
    }
    
    /**
     * Tests if the offset value of a character is in this code snippet.
     * @param offset the offset value to be checked
     * @return <code>true</code> if the offset value in the range, other wise <code>false</code>
     */
    public boolean inRange(int offset) {
        return codeRange.inRange(offset);
    }
    
    /**
     * Returns the string for printing, which does not contain a new line character at its end.
     * @return the string for printing
     */
    public String toSimpleString() {
        StringBuilder buf = new StringBuilder();
        buf.append(ChangeOperation.getFormatedTime(time));
        buf.append(" [");
        buf.append(String.valueOf(getStart()));
        buf.append("-");
        buf.append(String.valueOf(getEnd()));
        buf.append("] ");
        buf.append(getText());
        return buf.toString();
    }
}
