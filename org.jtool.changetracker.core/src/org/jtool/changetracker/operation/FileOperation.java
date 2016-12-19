/*
 *  Copyright 2016
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.changetracker.operation;

import java.time.ZonedDateTime;

/**
 * Stores information on a file operation.
 * @author Katsuhisa Maruyama
 */
public class FileOperation extends ChangeOperation {
    
    /**
     * The action of a file operation.
     */
    public enum Action {
        ADDED, REMOVED, OPENED, CLOSED, SAVED, ACTIVATED,
        MOVED_FROM, MOVED_TO, RENAMED_FROM, RENAMED_TO, CONTENT_CHANGED,
        GIT_ADDED, GIT_REMOVED, GIT_MODIFIED;
    }
    
    /**
     * The contents of the source code when this file operation was performed.
     */
    protected String code;
    
    /**
     * The name of a charset of the file.
     */
    protected String charset;
    
    /**
     * The path of the source or destination of the rename or move.
     */
    protected String srcDstPath;
    
    /**
     * Creates an instance storing information on this file operation.
     * @param time the time when the file operation was performed
     * @param path the path name of a resource on which the file operation was performed
     * @param branch the branch name of a resource on which the file operation was performed
     * @param action the action of the file operation
     * @param author the author's name
     */
    public FileOperation(ZonedDateTime time, String path, String branch, String action, String author) {
        super(time, Type.FILE, path, branch, action, author);
        this.code = "";
    }
    
    /**
     * Creates an instance storing information on this file operation.
     * @param time the time when the file operation was performed
     * @param path the path name of a resource on which the file operation was performed
     * @param branch the branch name of a resource on which the change operation was performed
     * @param action the action of the file operation
     */
    public FileOperation(ZonedDateTime time, String path, String branch, String action) {
        this(time, path, branch, action, ChangeOperation.getUserName());
    }
    
    /**
     * Returns the contents of the source code after the file operation was performed.
     * @return the contents of the source code
     */
    public String getCode() {
        return code;
    }
    
    /**
     * Sets the contents of the source code after the file operation was performed.
     * @param code the contents of the source code
     */
    public void setCode(String code) {
        this.code = code;
    }
    
    /**
     * Returns the name of a charset of the file.
     * @return the name of a charset of the source code
     */
    public String getCharset() {
        return charset;
    }
    
    /**
     * Returns the name of a charset of the file.
     * @param charset the name of a charset of the source code
     */
    public void setCharset(String charset) {
        this.charset = charset;
    }
    
    /**
     * Returns the path of the source or destination of the rename or move.
     * @return the the source or destination path of the rename or move
     */
    public String getSrcDstPath() {
        return srcDstPath;
    }
    
    /**
     * Returns the path of the source or destination of the rename or move.
     * @param the path of the source or destination of the rename or move
     */
    public void setSrcDstPath(String sdpath) {
        assert sdpath != null;
        this.srcDstPath = sdpath;
    }
    
    /**
     * Tests this file operation adds a file.
     * @return <code>true</code> if the file operation adds a file, otherwise <code>false</code>
     */
    public boolean isAdd() {
        return action.equals(Action.ADDED.toString());
    }
    
    /**
     * Tests this file operation removes a file.
     * @return <code>true</code> if the file operation removes a file, otherwise <code>false</code>
     */
    public boolean isRemove() {
        return action.equals(Action.REMOVED.toString());
    }
    
    /**
     * Tests this file operation opens a file.
     * @return <code>true</code> if the file operation opens a file, otherwise <code>false</code>
     */
    public boolean isOpen() {
        return action.equals(Action.OPENED.toString());
    }
    
    /**
     * Tests this file operation closes a file.
     * @return <code>true</code> if the file operation closes a file, otherwise <code>false</code>
     */
    public boolean isClose() {
        return action.equals(Action.CLOSED.toString());
    }
    
    /**
     * Tests this file operation saves a file.
     * @return <code>true</code> if the file operation saves a file, otherwise <code>false</code>
     */
    public boolean isSave() {
        return action.equals(Action.SAVED.toString());
    }
    
    /**
     * Tests this file operation moves a file from somewhere.
     * @return <code>true</code> if the file operation moves a file from somewhere, otherwise <code>false</code>
     */
    public boolean isMoveFrom() {
        return action.equals(Action.MOVED_FROM.toString());
    }
    
    /**
     * Tests this file operation moves a file to somewhere.
     * @return <code>true</code> if the file operation moves a file to somewhere, otherwise <code>false</code>
     */
    public boolean isMoveTo() {
        return action.equals(Action.MOVED_TO.toString());
    }
    
    /**
     * Tests if this macro changes the name of a file from the old one.
     * @return <code>true</code> if this macro changes the name of a file from the old one, otherwise <code>false</code>
     */
    public boolean isRenameFrom() {
        return action.equals(Action.RENAMED_FROM.toString());
    }
    
    /**
     * Tests if this macro changes the name of a file to the new one.
     * @return <code>true</code> if this macro changes the name of a file to the new one, otherwise <code>false</code>
     */
    public boolean isRenameTo() {
        return action.equals(Action.RENAMED_TO.toString());
    }
    
    /**
     * Tests if this macro changes the content of a file.
     * @return <code>true</code> if this macro changes the content of a file, otherwise <code>false</code>
     */
    public boolean isContentChange() {
        return action.equals(Action.CONTENT_CHANGED.toString());
    }
    
    /**
     * Tests if this macro adds a file to the git repository.
     * @return <code>true</code> if this macro adds a file to the git repository, otherwise <code>false</code>
     */
    public boolean isGitAdd() {
        return action.equals(Action.GIT_ADDED.toString());
    }
    
    /**
     * Tests if this macro removes a file from the git repository.
     * @return <code>true</code> if this macro removes a file from the git repository, otherwise <code>false</code>
     */
    public boolean isGitRemove() {
        return action.equals(Action.GIT_REMOVED.toString());
    }
    
    /**
     * Tests if this macro modifies a file within the git repository.
     * @return <code>true</code> if this macro modifies a file within the git repository, otherwise <code>false</code>
     */
    public boolean isGitModify() {
        return action.equals(Action.GIT_MODIFIED.toString());
    }
    
    /**
     * Returns the string for printing, which does not contain a new line character at its end.
     * @return the string for printing
     */
    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder();
        buf.append(super.toString());
        
        buf.append(" code=[" + getShortText(code) + "]");
        return buf.toString();
    }
}
