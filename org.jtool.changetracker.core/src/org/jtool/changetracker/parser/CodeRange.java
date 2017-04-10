/*
 *  Copyright 2017
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.changetracker.parser;

/**
 * Stores information about the range of code.
 * @author Katsuhisa Maruyama
 */
public class CodeRange {
    
    /**
     * The offset value of the start point of this code range on the source code.
     */
    private int start;
    
    /**
     * The offset value of the end point of this code range on the source code.
     */
    private int end;
    
    /**
     * Creates an instance that stores information about the code range.
     * @param start the start point of the code range
     * @param end the end point of the code range
     */
    CodeRange(int start, int end) {
        if (start > end) {
            this.start = end;
            this.end = start;
        } else {
            this.start = start;
            this.end = end;
        }
    }
    
    /**
     * Returns the start point on this code range.
     * @return the offset value of the start point
     */
    public int getStart() {
        return start;
    }
    
    /**
     * Returns the end point on this code range.
     * @return the offset value of the end point
     */
    public int getEnd() {
        return end;
    }
    
    /**
     * Tests if the offset value is in this code range.
     * @param offset the offset value to be checked
     * @return <code>true</code> if the offset value is in, otherwise <code>false</code>
     */
    public boolean inRange(int offset) {
        if (start <= offset && offset <= end) {
            return true;
        }
        return false;
    }
    
    /**
     * Tests if the offset value is in this code range without including the start point.
     * @param offset the offset value to be checked
     * @return <code>true</code> if the offset value is in, or <code>false</code> if the offset value is not in or is equals to the start value 
     */
    public boolean inRangeMore(int offset) {
        if (start < offset && offset <= end) {
            return true;
        }
        return false;
    }
    
    /**
     * Tests if the offset value is in this code range without including the end point.
     * @param offset the offset value to be checked
     * @return <code>true</code> if the offset value is in, or <code>false</code> if the offset value is not in or is equals to the end value 
     */
    public boolean inRangeLess(int offset) {
        if (start <= offset && offset < end) {
            return true;
        }
        return false;
    }
    
    /**
     * Tests if a given code range is in this code range.
     * @param range the code range to be checked
     * @return <code>true</code> if the given code range is in, otherwise <code>false</code>
     */
    public boolean inRangeTotally(CodeRange range) {
        if (start <= range.getStart() && range.getEnd() <= end) {
            return true;
        }
        return false;
    }
    
    /**
     * Tests if a given code range is partially in this code range.   
     * @param range the code range to be checked
     * @return <code>true</code> if the given code range is partially in, otherwise <code>false</code>
     */
    public boolean inRangePartially(CodeRange range) {
        if (range.getEnd() < start || end < range.getStart()) {
            return false;
        }
        return true;
    }
    
    /**
     * Returns the string for printing, which does not contain a new line character at its end. 
     * @return the string for printing
     */
    @Override
    public String toString() {
        StringBuffer buf = new StringBuffer();
        buf.append("[");
        buf.append(String.valueOf(getStart()));
        buf.append("-");
        buf.append(String.valueOf(getEnd()));
        buf.append("] ");
        return buf.toString();
    }
}
