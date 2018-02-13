/*
 *  Copyright 2018
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.changetracker.operation;

import org.jtool.changetracker.repository.CTPath;
import java.time.ZonedDateTime;

/**
 * Stores information about a resource operation.
 * @author Katsuhisa Maruyama
 */
public class ResourceOperation extends ChangeOperation {
    
    /**
     * The action of a file operation.
     */
    public enum Action {
        ADDED, REMOVED, CHANGED, RENAMED_FROM, RENAMED_TO, MOVED_FROM, MOVED_TO;
    }
    
    /**
     * The type of this macro.
     */
    public enum Target {
        PROJECT, PACKAGE, FILE, TYPE, OTHERS, NONE;
    }
    
    /**
     * The kind of the target of the resource.
     */
    protected Target target;
    
    /**
     * The path of the source or destination of the resource.
     */
    protected String srcDstPath;
    
    /**
     * Creates an instance storing information about this file operation.
     * @param time the time when the file operation was performed
     * @param pathinfo information about path of a resource on which the file operation was performed
     * @param action the action of the file operation
     * @param target the kind of the target of the resource
     * @param author the author's name
     */
    public ResourceOperation(ZonedDateTime time, CTPath pathinfo, String action, Target target, String author) {
        super(time, Type.FILE, pathinfo, action, author);
        this.target = target;
    }
    
    /**
     * Creates an instance storing information about this file operation.
     * @param time the time when the file operation was performed
     * @param pathinfo information about path of a resource on which the file operation was performed
     * @param target the kind of the target of the resource
     * @param action the action of the file operation
     */
    public ResourceOperation(ZonedDateTime time, CTPath pathinfo, String action, Target target) {
        super(time, Type.FILE, pathinfo, action);
        this.target = target;
    }
    
    /**
     * Returns the path of the source or destination for the rename or move.
     * @return the source or destination path for the rename or move
     */
    public String getSrcDstPath() {
        return srcDstPath;
    }
    
    /**
     * Returns the path of the source or destination for the rename or move.
     * @param the source or destination path for the rename or move
     */
    public void setSrcDstPath(String sdpath) {
        assert sdpath != null;
        this.srcDstPath = sdpath;
    }
    
    /**
     * Tests if the changed resource is a project.
     * @return <code>true</code> if the changed resource is a project, otherwise <code>false</code>
     */
    public boolean isProjectChange() {
        return target == Target.PROJECT;
    }
    
    /**
     * Tests if the changed resource is a package.
     * @return <code>true</code> if the changed resource is a package, otherwise <code>false</code>
     */
    public boolean isPackageChange() {
        return target == Target.PACKAGE;
    }
    
    /**
     * Tests if the changed resource is a file.
     * @return <code>true</code> if the changed resource is a file, otherwise <code>false</code>
     */
    public boolean isFileChange() {
        return target == Target.FILE;
    }
    
    /**
     * Tests if the changed target was added in this resource operation.
     * @return <code>true</code> if the changed target was added, otherwise <code>false</code>
     */
    public boolean isAdd() {
        return action.equals(Action.ADDED.toString());
    }
    
    /**
     * Tests if the changed target was removed in this resource operation.
     * @return <code>true</code> if the changed target was removed, otherwise <code>false</code>
     */
    public boolean isRemove() {
        return action.equals(Action.REMOVED.toString());
    }
    
    /**
     * Tests if the changed target was removed in this resource operation.
     * @return <code>true</code> if the changed target was removed, otherwise <code>false</code>
     */
    public boolean isChange() {
        return action.equals(Action.CHANGED.toString());
    }
    
    /**
     * Tests if the changed target was renamed from another in this resource operation.
     * @return <code>true</code> if the changed target was renamed from another, otherwise <code>false</code>
     */
    public boolean isRenameFrom() {
        return action.equals(Action.RENAMED_FROM.toString());
    }
    
    /**
     * Tests if the changed target was renamed to another in this resource operation.
     * @return <code>true</code> if the changed target was renamed to another, otherwise <code>false</code>
     */
    public boolean isRenameTo() {
        return action.equals(Action.RENAMED_TO.toString());
    }
    
    /**
     * Tests if the changed target was moved from another in this resource operation.
     * @return <code>true</code> if the changed target was moved from another, otherwise <code>false</code>
     */
    public boolean isMoveFrom() {
        return action.equals(Action.MOVED_FROM.toString());
    }
    
    /**
     * Tests if the changed target was moved to another in this resource operation.
     * @return <code>true</code> if the changed target was moved to another, otherwise <code>false</code>
     */
    public boolean isMoveTo() {
        return action.equals(Action.MOVED_TO.toString());
    }
    
    /**
     * Returns the string for printing.
     * @return the string for printing
     */
    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder();
        buf.append(super.toString());
        buf.append(" target=[" + target.toString() + "]");
        if (!getPath().equals(srcDstPath)) {
            if (isRemove() || isMoveTo() || isRenameTo()) {
                buf.append(" to [" + srcDstPath + "]");
            } else {
                buf.append(" from [" + srcDstPath + "]");
            }
        }
        return buf.toString();
    }
}
