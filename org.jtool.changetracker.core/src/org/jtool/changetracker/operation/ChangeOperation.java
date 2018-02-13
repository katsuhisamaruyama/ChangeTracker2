/*
 *  Copyright 2017
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.changetracker.operation;

import org.jtool.changetracker.repository.CTFile;
import org.jtool.changetracker.repository.CTPath;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Defines an abstract class that accesses information about the all kinds of operations.
 * @author Katsuhisa Maruyama
 */
public abstract class ChangeOperation implements IChangeOperation {
    
    /**
     * The time when this change operation was performed.
     * There is no change operations with the same time within the same file
     * (with the same path and the same branch).
     */
    protected ZonedDateTime time;
    
    /**
     * The type of this change operation.
     */
    protected Type type;
    
    /**
     * Information about path of a resource on which this change operation was performed.
     */
    protected CTPath pathinfo;
    
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
    protected ZonedDateTime compoundTime = null;
    
    /**
     * The information about a file that this change operation affects.
     */
    protected CTFile fileInfo;
    
    /**
     * Creates an instance storing information about this change operation.
     * @param time the time when the change operation was performed
     * @param type the type of the change operation
     * @param pathinfo information about path of a resource on which the change operation was performed
     * @param action the action of the change operation
     * @param author the author's name
     */
    protected ChangeOperation(ZonedDateTime time, Type type, CTPath pathinfo, String action, String author) {
        this.time = time;
        this.type = type;
        this.pathinfo = pathinfo;
        this.action = action;
        this.author = author;
    }
    
    /**
     * Creates an instance storing information about this change operation.
     * @param time the time when the change operation was performed
     * @param type the type of the change operation
     * @param pathinfo information about path of a resource on which the change operation was performed
     * @param action the action of the change operation
     */
    protected ChangeOperation(ZonedDateTime time, Type type, CTPath pathinfo, String action) {
        this(time, type, pathinfo, action, getUserName());
    }
    
    /**
     * Obtains the name of an author who writes code related to this change operation.
     * @return the author name
     */
    protected static String getUserName() {
        return System.getProperty("user.name");
    }
    @Override
    /**
     * Returns the qualified name of this change operation.
     * @return the qualified name
     */
    public String getQualifiedName() {
        return String.valueOf(time.toInstant().toEpochMilli()) + "+" + pathinfo.getPath() + "+" + pathinfo.getBranch();
    }
    
    /**
     * Sets the time when this change operation was performed.
     * @param time the time of the change operation
     */
    public void setTime(ZonedDateTime time) {
        this.time = time;
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
     * @return the <code>long</code> value
     */
    @Override
    public long getTimeAsLong() {
        return time.toInstant().toEpochMilli();
    }
    
    /**
     * Returns the time when this change operation was performed.
     * @return the <code>String</code> value
     */
    @Override
    public String getTimeAsString() {
        return getTimeAsString(time);
    }
    
    /**
     * Returns the time when this change operation was performed.
     * @return the formatted <code>String</code> value
     */
    @Override
    public String getFormatedTime() {
        return getFormatedTime(time);
    }
    
    /**
     * Returns the string corresponding time.
     * @param time the time information
     * @return the string corresponding the time
     */
    public static String getTimeAsString(ZonedDateTime time) {
        if (time != null) {
            return time.format(DateTimeFormatter.ISO_ZONED_DATE_TIME);
        }
        return "";
    }
    
    /**
     * Returns the formated information about time.
     * @param time the time information
     * @return the formatted information about the time
     */
    public static String getFormatedTime(ZonedDateTime time) {
        if (time != null) {
            return time.format(DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss.SSS"));
        }
        return "";
    }
    
    /**
     * Obtains the time from string corresponding time.
     * @param str the string corresponding the time
     * @return the time information
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
     * Returns the name of a project containing a resource on which this change operation was performed.
     * @return the project name
     */
    @Override
    public String getProjectName() {
        return pathinfo.getProjectName();
    }
    
    /**
     * Returns the name of a package containing a resource on which this change operation was performed.
     * @return the package name
     */
    @Override
    public String getPackageName() {
        return pathinfo.getPackageName();
    }
    
    /**
     * Returns the name of a file on which this change operation was performed.
     * @return the file name without its location information
     */
    @Override
    public String getFileName() {
        return pathinfo.getFileName();
    }
    
    /**
     * Returns the path of a file on which this change operation was performed.
     * @return the path of the change operation
     */
    @Override
    public String getPath() {
        return pathinfo.getPath();
    }
    
    /**
     * The branch of the file on which this change operation was performed.
     * @return the branch of the change operation
     */
    @Override
    public String getBranch() {
        return pathinfo.getBranch();
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
     * @param the action of the change operation
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
        return compoundTime != null;
    }
    
    /**
     * Returns the time when the compounding change operation was performed.
     * @return the time for the compounding change operation
     */
    @Override
    public ZonedDateTime getCompoundTime() {
        return compoundTime;
    }
    
    /**
     * Sets the time when the compounding change operation was performed.
     * @param ctime the time for the compounding change operation
     */
    public void setCompoundTime(ZonedDateTime ctime) {
        compoundTime = ctime;
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
     * Tests if this change operation edits or copies any text of code.
     * @return <code>true</code> if the change operation edits or copies any text, otherwise <code>false</code>
     */
    @Override
    public boolean isDocumentOrCopy() {
        return isDocument() || isCopy();
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
     * Tests if this change operation is related to a command.
     * @return <code>true</code> if the change operation is related to a command, otherwise <code>false</code>
     */
    @Override
    public boolean isCommand() {
         return type == Type.COMMAND;
    }
    
    /**
     * Tests if this change operation is related to refactoring.
     * @return <code>true</code> if the change operation is related to refactoring, otherwise <code>false</code>
     */
    @Override
    public boolean isRefactor() {
         return type == Type.REFACTOR;
    }
    
    /**
     * Tests if this change operation is performed on a resource represented by a branch and a path.
     * @param branch the branch of the resource
     * @param path the path of the resource
     * @return <code>true</code> if this change operation is performed on the resource, otherwise <code>false</code>
     */
    @Override
    public boolean isPerformedOn(String branch, String path) {
        return getBranch().equals(branch) && getPath().equals(path);
    }
    
    /**
     * Obtains change operations that affect this change operation.
     * Note that this method execution requires much time for huge number of change operations.
     * Instead an operation history graph can be used if you code will invoke this method many times.
     * @return the collection of the affecting change operations
     */
    public List<ICodeOperation> getAffectingOperations() {
        return new ArrayList<ICodeOperation>();
    }
    
    /**
     * Tests if this change operation depends on a given change operation.
     * @param op the change operation that might affect this change operation
     * @return <code>true</code> if this change operation depends on the given operation, otherwise <code>false</code>
     */
    public boolean dependsOn(IChangeOperation op) {
        return false;
    }
    
    /**
     * Returns information about a file that this change operation affects.
     * @return the file information
     */
    public CTFile getFile() {
        return fileInfo;
    }
    
    /**
     * Sets information about a file that this change operation affects.
     * @param finfo the file information
     */
    public void setFile(CTFile finfo) {
        fileInfo = finfo;
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
        return time.equals(op.getTime()) && getBranch().equals(op.getBranch()) && getPath().equals(op.getPath());
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
     * Sorts change operations in time order.
     * @param ops the the collection of change operations to be sorted
     */
    public static void sort(List<? extends IChangeOperation> ops) {
        Collections.sort(ops, new Comparator<IChangeOperation>() {
            
            /**
             * Compares two code change operations for order.
             * @param op1 the first code change operation to be compared
             * @param op2 the second code change operation to be compared
             */
            public int compare(IChangeOperation op1, IChangeOperation op2) {
                ZonedDateTime time1 = op1.getTime();
                ZonedDateTime time2 = op2.getTime();
                
                if (time1.isAfter(time2)) {
                    return 1;
                } else if (time1.isBefore(time2)) {
                    return -1;
                } else {
                    return 0;
                }
            }
        });
    }
    
    /**
     * Converts a text into its pretty one.
     * @param text the original text
     * @return the text consists of several characters not including the new line
     */
    protected String getShortText(String text) {
        if (text == null) {
            return "NULL";
        }
        
        final int LESS_LEN = 20;
        
        String text2;
        if (text.length() < LESS_LEN) {
            text2 = text;
        } else {
            text2 = text.substring(0, LESS_LEN) + "...";
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
        buf.append(" path=[" + getPath() + "]");
        buf.append(" branch=[" + getBranch() + "]");
        buf.append(" resource=[" + getProjectName() + "/" + getPackageName() + "/" + getFileName() + "]");
        buf.append(" action=[" + action + "]");
        buf.append(" author=[" + author + "]");
        if (isCompounded()) {
            buf.append(" compound=" + getFormatedTime(compoundTime));
        }
        if (description.length() != 0) {
            buf.append(" description=[" + description + "]");
        }
        return buf.toString();
    }
}
