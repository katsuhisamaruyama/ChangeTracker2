/*
 *  Copyright 2017
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.changetracker.repository;

import org.jtool.changetracker.dependencyanalyzer.ParseableSnapshot;
import org.jtool.changetracker.dependencyanalyzer.DependencyDetector;
import org.jtool.changetracker.operation.IChangeOperation;

import java.util.List;
import java.util.ArrayList;
import java.time.ZonedDateTime;

/**
 * Stores the information about a file.
 * @author Katsuhisa Maruyama
 */
public class CTFile extends CTResource {
    
    /**
     * The path name of this file.
     */
    protected String path;
    
    /**
     * The change operation history related to this file.
     */
    protected OperationHistory history;
    
    /**
     * The information about a project containing this file.
     */
    protected CTProject projectInfo;
    
    /**
     * The information about a package containing this file.
     */
    protected CTPackage packageInfo;
    
    /**
     * The time when this file information was last modified (or initially generated).
     */
    protected ZonedDateTime lastUpdatedTime;
    
    /**
     * The previous file that exists before the file rename or move.
     */
    private CTFile fileInfoFrom = null;
    
    /**
     * The next file that exists after the file rename or move.
     */
    private CTFile fileInfoTo = null;
    
    /**
     * The collections of all snapshots of this file.
     */
    private List<ParseableSnapshot> snapshots = new ArrayList<ParseableSnapshot>();
    
    /**
     * Creates an instance that stores information about this file.
     * @param pathinfo information about path of this file
     * @param prjinfo the information about a package containing this file
     * @param pkginfo the information about a package containing this file
     */
    CTFile(CTPath pathinfo, CTProject prjinfo, CTPackage pkginfo) {
        super(pathinfo.getFileName(), pathinfo.getQualifiedName());
        this.path = pathinfo.getPath();
        this.projectInfo = prjinfo;
        this.packageInfo = pkginfo;
        this.history = new OperationHistory();
    }
    
    /**
     * Clears the information about this file.
     */
    public void clear() {
        history.clear();
    }
    
    /**
     * Returns the path of this file.
     * @return the path string
     */
    public String getPath() {
        return path;
    }
    
    /**
     * Returns the branch of this file.
     * @return the branch name
     */
    public String getBranch() {
        return projectInfo.getBranch();
    }
    
    /**
     * Returns the information about a project containing this file.
     * @return the project information
     */
    public CTProject getProject() {
        return projectInfo;
    }
    
    /**
     * Returns the information about a package containing this file.
     * @return the package information
     */
    public CTPackage getPackage() {
        return packageInfo;
    }
    
    /**
     * Returns the history that stores all change operations related to this file.
     * @return the operation history
     */
    public OperationHistory getOperationHistory() {
        return history;
    }
    
    /**
     * Sets change operations related to this file.
     * @param ops the collection of the change operations related to the file
     */
    public void setOperations(List<IChangeOperation> ops) {
        history.clear();
        history.addAll(ops);
        lastUpdatedTime = ZonedDateTime.now();
    }
    
    /**
     * Returns all the change operations related to this file.
     * @return the collection of the change operations related to the file
     */
    public List<IChangeOperation> getOperations() {
        return history.getOperations();
    }
    
    /**
     * Returns the number of the change operations related to this file.
     * @return the number of the change operations
     */
    public int getNumberOfOprations() {
        return history.size();
    }
    
    /**
     * Stores a change operation related to this file.
     * @param op the operation to be stored
     */
    void addOperation(IChangeOperation op) {
        history.add(op);
        lastUpdatedTime = ZonedDateTime.now();
    }
    
    /**
     * Stores a change operation related to this file.
     * @param ops the collection of the change operations related to the file
     */
    void addOperations(List<IChangeOperation> ops) {
        history.addAll(ops);
        lastUpdatedTime = ZonedDateTime.now();
    }
    
    /**
     * Returns the time when this file information was last updated.
     * @return the last updated time of this file information
     */
    public ZonedDateTime getLastUpdatedTime() {
        return lastUpdatedTime;
    }
    
    /**
     * Sets the previous file that exists before the file rename or move.
     * @param finfo the information about the file that is backward connected to this file
     */
    public void setFileInfoFrom(CTFile finfo) {
        fileInfoFrom = finfo;
    }
    
    /**
     * Returns the previous file that exists before the file rename or move.
     * @return the information about the file that is backward connected to this file
     */
    public CTFile getDataInfoFrom() {
        return fileInfoFrom;
    }
    
    /**
     * Sets the next file that exists before the file rename or move.
     * @param finfo the information about the file that is forward connected from this file
     */
    public void setFileInfoTo(CTFile finfo) {
        fileInfoTo = finfo;
    }
    
    /**
     * Returns the next file that exists before the file rename or move.
     * @return the information about the file that is forward connected from this file
     */
    public CTFile getFileInfoTo() {
        return fileInfoTo;
    }
    
    /**
     * Finds the last change operation that was performed at the specified time or immediately before.
     * @param time the specified time
     * @return the index of the found operation, or <code>-1</code> if none
     */
    public int getLastOperationIndexBefore(ZonedDateTime time) {
        return history.getLastOperationIndexBefore(time);
    }
    
    /**
     * Finds the latest change operation that was performed at the specified time or immediately before.
     * @param time the specified time
     * @return the found operation, or <code>-1</code> if none
     */
    public IChangeOperation getLastOperationBefore(ZonedDateTime time) {
        int idx = getLastOperationIndexBefore(time);
        if (idx != -1) {
            return history.getOperation(idx);
        }
        return null;
    }
    
    /**
     * Finds the first change operation that was performed at the specified time or immediately after.
     * @param time the specified time
     * @return the index of the found operation, or <code>-1</code> if none
     */
    public int getFirstOperationIndexAfter(ZonedDateTime time) {
        return history.getFirstOperationIndexAfter(time);
    }
    
    /**
     * Finds the first change operation that was performed at the specified time or immediately after.
     * @param time the specified time
     * @return the found operation, or <code>-1</code> if none
     */
    public IChangeOperation getFirstOperationAfter(ZonedDateTime time) {
        int idx = getFirstOperationIndexAfter(time);
        if (idx != -1) {
            return history.getOperation(idx);
        }
        return null;
    }
    
    /**
     * Obtains change operations that were performed between time period.
     * @param stime the time of the starting point
     * @param to the time of the ending point
     * @return the collection of the change operations
     */
    public List<IChangeOperation> getOperations(ZonedDateTime stime, ZonedDateTime etime) {
        int from = getOperationIndexAt(stime);
        int to = getOperationIndexAt(etime);
        return history.getOperations(from, to);
    }
    
    /**
     * Finds the index of the change operation that was performed at the specified time.
     * @param time the specified time
     * @return the index of the found operation, or <code>-1</code> if none
     */
    public int getOperationIndexAt(ZonedDateTime time) {
        return history.getOperationIndexAt(time);
    }
    
    /**
     * Finds the change operation that was performed at the specified time.
     * @param time the specified time
     * @return the found operation, or <code>null</code> if none
     */
    public IChangeOperation getOperationAt(ZonedDateTime time) {
        int idx = history.getOperationIndexAt(time);
        if (idx != -1) {
            return history.getOperation(idx);
        }
        return null;
    }
    
    /**
     * Obtains change operations that were performed at the specified time or before.
     * @param time the time
     * @return the collection of the change operation nodes
     */
    public List<IChangeOperation> getOperationsBefore(ZonedDateTime time) {
        int to = getLastOperationIndexBefore(time);
        return history.getOperationsBefore(to);
    }
    
    /**
     * Obtains change operation nodes that were performed after a time.
     * @param time the time
     * @return the collection of the change operation nodes
     */
    public List<IChangeOperation> getOperationsAfter(ZonedDateTime time) {
        int from = getFirstOperationIndexAfter(time);
        return history.getOperationsAfter(from);
    }
    
    /**
     * Returns a change operation with a given index number.
     * @param index the index number of the change operation to be returned
     * @return the found change operation, or <code>null</code> if none
     */
    public IChangeOperation getOperation(int index) {
        return history.getOperation(index);
    }
    
    /**
     * Returns the first change operation.
     * @return the first change operation
     */
    public IChangeOperation getFirstOperation() {
        return history.getFirstOperation();
    }
    
    /**
     * Returns the last change operation.
     * @return the last change operation
     */
    public IChangeOperation getLastOperation() {
        return history.getLastOperation();
    }
    
    /**
     * Restores the contents of source code restored at the time when a specified change operation was performed.
     * @param index the index of the code change operation at the restoration point
     * @return the contents of the restored source code, <code>null</code> if the restoration fails
     */
    public String getCode(int index) {
        return history.getCode(index);
    }
    
    /**
     * Restores the contents of source code restored at the time when a specified change operation was performed.
     * @param curCode the contents of the current code
     * @param curIndex the index of the current code
     * @param index the index of the code change operation at the restoration point
     * @return the contents of restored source code, <code>null</code> if the restoration fails
     */
    public String getCode(String curCode, int curIndex, int index) {
        return history.getCode(curCode, curIndex, index);
    }
    
    /**
     * Sets the initial source code before no change operation is recorded.
     * @param code the contents of the initial source code
     */
    void setInitialCode(String code) {
        ParseableSnapshot sn = DependencyDetector.parse(this, -1, code);
        addSnapshot(sn);
    }
    
    /**
     * Adds a parseable snapshot for this file.
     * @param sn the parseable snapshot to be added
     */
    public void addSnapshot(ParseableSnapshot sn) {
        if (sn != null) {
            snapshots.add(sn);
        }
    }
    
    /**
     * Returns all parseable snapshot for this file.
     * @return the collection of the parseable snapshots
     */
    public List<ParseableSnapshot> getSnapshots() {
        return snapshots;
    }
    
    /**
     * Returns the last parseable snapshot for this file.
     * @param snapshot the last parseable, or <code>null</code> if no parseable snapshots have been stored
     */
    public ParseableSnapshot getLastSnapshot() {
        if (snapshots.size() > 0) {
            return snapshots.get(snapshots.size() - 1);
        };
        return null;
    }
    
    /**
     * Tests if this resource is the same as a given one.
     * @param resource the resource
     * @return <code>true</code> if the two resources are the same, otherwise <code>false</code>
     */
    public boolean equalsDeep(CTFile finfo) {
        if (finfo == null) {
            return false;
        }
        return super.equals(finfo) && history.equals(finfo.getOperationHistory());
    }
}
