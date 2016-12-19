/*
 *  Copyright 2016
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.changetracker.parser;

import org.jtool.changetracker.repository.FileInfo;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/**
 * Stores information about a Java element (a class, method, or field).
 * @author Katsuhisa Maruyama
 */
public class OpJavaElement {
    
    /**
     * The information about a file that contains this Java element.
     */
    protected FileInfo fileInfo;
    
    /**
     * The name for this Java element.
     */
    protected String name;
    
    /**
     * The code range of the source code for this Java element.
     */
    protected CodeRange codeRange;
    
    /**
     * The collection of code ranges related to this Java element.
     */
    private List<CodeRange> codeRanges = null;
    
    /**
     * The code ranges of code fragments that are not related to this Java element within this code range.
     */
    protected List<CodeRange> excludedCodeRanges = new ArrayList<CodeRange>();
    
    /**
     * The Java elements that are enclosed in this Java element.
     */
    protected List<OpJavaElement> elements = new ArrayList<OpJavaElement>(); 
    
    /**
     * Creates an instance that stores information about a Java element.
     * @param start the start point of the Java element on the source code
     * @param end the end point of the Java element on the source code
     * @param finfo the information about a file that contains the Java element
     * @param name the name of the Java element
     */
    public OpJavaElement(int start, int end, FileInfo finfo, String name) {
        this.codeRange = new CodeRange(start, end);
        this.fileInfo = finfo;
        this.name = name;
    }
    
    /**
     * Returns the code range of this Java element.
     * @return the code range of the Java element
     */
    public CodeRange getCodeRange() {
        return codeRange;
    }
    
    /**
     * Returns the start point of this Java element on the source code.
     * @return the offset value of the start point
     */
    public int getStart() {
        return codeRange.getStart();
    }
    
    /**
     * Returns the end point of this Java element on the source code.
     * @return the offset value of the end point.
     */
    public int getEnd() {
        return codeRange.getEnd();
    }
    
    /**
     * Returns the name of this Java element.
     * @return the Java element name
     */
    public String getName() {
        return name;
    }
    
    /**
     * Returns the full name of this Java element.
     * @return the Java element name
     */
    public String getFullName() {
        return fileInfo.getQualifiedName() + ":" + name;
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
     * Adds a Java element enclosed in this element. It is considered to be excluded from this element under range check.
     * @param element the Java element to be added
     */
    public void addJavaElement(OpJavaElement element) {
        elements.add(element);
        addExcludedCodeRange(element.getStart(), element.getEnd());
    }
    
    /**
     * Returns the elements that are enclosed in this Java element.
     * @return the collection of the enclosed elements.
     */
    public List<OpJavaElement> getEnclosedJavaElements() {
        return elements;
    }
    
    /**
     * Returns the code ranges that correspond to this Java element, not including the excluded code.
     * @return the collection of the code ranges
     */
    public List<CodeRange> getCodeRanges() {
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
     * Sorts the code ranges of Java elements by their respective start points.
     * @param ranges the collection of the code ranges to be sorted
     */
    private static void sortByStart(List<CodeRange> ranges) {
        Collections.sort(ranges, new Comparator<CodeRange>() {
            public int compare(CodeRange range1, CodeRange range2) {
                int start1 = range1.getStart();
                int start2 = range2.getStart();
                
                if (start2 > start1) {
                    return -1;
                } else if (start2 == start1) {
                    return 0;
                }else{
                    return 1;
                }
            }
        });
    }
    
    /**
     * Sorts the code ranges of Java elements by their respective end points.
     * @param ranges the collection of the code ranges to be sorted
     */
    private static void sortByEnd(List<CodeRange> ranges) {
        Collections.sort(ranges, new Comparator<CodeRange>() {
            public int compare(CodeRange range1, CodeRange range2) {
                int end1 = range1.getEnd();
                int end2 = range2.getEnd();
                
                if (end2 > end1) {
                    return -1;
                } else if (end2 == end1) {
                    return 0;
                }else{
                    return 1;
                }
            }
        });
    }
    
    /**
     * Returns the string for printing, which does not contain a new line character at its end. 
     * @return the string for printing
     */
    public String toString() {
        StringBuffer buf = new StringBuffer();
        buf.append("[");
        buf.append(String.valueOf(getStart()));
        buf.append("-");
        buf.append(String.valueOf(getEnd()));
        buf.append("] ");
        
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
        
        buf.append(getFullName());
        return buf.toString();
    }
}
