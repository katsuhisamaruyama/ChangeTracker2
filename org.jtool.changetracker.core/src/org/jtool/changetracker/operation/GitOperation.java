/*
 *  Copyright 2016
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.changetracker.operation;

import java.time.ZonedDateTime;
import java.util.Set;
import java.util.Arrays;
import java.util.HashSet;

/**
 * Stores information on a git operation.
 * @author Katsuhisa Maruyama
 */
public class GitOperation extends ChangeOperation {
    
    /**
     * The action of this resource operation.
     */
    public enum Action {
        OPEN, REFS_CHANGED, INDEX_CHANGED;
    }
    
    /**
     * The list of files modified on disk relative to the index.
     */
    private Set<String> filesModified;
    
    /**
     * The list of files added to the index, not in HEAD.
     */
    private Set<String> filesAdded;
    
    /**
     * The list of files removed from index, but in HEAD.
     */
    private Set<String> filesRemoved;
    
    /**
     * Creates an instance storing information on this file operation.
     * @param time the time when the file operation was performed
     * @param path the path name of a resource on which the file operation was performed
     * @param branch the branch name of a resource on which the file operation was performed
     * @param action the action of the file operation
     * @param author the author's name
     */
    public GitOperation(ZonedDateTime time, String path, String branch, String action, String author) {
        super(time, Type.GIT, path, branch, action, author);
    }
    
    /**
     * Creates an instance storing information on this file operation.
     * @param time the time when the file operation was performed
     * @param path the path name of a resource on which the file operation was performed
     * @param branch the branch name of a resource on which the change operation was performed
     * @param action the action of the file operation
     */
    public GitOperation(ZonedDateTime time, String path, String branch, String action) {
        this(time, path, branch, action, ChangeOperation.getUserName());
    }
    
    /**
     * Tests if this macro represents an open action.
     * @return <code>true</code> if this macro represents an open action, otherwise <code>false</code>
     */
    public boolean isOpen() {
        return action.equals(Action.OPEN.toString());
    }
    
    /**
     * Tests if this macro represents a refs change action.
     * @return <code>true</code> if this macro represents a refs change action, otherwise <code>false</code>
     */
    public boolean isRefsChange() {
        return action.equals(Action.REFS_CHANGED.toString());
    }
    
    /**
     * Tests if this macro represents an index change action.
     * @return <code>true</code> if this macro represents an index change action, otherwise <code>false</code>
     */
    public boolean isIndexChange() {
        return action.equals(Action.INDEX_CHANGED.toString());
    }
    
    /**
     * Returns files added to the index.
     * @return the list of the names of the added files
     */
    public Set<String> getAddedFiles() {
        return filesAdded;
    }
    
    /**
     * Sets files added to the index.
     * @param files the list of the names of the added files
     */
    public void setAddedFiles(Set<String> files) {
        filesAdded = files;
    }
    
    /**
     * Returns files removed from index.
     * @return the list of the names of the removed files
     */
    public Set<String> getRemovedFiles() {
        return filesRemoved;
    }
    
    /**
     * Sets files removed from index.
     * @param files the list of the names of the removed files
     */
    public void setRemovedFiles(Set<String> files) {
        filesRemoved = files;
    }
    
    /**
     * Returns files modified on disk relative to the index.
     * @return the list of the names of the modified files
     */
    public Set<String> getModifiedFiles() {
        return filesModified;
    }
    
    /**
     * Sets files modified on disk relative to the index.
     * @param files the list of the names of the modified files
     */
    public void setModifiedFiles(Set<String> files) {
        filesModified = files;
    }
    
    /**
     * Obtains the name list string from the collection of names.
     * @param the collection of the names
     * @return the name list string
     */
    public static String getNameList(Set<String> names) {
        if (names.size() == 0) {
            return "";
        }
        StringBuilder buf = new StringBuilder();
        for (String name : names) {
            buf.append(":" + name);
        }
        return buf.substring(1);
    }
    
    /**
     * Obtains the collection of names from a name list string.
     * @param the name list string
     * @return the collection of the names
     */
    public static Set<String> getNameSet(String nameList) {
        String[] names = nameList.split(":");
        return new HashSet<String>(Arrays.asList(names));
    }
    
    /**
     * Returns the string for printing, which does not contain a new line character at its end.
     * @return the string for printing
     */
    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder();
        buf.append(super.toString());
        
        buf.append(" added=[" + getNameList(getAddedFiles()) + "]");
        buf.append(" removed=[" + getNameList(getRemovedFiles()) + "]");
        buf.append(" modified=[" + getNameList(getModifiedFiles()) + "]");
        return buf.toString();
    }
}
