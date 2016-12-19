/*
 *  Copyright 2016
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.changetracker.repository;

import org.jtool.changetracker.operation.FileOperation;
import org.jtool.changetracker.operation.IChangeOperation;
import java.time.ZonedDateTime;
import java.util.List;

/**
 * Stores the information about a file.
 * @author Katsuhisa Maruyama
 */
public class FileInfo extends ElementInfo{
    
    /**
     * The path name of this file.
     */
    private String path;
    
    /**
     * The collection of all code change operations related to this file.
     */
    private OperationHistory history;
    
    /**
     * The information about a project containing this file.
     */
    private ProjectInfo projectInfo;
    
    /**
     * The information about a package containing this file.
     */
    private PackageInfo packageInfo;
    
    
    
    /**
     * The time when this file information was generated or last modified.
     */
    private ZonedDateTime lastUpdatedTime;
    
    /**
     * The previous file that exists before the file rename or move.
     */
    private FileInfo fileInfoFrom;
    
    /**
     * The next file that exists after the file rename or move.
     */
    private FileInfo fileInfoTo;
    
    /**
     * Creates an instance that stores information on this file.
     * @param name the name of the file
     * @param path the path name of the file
     * @param pinfo the information about a package containing this file
     */
    FileInfo(String name, String path, PackageInfo pinfo) {
        super(name, pinfo.getProject().getName() + "#" + pinfo.getName() + "." + name);
        this.path = path;
        this.packageInfo = pinfo;
        this.projectInfo = pinfo.getProject();
        
        this.history = new OperationHistory();
        
        this.fileInfoFrom = null;
        this.fileInfoTo = null;
    }
    
    /**
     * Clears the information on this file.
     */
    public void clear() {
        history.clear();
    }
    
    /**
     * Returns the path name of this file.
     * @return the path name string
     */
    public String getPath() {
        return path;
    }
    
    /**
     * Returns the information about a project containing this file.
     * @return the project information
     */
    public ProjectInfo getProjectInfo() {
        return projectInfo;
    }
    
    /**
     * Returns the information about a package containing this file.
     * @return the package information
     */
    public PackageInfo getPackageInfo() {
        return packageInfo;
    }
    
    /**
     * Returns all the code change operations related to this file.
     * @return the collection of the code change operations related to the file
     */
    public List<IChangeOperation> getOperations() {
        return history.getOperations();
    }
    
    /**
     * Returns the number of the code change operations related to this resource.
     * @return the number of the code change operations
     */
    public int getNumberOfOprations() {
        return history.size();
    }
    
    /**
     * Stores a code change operation related to the file.
     * @param operation the operation to be stored
     */
    void addOperation(IChangeOperation operation) {
        history.add(operation);
        
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
    public void setFileInfoFrom(FileInfo finfo) {
        fileInfoFrom = finfo;
    }
    
    /**
     * Returns the previous file that exists before the file rename or move.
     * @return the information about the file that is backward connected to this file
     */
    public FileInfo getDataInfoFrom() {
        return fileInfoFrom;
    }
    
    /**
     * Sets the next file that exists before the file rename or move.
     * @param finfo the information about the file that is forward connected from this file
     */
    public void setFileInfoTo(FileInfo finfo) {
        fileInfoTo = finfo;
    }
    
    /**
     * Returns the next file that exists before the file rename or move.
     * @return the information about the file that is forward connected from this file
     */
    public FileInfo getFileInfoTo() {
        return fileInfoTo;
    }
    
    /**
     * Finds the index of the former code change operation that was performed at the specified time or immediately before.
     * @param time the specified time
     * @return the index of the found operation, or <code>-1</code> if none
     */
    public int getFormerBy(ZonedDateTime time) {
        return history.getFormerBy(time);
    }
    
    /**
     * Finds the former code change operation that was performed at the specified time or immediately before.
     * @param time the specified time
     * @return the found operation, or <code>-1</code> if none
     */
    public IChangeOperation getFormerOperationBy(ZonedDateTime time) {
        int idx = getFormerBy(time);
        if (idx != -1) {
            return history.getOperation(idx);
        }
        return null;
    }
    
    /**
     * Finds the index of the latter code change operation that was performed at the specified time or immediately before.
     * @param time the specified time
     * @return the index of the found operation, or <code>-1</code> if none
     */
    public int getLatterBy(ZonedDateTime time) {
        return history.getLatterBy(time);
    }
    
    /**
     * Finds the latter code change operation that was performed at the specified time or immediately before.
     * @param time the specified time
     * @return the found operation, or <code>-1</code> if none
     */
    public IChangeOperation getLatterOperationBy(ZonedDateTime time) {
        int idx = getLatterBy(time);
        if (idx != -1) {
            return history.getOperation(idx);
        }
        return null;
    }
    
    /**
     * Finds the index of the code change operation that was performed at the specified time.
     * @param time the specified time
     * @return the index of the found operation, or <code>-1</code> if none
     */
    public int getBy(ZonedDateTime time) {
        return history.getBy(time);
    }
    
    /**
     * Finds the index of the code change operation that was performed at the specified time.
     * @param time the specified time
     * @return the found operation, or <code>-1</code> if none
     */
    public IChangeOperation getOperationBy(ZonedDateTime time) {
        int idx = history.getBy(time);
        if (idx != -1) {
            return history.getOperation(idx);
        }
        return null;
    }
    
    /**
     * Restores the contents of source code restored at the time when a specified code change operation was performed.
     * @param index the index of the code change operation at the restoration point
     * @return the contents of restored source code, <code>null</code> if the restoration fails
     */
    public String getCode(int index) {
        return history.restore(index);
    }
    
    /**
     * Restores the contents of source code restored at the time when a specified code change operation was performed.
     * @param curCode the current code
     * @param curIndex the index of the current code
     * @param index the index of the code change operation at the restoration point
     * @return the contents of restored source code, <code>null</code> if the restoration fails
     */
    public String getCode(String curCode, int curIndex, int index) {
        return history.restore(curCode, curIndex, index);
    }
    
    /**
     * Checks mismatches between two code change operations within this file.
     * @return <code>true</code> if mismatches were found, otherwise <code>false</code>
     */
    public boolean checkGeneralMismatches() {
        boolean errflag = false;
        List<IChangeOperation> ops = getOperations();
        
        for (int i = 0; i < ops.size(); i++) {
            IChangeOperation op = ops.get(i);
            
            if (op.isDocument()) {
                String code = getCode(i);
                
                if (code == null) {
                    System.err.println(" -- ERROR IN " + getPath() + " " + i);
                    errflag = true;
                    
                    int f = i + 1;
                    for ( ; f < ops.size(); f++) {
                        IChangeOperation op2 = ops.get(f);
                        if (op2.isFile()) {
                            i = f;
                            break;
                        }
                    }
                    
                    if (f >= ops.size()) {
                        break;
                    }
                }
            }
        }
        return errflag;
    }
    
    /**
     * Checks mismatches between a file close operation and a file open one within this file.
     * @return <code>true</code> if mismatches were found, otherwise <code>false</code>
     */
    public boolean checkCloseOpenMismatches() {
        List<IChangeOperation> ops = getOperations();
        
        for (int i = 0; i < ops.size(); i++) {
            IChangeOperation op = ops.get(i);
            
            if (op.isFile() && ((FileOperation)op).isClose()) {
                String closedCode = getCode(i);
                
                if (i + 1 < ops.size()) {
                    IChangeOperation op2 = ops.get(i + 1);
                    if (op2.isFile()) {
                        String openedCode = getCode(i + 1);
                        
                        if (closedCode != null && !closedCode.equals(openedCode)) {
                            System.err.println("-- CLOSE/OPEN MISMATCH IN " + getPath() + " " + (i + 1) + "NEED TO FIX");
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }
}
