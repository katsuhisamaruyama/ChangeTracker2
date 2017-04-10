/*
 *  Copyright 2017
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.changetracker.operation;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Defines an abstract class that accesses information about the all kinds of operations.
 * @author Katsuhisa Maruyama
 */
public abstract class ChangeOperation implements IChangeOperation {
    
    /**
     * The time when this change operation was performed.
     */
    protected ZonedDateTime time;
    
    /**
     * The type of this change operation.
     */
    protected Type type;
    
    /**
     * The path name of a resource on which this change operation was performed.
     */
    protected String path;
    
    /**
     * The branch name of a resource on which this change operation was performed.
     */
    protected String branch;
    
    /**
     * The action of this change operation.
     */
    protected String action;
    
    /**
     * The name of a developer who performed this change operation.
     */
    protected String author;
    
    /**
     * The description of this change operation.
     */
    protected String description = "";
    
    /**
     * The identification number for compounded change operation including this change operation.
     */
    protected long compoundId = -1;
    
    /**
     * Creates an instance storing information about this change operation.
     * @param time the time when the change operation was performed
     * @param type the type of the change operation
     * @param path the path of a file on which the change operation was performed
     * @param branch the branch of a file on which the change operation was performed
     * @param action the action of the change operation
     * @param author the author's name
     */
    protected ChangeOperation(ZonedDateTime time, Type type, String path, String branch, String action, String author) {
        this.time = time;
        this.type = type;
        this.path = path;
        this.branch = branch;
        this.action = action;
        this.author = author;
    }
    
    /**
     * Creates an instance storing information about this change operation.
     * @param time the time when the change operation was performed
     * @param type the type of the change operation
     * @param path the path of a file on which the change operation was performed
     * @param branch the branch of a file on which the change operation was performed
     * @param action the action of the change operation
     */
    protected ChangeOperation(ZonedDateTime time, Type type, String path, String branch, String action) {
        this(time, type, path, branch, action, getUserName());
    }
    
    /**
     * Obtains the name of an author who writes code related to this change operation.
     * @return the author name
     */
    protected static String getUserName() {
        return System.getProperty("user.name");
    }
    
    /**
     * Returns the time when this change operation was performed.
     * @return the time of the change operation
     */
    @Override
    public ZonedDateTime getTime() {
        return time;
    }
    
    /**
     * Returns the time when this change operation was performed.
     * @return the <code>long</code> value that represents the time of the change operation
     */
    @Override
    public long getTimeAsLong() {
        return time.toInstant().toEpochMilli();
    }
    
    /**
     * Returns the time when this change operation was performed.
     * @param time the time of the change operation
     * @return the <code>String</code> value that represents the time of the change operation
     */
    public static String getTimeAsString(ZonedDateTime time) {
        return time.format(DateTimeFormatter.ISO_ZONED_DATE_TIME);
    }
    
    /**
     * Returns the formated information about the time when this change operation was performed.
     * @param time the time of the change operation
     * @return the formatted information about the time
     */
    public static String getFormatedTime(ZonedDateTime time) {
        return time.format(DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss.SSS"));
    }
    
    /**
     * Returns the time when this change operation was performed.
     * @param str the <code>String</code> value that represents the time of the change operation
     * @return the time of the change operation
     */
    public static ZonedDateTime getTime(String str) {
        return ZonedDateTime.parse(str, DateTimeFormatter.ISO_ZONED_DATE_TIME);
    }
    
    /**
     * Returns the type of this change operation.
     * @return the type of the change operation
     */
    @Override
    public Type getType() {
        return type;
    }
    
    /**
     * Returns the path of a file on which this change operation was performed.
     * @return the path of the change operation
     */
    @Override
    public String getPath() {
        return path;
    }
    
    /**
     * The path of a file on which this change operation was performed
     * @return the branch of the change operation
     */
    public void setPath(String path) {
        assert path != null;
        this.path = path;
    }
    
    /**
     * The branch of the file on which this change operation was performed.
     * @return the branch of the change operation
     */
    @Override
    public String getBranch() {
        return branch;
    }
    
    /**
     * The branch of a file on which this change operation was performed.
     * @return the branch of the change operation
     */
    public void setBranch(String branch) {
        assert branch != null;
        this.branch = branch;
    }
    
    /**
     * Returns the action of this change operation.
     * @return the action of the change operation
     */
    @Override
    public String getAction() {
        return action;
    }
    
    /**
     * Sets the action of this change operation.
     * @param action the action of the change operation
     */
    public void setAction(String action) {
        this.action = action;
    }
    
    /**
     * Returns the author name of this change operation.
     * @return the author's name
     */
    @Override
    public String getAuthor() {
        return author;
    }
    
    /**
     * Returns the description of this change operation.
     * @return the description
     */
    @Override
    public String getDescription() {
        return description;
    }
    
    /**
     * Sets the description of this change operation.
     * @param desc the description
     */
    public void setDescription(String desc) {
        assert desc != null;
        this.description = desc;
    }
    
    /**
     * Tests if this code change operations is compounded.
     * @return <code>true</code> if this code change operations is compounded, otherwise <code>false</code>
     */
    @Override
    public boolean isCompounded() {
        return compoundId >= 0;
    }
    
    /**
     * Returns the identification number for compounded change operations.
     * @return the identification number for the compounded change operations
     */
    @Override
    public long getCompoundId() {
        return compoundId;
    }
    
    /**
     * Sets the identification number for compounded change operations.
     * @param cid the identification number for compounded change operations
     */
    public void setCompoundId(long cid) {
        compoundId = cid;
    }
    
    /**
     * Tests if this change operation edits any text of code.
     * @return <code>true</code> if the change operation edits any text, otherwise <code>false</code>
     */
    @Override
    public boolean isDocument() {
         return type == Type.DOCUMENT;
    }
    
    /**
     * Tests if this change operation copies any text of code.
     * @return <code>true</code> if the change operation copies any text, otherwise <code>false</code>
     */
    @Override
    public boolean isCopy() {
         return type == Type.COPY;
    }
    
    /**
     * Tests if this change operation is related to a file.
     * @return <code>true</code> if the change operation is related to a file, otherwise <code>false</code>
     */
    @Override
    public boolean isFile() {
         return type == Type.FILE;
    }
    
    /**
     * Tests if this change operation is performed on a file represented by a branch and a path.
     * @param branch the branch of the file
     * @param path the path of the file
     * @return <code>true</code> if this change operation is performed on the specified file, otherwise <code>false</code>
     */
    @Override
    public boolean isPerformedOn(String branch, String path) {
        return this.branch.equals(branch) && this.path.equals(path);
    }
    
    /**
     * Tests if this change operation is the same as a given one.
     * @param op the change operation
     * @return <code>true</code> if the two change operations are the same, otherwise <code>false</code>
     */
    @Override
    public boolean equals(IChangeOperation op) {
        if (op == null) {
            return false;
        }
        
        return time.equals(op.getTime()) && branch.equals(op.getBranch()) && path.equals(op.getPath());
    }
    
    /**
     * Tests if this object is the same as a given object.
     * @param obj the object
     * @return <code>true</code> if the two objects are the same, otherwise <code>false</code>
     */
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ChangeOperation) {
            return equals((ChangeOperation)obj);
        }
        return false;
    }
    
    /**
     * Returns a hash code value for this object.
     * @return always <code>0</code> that means all objects have the same hash code
     */
    @Override
    public int hashCode() {
        return 0;
    }
    
    /**
     * Converts a text into its pretty one.
     * @param text the original text
     * @return the text consists of the first four characters not including the new line
     */
    protected String getShortText(String text) {
        if (text == null) {
            return "NULL";
        }
        
        final int LESS_LEN = 20;
        
        String text2;
        if (text.length() < LESS_LEN + 1) {
            text2 = text;
        } else {
            text2 = text.substring(0, LESS_LEN + 1) + "...";
        }
        
        return text2.replace('\n', '~');
    }
    
    /**
     * Returns the string for printing.
     * @return the string for printing
     */
    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder();
        buf.append(getFormatedTime(time));
        buf.append(" type=[" + type + "]");
        buf.append(" path=[" + path + "]");
        buf.append(" branch=[" + branch + "]");
        buf.append(" action=[" + action + "]");
        buf.append(" author=[" + author + "]");
        if (description.length() != 0) {
            buf.append(" description=[" + description + "]");
        }
        return buf.toString();
    }
}
