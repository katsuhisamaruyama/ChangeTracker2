/*
 *  Copyright 2016
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.changetracker.operation;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Defines an abstract class that accesses information on the all kinds of operations.
 * @author Katsuhisa Maruyama
 */
public class ChangeOperation implements IChangeOperation {
    
    /**
     * The common action of an operation.
     */
    public enum CommonAction {
        REFACTOING, REFACTOING_UNDO, REFACTOING_REDO,
        QUICK_ASSIST, CONTENT_ASSIST;
    }
    
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
    protected String description;
    
    /**
     * The identification number for this change operation that can be bundled.
     */
    protected long bundleId;
    
    /**
     * Creates an instance storing information on this change operation.
     * @param time the time when the change operation was performed
     * @param type the type of the change operation
     * @param path the path name of a resource on which the change operation was performed
     * @param branch the branch name of a resource on which the change operation was performed
     * @param action the action of the change operation
     * @param author the author's name
     */
    protected ChangeOperation(ZonedDateTime time, Type type, String path, String branch, String action, String author) {
        this.time = time;
        this.type = type;
        if (path != null) {
            this.path = path;
        } else {
            this.path = GLOBAL_PATH;
        }
        this.branch = branch;
        this.action = action;
        this.author = author;
        this.description = "";
    }
    
    /**
     * Creates an instance storing information on this change operation.
     * @param time the time when the change operation was performed
     * @param type the type of the change operation
     * @param path the path name of a resource on which the change operation was performed
     * @param branch the branch name of a resource on which the change operation was performed
     * @param action the action of the change operation
     */
    protected ChangeOperation(ZonedDateTime time, Type type, String path, String branch, String action) {
        this(time, type, path, branch, action, getUserName());
    }
    
    /**
     * Creates an instance storing information on this change operation.
     * @param time the time when the change operation was performed
     * @param type the type of the change operation
     * @param path the path name of a resource on which the change operation was performed
     * @param branch the branch name of a resource on which the change operation was performed
     * @param action the action of the change operation
     */
    protected ChangeOperation(String timeStr, Type type, String path, String branch, String action, String author) {
        this(getTime(timeStr), type, path, branch, action, getUserName());
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
     * @return the time of the change operation
     */
    @Override
    public long getTimeAsLong() {
        return time.toInstant().toEpochMilli();
    }
    
    /**
     * Returns the time when this change operation was performed.
     * @return the time of the change operation
     */
    public static String getTimeAsString(ZonedDateTime t) {
        return t.format(DateTimeFormatter.ISO_ZONED_DATE_TIME);
    }
    
    /**
     * Returns the time when this change operation was performed.
     * @return the time of the change operation
     */
    public static ZonedDateTime getTime(String s) {
        return ZonedDateTime.parse(s, DateTimeFormatter.ISO_ZONED_DATE_TIME);
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
     * Returns the path name of the file on which this change operation was performed.
     * @return the path name of the change operation
     */
    @Override
    public String getPath() {
        return path;
    }
    
    /**
     * The name of the branch of a resource on which this macro was performed.
     * @return the branch name of the change operation
     */
    @Override
    public String getBranch() {
        return branch;
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
     * @param the description
     */
    public void setDescription(String info) {
        assert description != null;
        this.description = info;
    }
    
    /**
     * Tests if this code change operations can be bundled.
     * @return <code>true</code> if this code change operations can be bundled, otherwise <code>false</code>
     */
    @Override
    public boolean canBeBundled() {
        return bundleId > 0;
    }
    
    /**
     * Returns the identification number for document operations that can be bundled.
     * @return the identification number for the bundled document operations
     */
    public long getBundleId() {
        return bundleId;
    }
    
    /**
     * Sets the identification number for document operations that can be bundled.
     * @param bid the identification number for the bundled document operations
     */
    public void setBundleId(long bid) {
        bundleId = bid;
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
     * Tests if this change operation represents any commend.
     * @return <code>true</code> if the change operation represents any commend, otherwise <code>false</code>
     */
    @Override
    public boolean isCommand() {
         return type == Type.COMMAND;
    }
    
    /**
     * Tests if this change operation operates any file.
     * @return <code>true</code> if the change operation operates any file, otherwise <code>false</code>
     */
    @Override
    public boolean isFile() {
         return type == Type.FILE;
    }
    
    /**
     * Tests if this change operation changes any resource.
     * @return <code>true</code> if the change operation changes any resource, otherwise <code>false</code>
     */
    @Override
    public boolean isResource() {
         return type == Type.RESOURCE;
    }
    
    /**
     * Tests if this change operation represents any git command.
     * @return <code>true</code> if the change operation represents any git command, otherwise <code>false</code>
     */
    public boolean isGit() {
        return type == Type.GIT;
    }
    
    /**
     * Tests if this change operation is performed on a resource that exists in a path of a branch.
     * @param branch the branch of the resource
     * @param path the path of the resource
     * @return <code>true</code> if this change operation is performed on the specified resource, otherwise <code>false</code>
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
     * Obtains the formated time information.
     * @param time the time information
     * @return the formatted string of the time
     */
    public static String getFormatedTime(ZonedDateTime t) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss.SSS");
        return t.format(formatter);
    }
    
    /**
     * Returns the string for printing, which does not contain a new line character at its end.
     * @return the string for printing
     */
    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder();
        buf.append(getFormatedTime(time));
        buf.append(" type=[" + type + "]");
        buf.append(" path=[" + path + "]");
        buf.append(" action=[" + action + "]");
        buf.append(" author=[" + author + "]");
        buf.append(" description=[" + description + "]");
        
        return buf.toString();
    }
}
