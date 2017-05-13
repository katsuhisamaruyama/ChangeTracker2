/*
 *  Copyright 2017
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.changetracker.dependencyanalyzer;

import org.jtool.changetracker.operation.ChangeOperation;
import org.jtool.changetracker.repository.ChangeTrackerFile;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.time.ZonedDateTime;

/**
 * Stores information about a Java construct (a class, method, or field).
 * @author Katsuhisa Maruyama
 */
public class JavaConstruct {
    
    /**
     * The type of a change operation.
     */
    public enum Type {
        CLASS, METHOD, FIELD, INNER_CLASS;
    }
    
    /**
     * The type of this Java construct.
     */
    private Type type;
    
    /**
     * The name of this Java construct.
     */
    protected String name;
    
    /**
     * The parseable snapshot that contains this Java construct
     */
    protected ParseableSnapshot snapshot;
    
    /**
     * The code range of the source code for this Java construct.
     */
    protected CodeRange codeRange;
    
    /**
     * The collection of code ranges related to this Java construct.
     */
    private List<CodeRange> codeRanges = null;
    
    /**
     * The code ranges of code fragments that are not related to this Java construct within this code range.
     */
    protected List<CodeRange> excludedCodeRanges = new ArrayList<CodeRange>();
    
    /**
     * The Java constructs that are enclosed in this Java construct.
     */
    protected List<JavaConstruct> enclosedConstructs = new ArrayList<JavaConstruct>();
    
    /**
     * Creates an instance that stores information about a Java construct.
     * @param type the type of this Java construct.
     * @param start the start point of the Java construct on the source code
     * @param end the end point of the Java construct on the source code
     * @param name the name of this Java construct
     */
    public JavaConstruct(Type type, int start, int end, String name) {
        this.type = type;
        this.codeRange = new CodeRange(start, end);
        this.name = name;
    }
    
    /**
     * Tests if this Java construct represents a class.
     * @return <code>true</code> if this Java construct is a class
     */
    public boolean isClass() {
        return Type.CLASS.toString().equals(type.toString());
    }
    
    /**
     * Tests if this Java construct represents a method.
     * @return <code>true</code> if this Java construct is a method
     */
    public boolean isMethod() {
        return Type.METHOD.toString().equals(type.toString());
    }
    
    /**
     * Tests if this Java construct represents a field.
     * @return <code>true</code> if this Java construct is a field
     */
    public boolean isField() {
        return Type.FIELD.toString().equals(type.toString());
    }
    
    /**
     * Tests if this Java construct represents an inner class.
     * @return <code>true</code> if this Java construct is an inner class
     */
    public boolean isInnerClass() {
        return Type.INNER_CLASS.toString().equals(type.toString());
    }
    
    /**
     * Returns the time of a snapshot containing this Java construct.
     * @return the time of the snapshot
     */
    public ZonedDateTime getTime() {
        return snapshot.getTime();
    }
    /**
     * Returns the snapshot a parseable snapshot that contains this Java construct.
     * @return the parseable snapshot
     */
    public ParseableSnapshot getSnapshot() {
        return snapshot;
    }
    
    /**
     * Sets the snapshot a parseable snapshot that contains this Java construct.
     * @param sn a parseable snapshot
     */
    public void setSnapshot(ParseableSnapshot sn) {
        snapshot = sn;
    }
    
    /**
     * Returns information about a file that contains this Java construct.
     * @return the file information
     */
    public ChangeTrackerFile getFile() {
        return snapshot.getFile();
    }
    
    /**
     * Returns the index number of the snapshot containing the Java construct.
     * @return the index number of the Java construct
     */
    public int getIndex() {
        return snapshot.getIndex();
    }
    
    /**
     * Returns the start point of this Java construct on the source code.
     * @return the offset value of the start point
     */
    public int getStart() {
        return codeRange.getStart();
    }
    
    /**
     * Returns the end point of this Java construct on the source code.
     * @return the offset value of the end point.
     */
    public int getEnd() {
        return codeRange.getEnd();
    }
    
    /**
     * Returns the length of the text enclosed in this Java construct.
     * @return the length of the text
     */
    public int getLength() {
        return codeRange.getLength();
    }
    
    /**
     * Returns the name of this Java construct
     * @return the name
     */
    public String getName() {
        return name;
    }
    
    /**
     * Returns the text of this Java construct.
     * @return the text of the Java construct
     */
    public String getText() {
        String code = getFile().getCode(getIndex());
        if (code != null && getEnd() < code.length()) {
            return code.substring(getStart(), getEnd() + 1);
        }
        return "";
    }
    
    /**
     * Returns the qualified name of this Java construct.
     * @return the qualified name
     */
    public String getQualifiedName() {
        return ChangeOperation.getFormatedTime(getTime()) + "+" + getName() + "+" +
                getFile().getQualifiedName() + "+" + codeRange.toString();
    }
    
    /**
     * Returns the simple name of this Java construct.
     * @return the simple name without its class name
     */
    public String getSimpleName() {
        int index = name.indexOf('#');
        if (index != -1) {
            return name.substring(index + 1);
        }
        return name;
    }
    
    /**
     * Adds the code range that indicates exclusion of a code fragment
     * @param start the start point of the excluded code range
     * @param end the end point of the excluded code range
     */
    public void addExcludedCodeRange(int start, int end) {
        CodeRange range = new CodeRange(start, end);
        if (codeRange.inRangePartially(range)) {
            excludedCodeRanges.add(range);
            sortByEnd(excludedCodeRanges);
            sortByStart(excludedCodeRanges);
        }
    }
    
    /**
     * Adds a Java construct enclosed in this Java construct.
     * It is considered to be excluded from this Java construct under range check.
     * @param con the Java construct to be added
     */
    public void addJavaConstruct(JavaConstruct con) {
        enclosedConstructs.add(con);
        addExcludedCodeRange(con.getStart(), con.getEnd());
    }
    
    /**
     * Returns the constructs that are enclosed in this Java construct.
     * @return the collection of the enclosed constructs.
     */
    public List<JavaConstruct> getEnclosedJavaConstructs() {
        return enclosedConstructs;
    }
    
    /**
     * Tests if the offset value is in the insertion range of this Java construct.
     * @param offset the offset value to be checked
     * @return <code>true</code> if the offset value in the insertion range, other wise <code>false</code>
     */
    public boolean inRangeForInsertion(int offset) {
        if (!codeRange.inRangeMore(offset)) {
            return false;
        }
        for (CodeRange range : excludedCodeRanges) {
            if (range.inRangeMore(offset)) {
                return false;
            }
        }
        return true;
    }
    
    /**
     * Tests if the offset value is in the deletion range of this Java construct.
     * @param offset the offset value to be checked
     * @return <code>true</code> if the offset value in the deletion range, other wise <code>false</code>
     */
    public boolean inRangeForDeletion(int offset) {
        if (!codeRange.inRange(offset)) {
            return false;
        }
        for (CodeRange range : excludedCodeRanges) {
            if (range.inRange(offset)) {
                return false;
            }
        }
        return true;
    }
    
    /**
     * Returns the code ranges that correspond to this Java construct, not including the excluded code.
     * @return the collection of the code ranges
     */
    List<CodeRange> getCodeRanges() {
        if (codeRanges != null) {
            return codeRanges;
        }
        codeRanges = new ArrayList<CodeRange>();
        if (excludedCodeRanges.size() == 0) {
            codeRanges.add(new CodeRange(codeRange.getStart(), codeRange.getEnd()));
            return codeRanges;
        }
        
        int len = codeRange.getEnd() - codeRange.getStart() + 1;
        boolean code[] = new boolean[len];
        for (int i = 0; i < len; i++) {
            code[i] = true;
        }
        for (CodeRange range : excludedCodeRanges) {
            for (int j = range.getStart(); j <= range.getEnd(); j++) {
                if (codeRange.inRange(j)) {
                    code[j - codeRange.getStart()] = false;
                }
            }
        }
        
        int start = -1;
        int end = -1;
        for (int i = 0; i < len; i++) {
            if (code[i]) {
                if (start < 0) {
                    start = i;
                    if (i == len - 1) {
                        codeRanges.add(new CodeRange(start + codeRange.getStart(), start + codeRange.getStart()));
                    }
                } else {
                    end = i;
                    if (i == len - 1) {
                        codeRanges.add(new CodeRange(start + codeRange.getStart(), end + codeRange.getStart()));
                    }
                }
            } else {
                if (start >= 0) {
                    end = i - 1;
                    codeRanges.add(new CodeRange(start + codeRange.getStart(), end + codeRange.getStart()));
                    
                    start = -1;
                    end = -1;
                }
            }
        }
        return codeRanges;
    }
    
    /**
     * Sorts the code ranges of Java constructs by their respective start points.
     * @param rngs the collection of the code ranges to be sorted
     */
    private static void sortByStart(List<CodeRange> rngs) {
        Collections.sort(rngs, new Comparator<CodeRange>() {
            
            /**
             * Compares two code ranges for order.
             * @param rng1 the first code range to be compared
             * @param rng2 the second code range to be compared
             */
            @Override
            public int compare(CodeRange rng1, CodeRange rng2) {
                int start1 = rng1.getStart();
                int start2 = rng2.getStart();
                
                if (start2 > start1) {
                    return -1;
                } else if (start2 == start1) {
                    return 0;
                } else {
                    return 1;
                }
            }
        });
    }
    
    /**
     * Sorts the code ranges of Java constructs by their respective end points.
     * @param ranges the collection of the code ranges to be sorted
     */
    private static void sortByEnd(List<CodeRange> rngs) {
        Collections.sort(rngs, new Comparator<CodeRange>() {
            
            /**
             * Compares two code ranges for order.
             * @param op1 the first code range to be compared
             * @param op2 the second code range to be compared
             */
            @Override
            public int compare(CodeRange rng1, CodeRange rng2) {
                int end1 = rng1.getEnd();
                int end2 = rng2.getEnd();
                
                if (end2 > end1) {
                    return -1;
                } else if (end2 == end1) {
                    return 0;
                } else {
                    return 1;
                }
            }
        });
    }
    
    /**
     * Returns the string for printing.
     * @return the string for printing
     */
    @Override
    public String toString() {
        StringBuffer buf = new StringBuffer();
        if (isClass()) {
            buf.append("C(" + getIndex() + ")");
        } else if (isMethod()) {
            buf.append("M(" + getIndex() + ")");
        } else if (isField()) {
            buf.append("F(" + getIndex() + ")");
        }
        buf.append(getQualifiedName());
        if (excludedCodeRanges.size() > 0) {
            buf.append("!");
            for (CodeRange r : excludedCodeRanges) {
                buf.append("[");
                buf.append(String.valueOf(r.getStart()));
                buf.append("-");
                buf.append(String.valueOf(r.getEnd()));
                buf.append("] ");
            }
        }
        return buf.toString();
    }
}
