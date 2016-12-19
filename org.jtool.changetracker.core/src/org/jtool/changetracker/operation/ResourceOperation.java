/*
 *  Copyright 2016
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.changetracker.operation;

import java.time.ZonedDateTime;

/**
 * Stores information on a resource operation.
 * @author Katsuhisa Maruyama
 */
public class ResourceOperation extends ChangeOperation {
    
    /**
     * The action of this resource operation.
     */
    public enum Action {
        ADDED, REMOVED, CHANGED, MOVED_FROM, MOVED_TO, RENAMED_FROM, RENAMED_TO,
        PROJECT, PACKAGE, FILE;
    }
    
    /**
     * The path name of the resource which this resource will rename or moved from/to.
     */
    protected String srcDstPath;
    
    /**
     * Creates an instance storing information on this resource operation.
     * @param time the time when the resource operation was performed
     * @param path the path name of a resource on which the resource operation was performed
     * @param branch the branch name of a resource on which the resource operation was performed
     * @param action the action of the resource operation
     * @param author the author's name
     */
    public ResourceOperation(ZonedDateTime time, String path, String branch, String action, String author) {
        super(time, Type.RESOURCE, path, branch, action, author);
    }
    
    /**
     * Creates an instance storing information on this resource operation.
     * @param time the time when the resource operation was performed
     * @param path the path name of a resource on which the resource operation was performed
     * @param branch the branch name of a resource on which the resource operation was performed
     * @param action the action of the resource operation
     * @param author the author's name
     */
    public ResourceOperation(ZonedDateTime time, String path, String branch, String action) {
        this(time, path, branch, action, ChangeOperation.getUserName());
    }
    
    /**
     * Returns the path name of the resource which this resource will rename or moved from/to.
     * @return the path name of the source or destination of the resource
     */
    public String getSrcDstPath() {
        return srcDstPath;
    }
    
    /**
     * Sets the path name of the resource which this resource will rename or moved from/to.
     * @param sdpath the path name of the source or destination of the resource
     */
    public void setSrcDstPath(String sdpath) {
        assert sdpath != null;
        this.srcDstPath = sdpath;
    }
    
    /**
     * Tests this resource operation adds a resource.
     * @return <code>true</code> if the resource operation adds a resource, otherwise <code>false</code>
     */
    public boolean isResourceAdd() {
        return action.equals(Action.ADDED.toString());
    }
    
    /**
     * Tests this resource operation removes a resource.
     * @return <code>true</code> if the resource operation removes a resource, otherwise <code>false</code>
     */
    public boolean isResourceRemove() {
        return action.equals(Action.REMOVED.toString());
    }
    
    /**
     * Tests this resource operation changes a resource.
     * @return <code>true</code> if the resource operation changes a resource, otherwise <code>false</code>
     */
    public boolean isResourceChange() {
        return action.equals(Action.CHANGED.toString());
    }
    
    /**
     * Tests this resource operation moves a resource from somewhere.
     * @return <code>true</code> if the resource operation moves a resource from somewhere, otherwise <code>false</code>
     */
    public boolean isResourceMoveFrom() {
        return action.equals(Action.MOVED_FROM.toString());
    }
    
    /**
     * Tests this resource operation moves a resource to somewhere.
     * @return <code>true</code> if the resource operation moves a resource to somewhere, otherwise <code>false</code>
     */
    public boolean isResourceMoveTo() {
        return action.equals(Action.MOVED_TO.toString());
    }
    
    /**
     * Tests this resource operation changes the name of a resource from the old one.
     * @return <code>true</code> if the resource operation changes the name of a resource from the old one, otherwise <code>false</code>
     */
    public boolean isResourceRenameFrom() {
        return action.equals(Action.RENAMED_FROM.toString());
    }
    
    /**
     * Tests this resource operation changes the name of a resource to the new one.
     * @return <code>true</code> if the resource operation changes the name of a resource to the new one, otherwise <code>false</code>
     */
    public boolean isResourceRenameTo() {
        return action.equals(Action.RENAMED_TO.toString());
    }
    
    /**
     * Tests this resource operation affects a project.
     * @return <code>true</code> if the resource operation affects a project, otherwise <code>false</code>
     */
    public boolean isProjectResource() {
        return action.equals(Action.PROJECT.toString());
    }
    
    /**
     * Tests this resource operation affects a package resource.
     * @return <code>true</code> if the resource operation affects a package, otherwise <code>false</code>
     */
    public boolean isPackageResource() {
        return action.equals(Action.PACKAGE.toString());
    }
    
    /**
     * Tests this resource operation affects a file.
     * @return <code>true</code> if the resource operation affects a file, otherwise <code>false</code>
     */
    public boolean isFileResource() {
        return action.equals(Action.FILE.toString());
    }
    
    /**
     * Returns the string for printing, which does not contain a new line character at its end.
     * @return the string for printing
     */
    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder();
        buf.append(super.toString());
        
        return buf.toString();
    }
}
