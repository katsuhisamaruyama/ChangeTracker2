/*
 *  Copyright 2016
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.changetracker.repository;

import org.jtool.changetracker.operation.IChangeOperation;
import java.util.List;
import java.time.ZonedDateTime;
import java.util.ArrayList;

/**
 * Stores information about a project.
 * @author Katsuhisa Maruyama
 */
public class ProjectInfo extends ElementInfo {
    
    /**
     * The collection of all packages within this project.
     */
    private List<PackageInfo> packages = new ArrayList<PackageInfo>();
    
    /**
     * Creates an instance that stores information about this project.
     * @param name the name of the project
     */
    ProjectInfo(String name) {
        super(name, name);
    }
    
    /**
     * Clears the information on this project.
     */
    public void clear() {
        packages.clear();
    }
    
    /**
     * Stores the package data.
     * @param data the package data to be stored
     */
    void addPackage(PackageInfo data) {
        if (!packages.contains(data)) {
            packages.add(data);
        }
    }
    
    /**
     * Returns all the data on the packages within this project.
     * @return the collection of the package data
     */
    public List<PackageInfo> getAllpackages() {
        ElementInfo.sort(packages);
        return packages;
    }
    
    /**
     * Returns all the data on the files within this project.
     * @return the collection of the file data
     */
    public List<FileInfo> getAllFiles() {
        List<FileInfo> files = new ArrayList<FileInfo>();
        for (PackageInfo pinfo : packages) {
            files.addAll(pinfo.getAllFiles());
        }
        
        ElementInfo.sort(files);
        return files;
    }
    
    /**
     * Returns all the code change operations related to this project.
     * @return the collection of the code change operations related to the project
     */
    public List<IChangeOperation> getOperations() {
        List<IChangeOperation> ops = new ArrayList<IChangeOperation>();
        for (FileInfo finfo : getAllFiles()) {
            ops.addAll(finfo.getOperations());
        }
        OperationHistory.sort(ops);
        return ops;
    }
    
    /**
     * Returns the time range for the code change operations related to this resource.
     * @return the time range, or <code>null</code> if this file information contains no code change operation
     */
    public TimeRange getTimeRange() {
        
        return timeRange;
    }
    
    /**
     * Returns the time that indicates the start point of the time range of this resource.
     * @return the start point, or <code>null</code> if this file information contains no code change operation
     */
    public ZonedDateTime getFromTime() {
        
        
        if (timeRange != null) {
            return timeRange.getFrom();
        }
        return null;
    }
    
    /**
     * Returns the time that indicates the end point of the time range of this resource.
     * @return the end point, or <code>null</code> if this file information contains no code change operation
     */
    public ZonedDateTime getToTime() {
        if (timeRange != null) {
            return timeRange.getTo();
        }
        return null;
    }
    
    
    /**
     * Checks mismatches code change operations within this workspace.
     */
    public void checkMismatches() {
        for (FileInfo finfo : getAllFiles()) {
            finfo.checkGeneralMismatches();
            finfo.checkCloseOpenMismatches();
        }
    }
}
